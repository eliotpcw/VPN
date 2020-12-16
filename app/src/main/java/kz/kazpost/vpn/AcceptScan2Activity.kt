package kz.kazpost.vpn

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.honeywell.aidc.*
import kotlinx.android.synthetic.main.activity_accept_scan2.*
import kotlinx.android.synthetic.main.toast_green.view.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.acts.ActsStartActivity
import kz.kazpost.vpn.acts.models.ActModel
import kz.kazpost.vpn.adapters.ItemAdapter
import kz.kazpost.vpn.adapters.OnItemClickListener
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.LabelsEnum
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.extensions.toEditable
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.utils.PreferenceHelper
import kz.kazpost.vpn.utils.toast

class AcceptScan2Activity : AppCompatActivity(), BarcodeReader.BarcodeListener,
    OnItemClickListener {

    private lateinit var database: StationData
    private lateinit var labels: MutableList<LabelsModel>
    private lateinit var scans: MutableList<ItemModel>
    private lateinit var scans2: MutableList<ItemModel>
    private lateinit var items: MutableList<ItemModel>
    private var manager: AidcManager? = null
    private var reader: BarcodeReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_scan2)
        PreferenceHelper.saveStep(this, Step.ACCEPT_SCAN2)
        title = Step.ACCEPT_SCAN2.title

        database = PreferenceHelper.getDB(this)
        labels = PreferenceHelper.getLabels(this)
        scans = PreferenceHelper.getScans(this) ?: mutableListOf()
        scans2 = PreferenceHelper.getScans2(this) ?: mutableListOf()
        items = PreferenceHelper.getItems(this) ?: mutableListOf()

        ascan2_recycler.layoutManager = LinearLayoutManager(this)
        val itemAdapter = ItemAdapter(this)
        ascan2_recycler.adapter = itemAdapter
        itemAdapter.items = scans2

        ascan2_act.isEnabled = false
        ascan2_inn.text = "В вагоне: ".plus(scans.count().toString())
        ascan2_added.text = "Просканировано: ".plus(scans2.count().toString())

        ascan2_textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (database.items!!.contains(s.toString())) {
                    lateinit var model: ItemModel
                    lateinit var name: String
                    for (labelItem in database.labelItems!!) {
                        if (labelItem.labelListId == s.toString()) {
                            val typeName = LabelsEnum.valueOf(labelItem.labelType).ru
                            val fromName = database.road.filter {
                                    road -> road.dept.name == labelItem.fromDepartment
                            }[0].dept.nameRu
                            val toName = database.road.filter {
                                    road -> road.dept.name == labelItem.toDepartment
                            }[0].dept.nameRu
                            model = ItemModel(
                                labelItem.labelType,
                                typeName,
                                labelItem.labelListId,
                                labelItem.fromDepartment,
                                labelItem.toDepartment,
                                fromName,
                                toName,
                                labelItem.hasAct
                            )
                            name = typeName
                        }
                    }
                    for (mailItem in database.mailItems!!) {
                        if (mailItem.mailId == s.toString()) {
                            val typeName = LabelsEnum.valueOf(mailItem.mailType).ru
                            val fromName = database.road.filter { road ->
                                road.dept.name == mailItem.fromDepartment
                            }[0].dept.nameRu
                            val toName = database.road.filter { road ->
                                road.dept.name == mailItem.toDepartment
                            }[0].dept.nameRu
                            model = ItemModel(
                                mailItem.mailType,
                                typeName,
                                mailItem.mailId,
                                mailItem.fromDepartment,
                                mailItem.toDepartment,
                                fromName,
                                toName,
                                mailItem.hasAct
                            )
                            name = typeName
                        }
                    }
                    if (!scans2.contains(model)) {
                        scans.add(model)
                        scans2.add(model)
                        items.add(model)
                        labels.filter { label -> label.name == name }[0].fact--
                        ascan2_textInputEditText.text?.clear()
                        ascan2_recycler.adapter?.notifyDataSetChanged()
                        ascan2_added.text = "Просканировано: ".plus(scans2.count().toString())
                        ascan2_inn.text = "В вагоне: ".plus(scans.count().toString())
                        //success.start()
                    } else {
                        ascan2_textInputEditText.text?.clear()
                        runRedToast("Данное ШПИ уже просканировано")
                        //error.start()
                    }
                    savePreference()
                } else {

                    toast("Нет привязки к данному транспорту")

                    if (s.toString().length > 13) {
                        ascan2_act.isEnabled = true
                        //error.start()
                    }
                }
            }
        })

        ascan2_act.setOnClickListener {
            val intent = Intent(this, ActsStartActivity::class.java)
            intent.putExtra("SHPI", ascan2_textInputEditText.text.toString())
            startActivityForResult(intent, 1)
        }

        ascan2_end.setOnClickListener {
            if (scans2.count() == database.items?.count()) {
                PreferenceHelper.saveScans2(this, mutableListOf())
                val intent = Intent(this, AcceptBGActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val count = database.items!!.count() - scans2.count()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Внимание")
                builder.setMessage("Недостача в количестве $count. Сформировать на них акты автоматически?")
                builder.setCancelable(false)
                builder.setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
                builder.setPositiveButton("Продолжить") { dialog, _ ->
                    dialog.cancel()
                    val acts = PreferenceHelper.getActs(this) ?: mutableListOf()
                    for (i in 0 until database.items!!.count()) {
                        val s = scans2.filter { model -> model.shpi == database.items!![i] }
                        if (s.isEmpty()) {
                            val model = if (database.items!![i][0] == 'G') {
                                ActModel(
                                    "label",
                                    "51",
                                    database.items!![i],
                                    "1",
                                    "1",
                                    "Целое",
                                    ""
                                )
                            } else {
                                ActModel(
                                    "mail",
                                    "51",
                                    database.items!![i],
                                    "1",
                                    "1",
                                    "Целое",
                                    ""
                                )
                            }
                            acts.add(model)
                        }
                    }
                    PreferenceHelper.saveActs(this, acts)
                    PreferenceHelper.saveScans2(this, mutableListOf())
                    val intent = Intent(this, AcceptBGActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                val alert = builder.create()
                alert.show()
            }
        }
        AidcManager.create(this) { aidcManager ->
            manager = aidcManager
            reader = manager?.createBarcodeReader()
            try {
                reader?.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, false)
                reader?.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true)
                reader?.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                )
            } catch (e: UnsupportedPropertyException) {
                runRedToast("Ошибка в настроках сканера")
            }
            reader?.addBarcodeListener(this)
        }
    }

    private fun savePreference() {
        PreferenceHelper.saveScans(this, scans)
        PreferenceHelper.saveScans2(this, scans2)
        PreferenceHelper.saveItems(this, items)
        PreferenceHelper.saveLabels(this, labels)
    }

    override fun onItemClicked(model: ItemModel) {
        val intent = Intent(this, ActsStartActivity::class.java)
        intent.putExtra("SHPI", model.shpi)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null) {
            val shpi = data.getStringExtra("SHPI")
            val type = data.getStringExtra("TYPE")
            if (shpi != null) {
                for (i in 0 until scans.size) {
                    if (scans[i].shpi == shpi) {
                        scans[i].hasAct = true
                    }
                }
                for (i in 0 until scans2.size) {
                    if (scans2[i].shpi == shpi) {
                        scans2[i].hasAct = true
                    }
                }
                ascan2_act.isEnabled = false
                ascan2_textInputEditText.text?.clear()
                ascan2_recycler.adapter?.notifyDataSetChanged()
                PreferenceHelper.saveScans(this, scans)
                PreferenceHelper.saveScans2(this, scans2)
                if (type != null) {
                    if (type == "51") {
                        runGreenToast("Акт создан")
                    } else {
                        runGreenToast("Извещение создано")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (reader != null) {
            try {
                reader?.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
                runRedToast("Сканер недоступен")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (reader != null) {
            reader?.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (reader != null) {
            reader?.removeBarcodeListener(this)
            reader?.close()
        }
        if (manager != null) {
            manager?.close()
        }
    }

    override fun onBarcodeEvent(event: BarcodeReadEvent) {
        runOnUiThread {
            ascan2_textInputEditText.text = event.barcodeData.toEditable()
        }
    }

    override fun onFailureEvent(event: BarcodeFailureEvent) {
        runOnUiThread {
            runRedToast("Ошибка при чтении Баркода")
        }
    }

    private fun runRedToast(text: String) {
        val toast = Toast(this)
        toast.duration = Toast.LENGTH_LONG
        val v = layoutInflater.inflate(R.layout.toast_red, null)
        v.red_text.text = text
        toast.view = v
        toast.show()
    }

    private fun runGreenToast(text: String) {
        val toast = Toast(this)
        toast.duration = Toast.LENGTH_LONG
        val v = layoutInflater.inflate(R.layout.toast_green, null)
        v.green_text.text = text
        toast.view = v
        toast.show()
    }

}