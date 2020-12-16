package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_accept_acts.*
import kz.kazpost.vpn.acts.models.ActModel
import kz.kazpost.vpn.adapters.ActAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.utils.PreferenceHelper

class AcceptActsActivity : AppCompatActivity() {

    private lateinit var acts: MutableList<ActModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_acts)
        PreferenceHelper.saveStep(this, Step.ACCEPT_ACT)
        title = Step.ACCEPT_ACT.title

        acts = PreferenceHelper.getActs(this) ?: mutableListOf()

        println("#### acts size ${acts.size}")

        aacts_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ActAdapter(context, acts)
        }
        aacts_count.text = "Всего актов: ".plus(acts.count().toString())
        aacts_accept.setOnClickListener {
            val intent = Intent(this, AcceptEndActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
