package kz.kazpost.vpn

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.honeywell.aidc.*
import kotlinx.android.synthetic.main.activity_transfer_scan2.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.adapters.ItemAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.extensions.toEditable
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.utils.PreferenceHelper

class TransferScan2Activity : AppCompatActivity(), BarcodeReader.BarcodeListener {

    private lateinit var database: StationData
    private lateinit var scans: MutableList<ItemModel>
    private lateinit var transfers: MutableList<ItemModel>
    private lateinit var lefts: MutableList<ItemModel>
    private var transfers2: MutableList<ItemModel> = mutableListOf()

    private var manager: AidcManager? = null
    private var reader: BarcodeReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_scan2)
        PreferenceHelper.saveStep(this, Step.TRANSFER_SCAN2)
        title = Step.TRANSFER_SCAN2.title
//        val success = MediaPlayer.create(this, R.raw.success)
//        success.setOnCompletionListener { success.pause() }
//        val error = MediaPlayer.create(this, R.raw.error)
//        error.setOnCompletionListener { error.pause() }

        database = PreferenceHelper.getDB(this)
        scans = PreferenceHelper.getScans(this) ?: mutableListOf()
        transfers = PreferenceHelper.getTransfers(this) ?: mutableListOf()
        lefts = PreferenceHelper.getLefts(this) ?: mutableListOf()

        val stationName = database.road.filter { road -> road.dept.name == database.nextDep }[0].dept.nameRu
        val itemAdapter = ItemAdapter(null)
        tscan2_station.text = "Для станции: ".plus(stationName)
        tscan2_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }
        itemAdapter.items = transfers2

        tscan2_added.text = "Просканировано: ".plus(transfers.count())
        tscan2_left.text = "Подлежат к сдаче: ".plus(lefts.count())
        tscan2_textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val leftItem = lefts.filter { left -> left.shpi == s.toString() }
                if (leftItem.count() > 0) {
                    if (leftItem[0].to == database.nextDep) {
                        if (!transfers.contains(leftItem[0])) {
                            transfers.add(leftItem[0])
                            transfers2.add(leftItem[0])
                            lefts.remove(leftItem[0])
                            tscan2_textInputEditText.text?.clear()
                            tscan2_recycler.adapter?.notifyDataSetChanged()
                            tscan2_added.text = "Просканировано: ".plus(transfers.count().toString())
                            tscan2_left.text = "Подлежат к сдаче: ".plus(lefts.count().toString())
                            //success.start()
                        }
                    }
                    savePreference()
                }
                val scanItem = scans.filter { scan -> scan.shpi == s.toString() }
                if (scanItem.count() > 0) {
                    tscan2_textInputEditText.text?.clear()
                    runRedToast("Данное ШПИ не относится к этой станции")
                    //error.start()
                }
            }
        })
        tscan2_end.setOnClickListener {
            if (lefts.count() != 0) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Внимание")
                builder.setMessage("Не все посылки были просканированы")
                builder.setCancelable(false)
                builder.setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
                builder.setPositiveButton("Продолжить") { dialog, _ ->
                    dialog.cancel()
                    val intent = Intent(this, TransferBGActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                val alert = builder.create()
                alert.show()
            } else {
                val intent = Intent(this, TransferBGActivity::class.java)
                startActivity(intent)
                finish()
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
            tscan2_textInputEditText.text = event.barcodeData.toEditable()
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
