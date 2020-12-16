package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_accept_end.*
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.utils.PreferenceHelper

class AcceptEndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_end)
        PreferenceHelper.saveStep(this, Step.ACCEPT_END)
        title = Step.ACCEPT_END.title

        aend_button.setOnClickListener {
            val intent = Intent(this, TransferScanActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
