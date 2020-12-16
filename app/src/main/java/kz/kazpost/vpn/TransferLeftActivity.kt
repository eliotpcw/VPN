package kz.kazpost.vpn

import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_transfer_left.*
import kotlinx.android.synthetic.main.progress.view.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.adapters.TransferInvoiceAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.LabelsEnum
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.models.Labels
import kz.kazpost.vpn.models.RequestItems
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.utils.HTTPHelper
import kz.kazpost.vpn.utils.InternetHelper
import kz.kazpost.vpn.utils.PreferenceHelper
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.ref.WeakReference

class TransferLeftActivity : AppCompatActivity(), InternetHelper.Consumer {

    private lateinit var dialog: AlertDialog
    private lateinit var database: StationData
    private lateinit var scans: MutableList<ItemModel>
    private var llabels: MutableList<LabelsModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_left)

        database = PreferenceHelper.getDB(this)
        scans = PreferenceHelper.getScans(this) ?: mutableListOf()
        llabels = transfersToLabelsList(scans)

        tleft_station.text = "Станция: ".plus(database.road.filter{ road -> road.dept.name == database.nextDep }[0].dept.nameRu)
        tleft_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransferInvoiceAdapter(llabels)
        }
        tleft_labels_bottom_plan.text = factCount(llabels)
        tleft_labels_bottom_fact.text = factCount(llabels)

        tleft_accept.setOnClickListener {
            InternetHelper(this, "")
        }

    }

    override fun accept(internet: Boolean, type: String) {
        if (internet) {
            val sendData = RequestItems(PreferenceHelper.getLogin(this),
                database.nextDep, database.transportListId, database.index+1, transfersToLabels(scans), transfersToItems(scans))
            TransferDataAsync(this).execute(sendData)
        } else {
            val toast = Toast(this)
            toast.duration = Toast.LENGTH_LONG
            val v = layoutInflater.inflate(R.layout.toast_red, null)
            v.red_text.text = "Нет интернета"
            toast.view = v
            toast.show()
        }
    }

    companion object {

        class TransferDataAsync internal constructor(context: TransferLeftActivity): AsyncTask<RequestItems, Void, Response<ResponseBody>>() {
            private val activityReference: WeakReference<TransferLeftActivity> = WeakReference(context)
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
                    view.progress_text.text = "Отправка недостач..."
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
                        val intent = Intent(context, TransferEndActivity::class.java)
                        context.startActivity(intent)
                        context.finish()
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
