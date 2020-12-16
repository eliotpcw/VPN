package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.honeywell.aidc.*
import kotlinx.android.synthetic.main.activity_transfer_scan.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.adapters.ItemAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.extensions.toEditable
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.utils.PreferenceHelper

class TransferScanActivity :
    AppCompatActivity(),
    BarcodeReader.BarcodeListener {

    private lateinit var database: StationData
    private lateinit var scans: MutableList<ItemModel>
    private lateinit var transfers: MutableList<ItemModel>
    private lateinit var lefts: MutableList<ItemModel>
    private var manager: AidcManager? = null
    private var reader: BarcodeReader? = null

    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_scan)
        PreferenceHelper.saveStep(this, Step.TRANSFER_SCAN)
        title = Step.TRANSFER_SCAN.title
        supportActionBar?.subtitle = "для сдачи в пункте обмена"

        database = PreferenceHelper.getDB(this)
        transfers = PreferenceHelper.getTransfers(this) ?: mutableListOf()
        scans = PreferenceHelper.getScans(this) ?: mutableListOf()
        lefts = PreferenceHelper.getLefts(this) ?: mutableListOf()

        val stationName = database.road.filter { road -> road.dept.name == database.nextDep }[0].dept.nameRu
        tscan_station.text = "Для станции: ".plus(stationName)
        itemAdapter = ItemAdapter(null)
        itemAdapter.items = transfers
        tscan_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }
        val toTransfer = scans.filter { scan -> scan.to == database.nextDep }
        for (toTrans in toTransfer) {
            lefts.add(toTrans)
        }
        for (toTrans in toTransfer) {
            scans.remove(toTrans)
        }
        PreferenceHelper.saveLefts(this, lefts)
        PreferenceHelper.saveScans(this, scans)

        tscan_added.text = "Просканировано: ".plus(transfers.count())
        tscan_left.text = "Подлежат к сдаче: ".plus(lefts.count())

        new_transfer.setOnClickListener {
            itemAdapter.items = toTransfer
            transfers.addAll(lefts)
            lefts.clear()
            tscan_left.text = "Подлежат к сдаче: ".plus(lefts.count())
            savePreference()
            new_transfer.isEnabled = false
        }

        tscan_textInputEditText.doOnTextChanged { text, start, before, count ->
            val leftItem = lefts.filter { left -> left.shpi == text.toString() }
            if (leftItem.count() > 0) {
                if (leftItem[0].to == database.nextDep) {
                    if (!transfers.contains(leftItem[0])) {
                        transfers.add(leftItem[0])
                        lefts.remove(leftItem[0])
                        tscan_textInputEditText.text?.clear()
                        tscan_recycler.adapter?.notifyDataSetChanged()
                        tscan_added.text = "Просканировано: ".plus(transfers.count().toString())
                        tscan_left.text = "Подлежат к сдаче: ".plus(lefts.count().toString())
                        //success.start()
                    }
                }
                savePreference()
            }
            val scanItem = scans.filter { scan -> scan.shpi == text.toString() }
            if (scanItem.count() > 0) {
                tscan_textInputEditText.text?.clear()
                runRedToast("Данное ШПИ не относится к этой станции")
                //error.start()
            }
        }

        tscan_end.setOnClickListener {
            val intent = Intent(this, TransferScan2Activity::class.java)
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
        PreferenceHelper.saveLefts(this, lefts)
        PreferenceHelper.saveTransfers(this, transfers)
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
            tscan_textInputEditText.text = event.barcodeData.toEditable()
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
}
