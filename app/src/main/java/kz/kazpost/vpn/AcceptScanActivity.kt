package kz.kazpost.vpn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.honeywell.aidc.*
import kotlinx.android.synthetic.main.activity_accept_scan.*
import kotlinx.android.synthetic.main.toast_green.view.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.acts.ActsStartActivity
import kz.kazpost.vpn.adapters.ItemAdapter
import kz.kazpost.vpn.adapters.OnItemClickListener
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.LabelsEnum
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.utils.PreferenceHelper
import kz.kazpost.vpn.utils.toast

class AcceptScanActivity
    : AppCompatActivity(),
    BarcodeReader.BarcodeListener,
    OnItemClickListener {
    
    private lateinit var database: StationData
    private lateinit var labels: MutableList<LabelsModel>
    private lateinit var scans: MutableList<ItemModel>
    private lateinit var scans2: MutableList<ItemModel>
    private var items = mutableListOf<ItemModel>()
    private var manager: AidcManager? = null
    private var reader: BarcodeReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_scan)
        PreferenceHelper.saveStep(this, Step.ACCEPT_SCAN)
        title = Step.ACCEPT_SCAN.title

        database = PreferenceHelper.getDB(this)
        labels = PreferenceHelper.getLabels(this)
        scans = PreferenceHelper.getScans(this) ?: mutableListOf()
        scans2 = PreferenceHelper.getScans2(this) ?: mutableListOf()

        val itemAdapter = ItemAdapter(this)
        ascan_recycler.layoutManager = LinearLayoutManager(this)
        ascan_recycler.adapter = itemAdapter
        itemAdapter.items = scans2

        ascan_act.isEnabled = false
        ascan_inn.text = "В вагоне: ".plus(scans.count().toString())
        ascan_added.text = "Просканировано: ".plus(scans2.count().toString())
        ascan_textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (database.items!!.contains(s.toString())) {
                    lateinit var model: ItemModel
                    lateinit var name: String
                    for (labelItem in database.labelItems!!) {
                        if (labelItem.labelListId == s.toString()) {
                            val typeName = LabelsEnum.valueOf(labelItem.labelType).ru
                            val fromName = database.road.filter { road ->
                                road.dept.name == labelItem.fromDepartment
                            }[0].dept.nameRu
                            val toName = database.road.filter { road ->
                                road.dept.name == labelItem.toDepartment
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
                        items.add(model)
                        scans2.add(model)
                        labels.filter { label -> label.name == name }[0].fact--
                        ascan_textInputEditText.text?.clear()
                        ascan_recycler.adapter?.notifyDataSetChanged()
                        ascan_inn.text = "В вагоне: ".plus(scans.count().toString())
                        ascan_added.text = "Просканировано: ".plus(scans2.count().toString())
                        //success.start()
                    } else {
                        ascan_textInputEditText.text?.clear()
                        runRedToast("Данное ШПИ уже просканировано")
                        //error.start()
                    }
                    savePreference()
                } else {

                    toast("Нет привязки к данному транспорту")

                    if (s.toString().length > 13) {
                        ascan_act.isEnabled = true
                        //error.start()
                    }
                }
            }
        })

        ascan_act.setOnClickListener {
            val intent = Intent(this, ActsStartActivity::class.java)
            intent.putExtra("SHPI", ascan_textInputEditText.text.toString())
            startActivityForResult(intent, 1)
        }

        ascan_end.setOnClickListener {
            val intent = Intent(this, AcceptScan2Activity::class.java)
            startActivity(intent)
            finish()
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
                ascan_act.isEnabled = false
                ascan_textInputEditText.text?.clear()
                ascan_recycler.adapter?.notifyDataSetChanged()
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
            toast(event.barcodeData)
//            ascan_textInputEditText.text?.clear()
//            ascan_textInputEditText.text = event.barcodeData.toEditable()
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
