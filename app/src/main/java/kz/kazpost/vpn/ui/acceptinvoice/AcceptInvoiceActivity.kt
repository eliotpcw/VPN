package kz.kazpost.vpn.ui.acceptinvoice

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_accept_invoice.*
import kz.kazpost.vpn.AcceptStartActivity
import kz.kazpost.vpn.R
import kz.kazpost.vpn.adapters.AcceptInvoiceAdapter
import kz.kazpost.vpn.adapters.AcceptInvoiceAdapterInterface
import kz.kazpost.vpn.enums.LabelsEnum
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.models.Labels
import kz.kazpost.vpn.models.RequestLabels
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.models.VPNData
import kz.kazpost.vpn.ui.Status
import kz.kazpost.vpn.utils.*
import kz.kazpost.vpn.utils.InternetHelper
import okhttp3.ResponseBody
import org.koin.android.viewmodel.ext.android.viewModel
import retrofit2.Response
import java.lang.ref.WeakReference

data class LoadDataModel(
    var tid: String,
    var index: Int
)

class AcceptInvoiceActivity
    : AppCompatActivity(),
    AcceptInvoiceAdapterInterface {

    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private var dialog: AlertDialog? = null

    private lateinit var database: StationData
    private lateinit var labels: MutableList<LabelsModel>

    private val viewModel by viewModel<AcceptInvoiceViewModel>()

    private val dialogView: View by lazy {
        LayoutInflater.from(this@AcceptInvoiceActivity).inflate(R.layout.progress, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_invoice)
        setSupportActionBar(toolbar)

        alertDialogBuilder = AlertDialog.Builder(this@AcceptInvoiceActivity).setView(dialogView)
        dialog = alertDialogBuilder
            .setMessage(R.string.alert_dialog_message)
            .setCancelable(false)
            .create()

        PreferenceHelper.saveStep(this, Step.ACCEPT_INVOICE)
        title = Step.ACCEPT_INVOICE.title
        PreferenceHelper.setAuth(this)

        initViewModel()

        ainvoice_accept.setOnClickListener {
            PreferenceHelper.saveDB(this, database)
            PreferenceHelper.saveLabels(this, labels)
            val sendData = RequestLabels(
                PreferenceHelper.getLogin(this),
                database.fromDep,
                database.transportListId,
                database.index,
                listToLabels(labels)
            )
            viewModel.verifyLabels(sendData)
        }
    }

    private fun initViewModel(){
        viewModel.getVpnData(PreferenceHelper.getLogin(this))

        viewModel.statusLiveData.observe(this, Observer { status ->
            when (status) {
                Status.SHOW_LOADING -> {
                    dialog?.show()
                }
                Status.HIDE_LOADING -> {
                    dialog?.dismiss()
                }
            }
        })

        viewModel.errorLiveData.observe(this, EventObserver { error ->
            toast(error)
        })

        viewModel.responseVpnData.observe(this, EventObserver {
            when(it.status){
                "Departed" ->{
                    ainvoice_recycler.visibility = View.GONE
                    status_tv.visibility = View.VISIBLE
                    status_tv.text = getString(R.string.departed)
                }
                "Arrived" ->{
                    ainvoice_recycler.visibility = View.VISIBLE
                    status_tv.visibility = View.GONE
                    PreferenceHelper.saveIndex(this, it.index!!)
                    viewModel.getDataOfStation(
                        it.transportListId,
                        it.index
                        )

                    ainvoice_accept.isEnabled = true
                }
                "Archived" -> {
                    ainvoice_recycler.visibility = View.GONE
                    status_tv.visibility = View.VISIBLE
                    status_tv.text = getString(R.string.archived)
                }
            }
        })

        viewModel.responseStationData.observe(this, EventObserver {
            ainvoice_station.text = it.road.filter { road ->
                road.dept.name == it.fromDep
            }[0].dept.nameRu
            database = it
            labels = labelsToList(it.labels)
            ainvoice_recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = AcceptInvoiceAdapter(labels, this@AcceptInvoiceActivity)
            }

            PreferenceHelper.saveRoads(this, it.road)
            ainvoice_labels_bottom_plan.text = it.items?.size.toString()
            ainvoice_labels_bottom_fact.text = "0"
            toast("Данные загружены  успешно!")
        })

        viewModel.responseSendLabels.observe(this, EventObserver {
            if (it) {
                val intent = Intent(
                    this@AcceptInvoiceActivity,
                    AcceptStartActivity::class.java
                )
                startActivity(intent)
                finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_accept_invoice, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return if (id == R.id.action_refresh) {
            viewModel.getVpnData(PreferenceHelper.getLogin(this))
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun labelsToList(labels: Labels): MutableList<LabelsModel> {
        val list = mutableListOf(
            LabelsModel(LabelsEnum.emsBag.ru, labels.emsBag, 0),
            LabelsModel(LabelsEnum.strBag.ru, labels.strBag, 0),
            LabelsModel(LabelsEnum.prvBag.ru, labels.prvBag, 0),
            LabelsModel(LabelsEnum.korBag.ru, labels.korBag, 0),
            LabelsModel(LabelsEnum.gazeta.ru, labels.gazeta, 0),
            LabelsModel(LabelsEnum.kgpo.ru, labels.kgpo, 0),
            LabelsModel(LabelsEnum.taraBag.ru, labels.taraBag, 0),
            LabelsModel(LabelsEnum.rpo.ru, labels.rpo, 0),
            LabelsModel(LabelsEnum.otherBag.ru, labels.otherBag, 0),
            LabelsModel(LabelsEnum.packetList.ru, labels.packetList, 0),
            LabelsModel(LabelsEnum.rpoCarefull.ru, labels.rpoCarefull, 0),
            LabelsModel(LabelsEnum.rpoEconom.ru, labels.rpoEconom, 0),
            LabelsModel(LabelsEnum.ppiBag.ru, labels.ppiBag, 0)
            )
        return list.filter { model -> model.plan != 0 }.toMutableList()
    }

    private fun listToLabels(labels: MutableList<LabelsModel>): Labels {
        return Labels(
            if (labels.filter { label -> LabelsEnum.emsBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.emsBag.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.strBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.strBag.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.prvBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.prvBag.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.korBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.korBag.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.gazeta.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.gazeta.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.kgpo.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.kgpo.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.taraBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.taraBag.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.rpo.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.rpo.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.otherBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.otherBag.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.rpoCarefull.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.rpoCarefull.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.packetList.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.packetList.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.rpoEconom.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.rpoEconom.ru == label.name }[0].fact
            },
            if (labels.filter { label -> LabelsEnum.ppiBag.ru == label.name }.count() == 0) { 0
            } else { labels.filter { label -> LabelsEnum.ppiBag.ru == label.name }[0].fact
            }
        )
    }

    override fun onFactChanged(sum: Int, isSavable: Boolean) {
        ainvoice_labels_bottom_fact.text = "$sum"
        ainvoice_accept.isEnabled = isSavable
    }
}