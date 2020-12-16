package kz.kazpost.vpn

import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_transfer_invoice.*
import kotlinx.android.synthetic.main.progress.view.*
import kotlinx.android.synthetic.main.toast_green.view.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.acts.models.*
import kz.kazpost.vpn.adapters.TransferInvoiceAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.LabelsEnum
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.models.Labels
import kz.kazpost.vpn.models.RequestItems
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.ui.acceptinvoice.LoadDataModel
import kz.kazpost.vpn.utils.HTTPHelper
import kz.kazpost.vpn.utils.InternetHelper
import kz.kazpost.vpn.utils.PreferenceHelper
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.ref.WeakReference

class TransferInvoiceActivity : AppCompatActivity(), InternetHelper.Consumer {

    private lateinit var dialog: AlertDialog
    private lateinit var database: StationData
    private lateinit var transfers: MutableList<ItemModel>
    private lateinit var items: MutableList<ItemModel>
    private var tlabels: MutableList<LabelsModel> = mutableListOf()
    private val taskType1 = "sendData"
    private val taskType2 = "loadData"
    private val taskType3 = "transferData"
    private val taskType4 = "sendActs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_invoice)
        setSupportActionBar(ttoolbar)
        PreferenceHelper.saveStep(this, Step.TRANSFER_INVOICE)
        title = Step.TRANSFER_INVOICE.title

        database = PreferenceHelper.getDB(this)
        transfers = PreferenceHelper.getTransfers(this) ?: mutableListOf()
        items = PreferenceHelper.getItems(this) ?: mutableListOf()
        tlabels = transfersToLabelsList(transfers)

        InternetHelper(this, taskType1)

        tinvoice_station.text = "Станция: ".plus(database.road.filter{ road ->
            road.dept.name == database.nextDep
        }[0].dept.nameRu)

        tinvoice_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransferInvoiceAdapter(tlabels)
        }

        tinvoice_labels_bottom_plan.text = factCount(tlabels)
        tinvoice_labels_bottom_fact.text = factCount(tlabels)
        tinvoice_accept.isEnabled = false

        tinvoice_accept.setOnClickListener {
            InternetHelper(this, taskType3)
        }
    }

    override fun accept(internet: Boolean, type: String) {
        if (internet) {
            when (type) {
                taskType1 -> {
                    val sendData = RequestItems(
                        PreferenceHelper.getLogin(this),
                        database.fromDep,
                        database.transportListId,
                        database.index,
                        transfersToLabels(items),
                        transfersToItems(items)
                    )
                    SendDataAsync(this).execute(sendData)
                }
                taskType2 -> {
                    val tid = PreferenceHelper.getDB(this).transportListId
                    val index = PreferenceHelper.getDB(this).index
                    val loadData = LoadDataModel(
                        tid,
                        index + 1
                    )
                    LoadDataAsync(this).execute(loadData)
                }
                taskType3 -> {
                    val sendData = RequestItems(
                        PreferenceHelper.getLogin(this),
                        database.nextDep,
                        database.transportListId,
                        database.index + 1,
                        transfersToLabels(transfers),
                        transfersToItems(transfers)
                    )
                    TransferDataAsync(this).execute(sendData)
                }
                taskType4 -> {
                    val actModel = RequestActs(
                        PreferenceHelper.getLogin(this),
                        database.transportListId,
                        database.nextDep,
                        PreferenceHelper.getActs(this)
                    )
                    SendActsAsync(this).execute(actModel)
                }
                else -> return
            }
        } else {
            val toast = Toast(this)
            toast.duration = Toast.LENGTH_LONG
            val v = layoutInflater.inflate(R.layout.toast_red, null)
            v.red_text.text = "Нет интернета"
            toast.view = v
            toast.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_accept_invoice, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return if (id == R.id.action_refresh) {
            InternetHelper(this, taskType2)
            true
        } else super.onOptionsItemSelected(item)
    }

    companion object {

        class SendDataAsync internal constructor(context: TransferInvoiceActivity)
            : AsyncTask<RequestItems, Void, Response<ResponseBody>>() {
            private val activityReference: WeakReference<TransferInvoiceActivity> = WeakReference(context)
            override fun doInBackground(vararg params: RequestItems?): Response<ResponseBody>? {
                return params[0]?.let {
                    HTTPHelper.postVerifyItems().post(it).execute()
                }
            }
            override fun onPreExecute() {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    val builder = AlertDialog.Builder(context)
                    val view = context.layoutInflater.inflate(R.layout.progress, null)
                    view.progress_text.text = "Отправка данных..."
                    builder.setView(view)
                    context.dialog = builder.create()
                    context.dialog.setCancelable(false)
                    context.dialog.setCanceledOnTouchOutside(false)
                    context.dialog.show()
                }
            }
            override fun onPostExecute(result: Response<ResponseBody>?) {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    context.dialog.dismiss()
                    result?.body()?.let {
                        val toast = Toast(context)
                        toast.duration = Toast.LENGTH_LONG
                        val v = context.layoutInflater.inflate(R.layout.toast_green, null)
                        v.green_text.text = "Данные отправлены успешно!"
                        toast.view = v
                        toast.show()
                        InternetHelper(context, context.taskType4)
                    }
                }
            }
        }

        class SendActsAsync internal constructor(context: TransferInvoiceActivity): AsyncTask<RequestActs, Void, String>() {
            private val activityReference: WeakReference<TransferInvoiceActivity> = WeakReference(context)
            override fun doInBackground(vararg params: RequestActs?): String? {
                params[0]?.let {
                    if (it.acts != null) {
                        for (i in 0 until it.acts.size) {
                            val violations = Violations(it.acts[i].violationId, it.acts[i].violationName)
                            if (it.acts[i].typeRpo == "label") {
                                val acts = ActLabel(it.acts[i].shpi, it.acts[i].typeAct, it.acts[i].reasonId, it.acts[i].content)
                                val request = RequestActLabel(it.login, it.transportId, it.currentDepartment, acts, violations)
                                HTTPHelper.postActLabels().post(request).execute()
                            } else {
                                if (it.acts[i].typeAct == "51") {
                                    val acts = Act51(it.acts[i].shpi, it.acts[i].reasonId, it.acts[i].content)
                                    val request = RequestAct51(it.login, it.transportId, it.currentDepartment, acts, violations)
                                    HTTPHelper.postAct51().post(request).execute()
                                } else {
                                    val acts = Act30(it.acts[i].shpi, it.acts[i].reasonId, it.acts[i].content)
                                    val request = RequestAct30(it.login, it.transportId, it.currentDepartment, acts, violations)
                                    HTTPHelper.postAct30().post(request).execute()
                                }
                            }
                        }
                        return "SUCCESS"
                    }
                    return "ERROR"
                }
                return "ERROR"
            }
            override fun onPreExecute() {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    val builder = AlertDialog.Builder(context)
                    val view = context.layoutInflater.inflate(R.layout.progress, null)
                    view.progress_text.text = "Отправка данных..."
                    builder.setView(view)
                    context.dialog = builder.create()
                    context.dialog.setCancelable(false)
                    context.dialog.setCanceledOnTouchOutside(false)
                    context.dialog.show()
                }
            }
            override fun onPostExecute(result: String?) {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    context.dialog.dismiss()
                    if (result == "SUCCESS") {
                        PreferenceHelper.saveActs(context, mutableListOf())
                        val toast = Toast(context)
                        toast.duration = Toast.LENGTH_LONG
                        val v = context.layoutInflater.inflate(R.layout.toast_green, null)
                        v.green_text.text = "Данные отправлены успешно!"
                        toast.view = v
                        toast.show()
                        InternetHelper(context, context.taskType2)
                    }
                }
            }
        }

        class LoadDataAsync internal constructor(context: TransferInvoiceActivity): AsyncTask<LoadDataModel, Void, Response<StationData>>() {
            private val activityReference: WeakReference<TransferInvoiceActivity> = WeakReference(context)
            override fun doInBackground(vararg params: LoadDataModel?): Response<StationData>? {
                return params[0]?.let {
                    HTTPHelper.getStationData().get(it.tid, it.index).execute()
                }
            }
            override fun onPreExecute() {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    val builder = AlertDialog.Builder(context)
                    val view = context.layoutInflater.inflate(R.layout.progress, null)
                    view.progress_text.text = "Проверка поезда на прибытие..."
                    builder.setView(view)
                    context.dialog = builder.create()
                    context.dialog.setCancelable(false)
                    context.dialog.setCanceledOnTouchOutside(false)
                    context.dialog.show()
                }
            }
            override fun onPostExecute(result: Response<StationData>?) {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    context.dialog.dismiss()
                    result?.body()?.let {
                        if (it.result == "success") {
                            context.tinvoice_accept.isEnabled = it.status == "Arrived"
                        }
                    }
                }
            }
        }

        class TransferDataAsync internal constructor(context: TransferInvoiceActivity)
            : AsyncTask<RequestItems, Void, Response<ResponseBody>>() {
            private val activityReference: WeakReference<TransferInvoiceActivity> = WeakReference(context)
            override fun doInBackground(vararg params: RequestItems?): Response<ResponseBody>? {
                return params[0]?.let {
                    HTTPHelper.postSendItems().post(it).execute()
                }
            }
            override fun onPreExecute() {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    val builder = AlertDialog.Builder(context)
                    val view = context.layoutInflater.inflate(R.layout.progress, null)
                    view.progress_text.text = "Отправка данных..."
                    builder.setView(view)
                    context.dialog = builder.create()
                    context.dialog.setCancelable(false)
                    context.dialog.setCanceledOnTouchOutside(false)
                    context.dialog.show()
                }
            }
            override fun onPostExecute(result: Response<ResponseBody>?) {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    context.dialog.dismiss()
                    println(result)
                    println(result?.message())
                    result?.body()?.let {
                        PreferenceHelper.saveTransfers(context, mutableListOf())
                        val intent = Intent(context, TransferEndActivity::class.java)
                        context.startActivity(intent)
                        context.finish()
//                        if (PreferenceHelper.getIndex(context)+1 >= context.database.road.count()) {
//                            val intent = Intent(context, TransferLeftActivity::class.java)
//                            context.startActivity(intent)
//                            context.finish()
//                        } else { }
                    }
                }
            }
        }
    }

    private fun factCount(tlabels: MutableList<LabelsModel>): String {
        var count = 0
        for (tlabel in tlabels) {
            count += tlabel.plan
        }
        return count.toString()
    }

    private fun transfersToLabelsList(transfers: MutableList<ItemModel>): MutableList<LabelsModel> {
        val emsCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.emsBag.ru }.count()
        val strCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.strBag.ru }.count()
        val prvCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.prvBag.ru }.count()
        val korCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.korBag.ru }.count()
        val gazetaCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.gazeta.ru }.count()
        val kgpoCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.kgpo.ru }.count()
        val taraCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.taraBag.ru }.count()
        val rpoCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.rpo.ru }.count()
        val otherCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.otherBag.ru }.count()
        val rpoCarefullCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.rpoCarefull.ru }.count()
        val packetListCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.packetList.ru }.count()
        val rpoEconomCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.rpoEconom.ru }.count()
        val ppiBagCount = transfers.filter { itemModel -> itemModel.typeName == LabelsEnum.ppiBag.ru }.count()

        val list = mutableListOf(
            LabelsModel(LabelsEnum.emsBag.ru, emsCount, emsCount),
            LabelsModel(LabelsEnum.strBag.ru, strCount, strCount),
            LabelsModel(LabelsEnum.prvBag.ru, prvCount, prvCount),
            LabelsModel(LabelsEnum.korBag.ru, korCount, korCount),
            LabelsModel(LabelsEnum.gazeta.ru, gazetaCount, gazetaCount),
            LabelsModel(LabelsEnum.kgpo.ru, kgpoCount, kgpoCount),
            LabelsModel(LabelsEnum.taraBag.ru, taraCount, taraCount),
            LabelsModel(LabelsEnum.rpo.ru, rpoCount, rpoCount),
            LabelsModel(LabelsEnum.otherBag.ru, otherCount, otherCount),
            LabelsModel(LabelsEnum.rpoCarefull.ru, rpoCarefullCount, rpoCarefullCount),
            LabelsModel(LabelsEnum.packetList.ru, packetListCount, packetListCount),
            LabelsModel(LabelsEnum.rpoEconom.ru, rpoEconomCount, rpoEconomCount),
            LabelsModel(LabelsEnum.ppiBag.ru, ppiBagCount, ppiBagCount)
            )
        return list.filter { model -> model.plan != 0 }.toMutableList()
    }

    private fun transfersToLabels(transfers: MutableList<ItemModel>): Labels {
        return Labels(
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.emsBag.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.strBag.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.prvBag.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.korBag.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.gazeta.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.kgpo.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.taraBag.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.rpo.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.otherBag.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.rpoCarefull.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.packetList.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.rpoEconom.ru }.count(),
            transfers.filter { transfer -> transfer.typeName == LabelsEnum.ppiBag.ru }.count()
            )
    }

    private fun transfersToItems(transfers: MutableList<ItemModel>): MutableList<String> {
        val l = mutableListOf<String>()
        for (transfer in transfers) {
            l.add(transfer.shpi)
        }
        return l
    }

}