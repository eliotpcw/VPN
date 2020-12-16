package kz.kazpost.vpn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_transfer_b_g.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.adapters.BGAdapter
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.utils.InternetHelper
import kz.kazpost.vpn.utils.PreferenceHelper

class TransferBGActivity : AppCompatActivity(), InternetHelper.Consumer  {

    private lateinit var transfers: MutableList<ItemModel>
    private val taskType = "check"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_b_g)
        PreferenceHelper.saveStep(this, Step.TRANSFER_BG)
        title = Step.TRANSFER_BG.title
        supportActionBar?.subtitle = "и емкостей для сдачи"

        transfers = PreferenceHelper.getTransfers(this) ?: mutableListOf()

        tbg_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BGAdapter(transfers)
        }
        tbg_count.text = "Всего на передачу: ".plus(transfers.count().toString())
        tbg_accept.setOnClickListener {
            InternetHelper(this, taskType)
        }
    }

    override fun accept(internet: Boolean, type: String) {
        if (internet) {
            val intent = Intent(this, TransferInvoiceActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val toast = Toast(this)
            toast.duration = Toast.LENGTH_LONG
            val v = layoutInflater.inflate(R.layout.toast_red, null)
            v.red_text.text = "Нет интернета"
            toast.view = v
            toast.show()
        }
    }
}
