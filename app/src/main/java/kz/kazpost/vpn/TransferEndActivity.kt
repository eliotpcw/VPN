package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_transfer_end.*
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.ui.acceptinvoice.AcceptInvoiceActivity
import kz.kazpost.vpn.utils.PreferenceHelper

class TransferEndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_end)
        PreferenceHelper.saveStep(this, Step.TRANSFER_END)
        title = Step.TRANSFER_END.title

        val database = PreferenceHelper.getDB(this)
        val scans = PreferenceHelper.getScans(this) ?: mutableListOf()
        val stationName = database.road.filter { road -> road.dept.name == database.nextDep }[0].dept.nameRu

        if (PreferenceHelper.getIndex(this)+1 >= database.road.count()) {
            tend_text.text = "Посылки переданы станции: ".plus(stationName).plus(". Это последняя станция.")
            tend_button.text = "Закончить маршрут"
        } else {
            tend_text.text = "Посылки переданы станции: ".plus(stationName).plus(".")
            tend_button.text = "Подтвердить"
        }
        tend_count.text = "В вагоне: ".plus(scans.count())
        tend_button.setOnClickListener {
            if (PreferenceHelper.getIndex(this)+1 >= database.road.count()) {
                PreferenceHelper.clearPreference(this)
                finish()
            } else {
                PreferenceHelper.saveIndex(this, PreferenceHelper.getIndex(this) + 1)
                val intent = Intent(this, AcceptInvoiceActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
