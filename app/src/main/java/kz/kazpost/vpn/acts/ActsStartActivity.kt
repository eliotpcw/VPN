package kz.kazpost.vpn.acts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_acts_start.*
import kz.kazpost.vpn.R

class ActsStartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acts_start)
        val shpi = intent.getStringExtra("SHPI")

        acts_start_button1.setOnClickListener {
            val intent = Intent(this, Acts51Activity::class.java)
            intent.putExtra("SHPI", shpi)
            startActivityForResult(intent, 2)
        }
        acts_start_button2.setOnClickListener {
            val intent = Intent(this, Acts30Activity::class.java)
            intent.putExtra("SHPI", shpi)
            startActivityForResult(intent, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && data != null) {
            val shpi = data.getStringExtra("SHPI")
            val type = data.getStringExtra("TYPE")
            if (shpi != null) {
                val intent = Intent()
                intent.putExtra("TYPE", type)
                intent.putExtra("SHPI", shpi)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}
