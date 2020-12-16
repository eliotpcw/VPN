package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_accept_start.*
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.LabelsEnum
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.utils.PreferenceHelper

class AcceptStartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_start)
        PreferenceHelper.saveStep(this, Step.ACCEPT_START)
        title = Step.ACCEPT_START.title

        val database = PreferenceHelper.getDB(this)
        val index = PreferenceHelper.getIndex(this)
        val stationName = database.road.filter { road -> road.index == index }[0].dept.nameRu
        astart_text.text = "Приём О-накладной со станции ".plus(stationName).plus(" закончен.")

        astart_button.setOnClickListener {
            val intent = Intent(this, AcceptScanActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
