package kz.kazpost.vpn.acts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_acts_end.*
import kz.kazpost.vpn.R
import kz.kazpost.vpn.utils.PreferenceHelper

class ActsEndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acts_end)

        val shpi = intent.getStringExtra("SHPI")
        val type = intent.getStringExtra("TYPE")
        if (type == "51") {
            acts_end_text.text = "Акт успешно создан"
        } else {
            acts_end_text.text = "Извещение успешно создано"
        }
        acts_end_button.setOnClickListener {
            val intent = Intent()
            intent.putExtra("SHPI", shpi)
            intent.putExtra("TYPE", type)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}
