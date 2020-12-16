package kz.kazpost.vpn.acts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_acts30.*
import kotlinx.android.synthetic.main.activity_acts30.fromDepSpinner
import kotlinx.android.synthetic.main.activity_acts30.toDepSpinner
import kz.kazpost.vpn.R
import kz.kazpost.vpn.acts.models.ActModel
import kz.kazpost.vpn.extensions.toEditable
import kz.kazpost.vpn.utils.PreferenceHelper

class Acts30Activity : AppCompatActivity() {

    private lateinit var acts: MutableList<ActModel>
    private var typeRPO = "label"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acts30)

        val shpi = intent.getStringExtra("SHPI")
        acts = PreferenceHelper.getActs(this) ?: mutableListOf()

        if (shpi != null) {
            acts_30_shpi.text = shpi.toEditable()
            typeRPO = if (shpi[0] == 'G') "label" else "mail"

            acts_30_spinner1.adapter = ArrayAdapter.createFromResource(
                this,
                R.array.labels_30_reasons,
                R.layout.item_spin
            )
            acts_30_spinner2.adapter = ArrayAdapter.createFromResource(
                this,
                R.array.labels_violations,
                R.layout.item_spin
            )
        }

//        fromDepSpinner.adapter = ArrayAdapter(
//            this,
//            R.layout.item_spin,
//            PreferenceHelper.getRoads(this)!!
//        )
//
//        toDepSpinner.adapter = ArrayAdapter(
//            this,
//            R.layout.item_spin,
//            PreferenceHelper.getRoads(this)!!
//        )

        acts_30_create.setOnClickListener {
            val model = ActModel(
                typeRPO,
                "30",
                acts_30_shpi.text.toString(),
                (acts_30_spinner1.selectedItemPosition + 1).toString(),
                (acts_30_spinner2.selectedItemPosition + 1).toString(),
                (acts_30_spinner2.selectedItem).toString(),
                acts_30_comment.text.toString()
            )
            acts.add(model)
            PreferenceHelper.saveActs(this, acts)
            val intent = Intent(this, ActsEndActivity::class.java)
            intent.putExtra("TYPE", "30")
            intent.putExtra("SHPI", acts_30_shpi.text.toString())
            startActivityForResult(intent, 3)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 &&  data != null) {
            val shpi = data.getStringExtra("SHPI")
            val type = data.getStringExtra("TYPE")
            if (shpi != null) {
                val intent = Intent()
                intent.putExtra("SHPI", shpi)
                intent.putExtra("TYPE", type)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}