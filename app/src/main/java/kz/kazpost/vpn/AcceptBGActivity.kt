package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_accept_b_g.*
import kz.kazpost.vpn.adapters.BGAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.utils.PreferenceHelper

class AcceptBGActivity : AppCompatActivity() {

    private lateinit var scans: MutableList<ItemModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_b_g)
        PreferenceHelper.saveStep(this, Step.ACCEPT_BG)
        title = Step.ACCEPT_BG.title

        scans = PreferenceHelper.getScans(this) ?: mutableListOf()

        abg_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BGAdapter(scans)
            recycledViewPool.setMaxRecycledViews(scans.count(), 0)
        }
        abg_count.text = "Всего в вагоне: ".plus(scans.count().toString())
        abg_accept.setOnClickListener {
            val intent = Intent(this, AcceptActsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
