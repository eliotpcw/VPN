package kz.kazpost.vpn.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_tlabels.view.*
import kz.kazpost.vpn.R
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.extensions.inflate

class TransferInvoiceAdapter(private val list: MutableList<LabelsModel>):
    RecyclerView.Adapter<TransferInvoiceAdapter.LabelsViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelsViewHolder {
        val inflatedView = parent.inflate(R.layout.item_tlabels, false)
        return LabelsViewHolder(inflatedView)
    }
    
    override fun onBindViewHolder(holder: LabelsViewHolder, position: Int) {
        val model = list[position]
        holder.bind(model)
    }
    override fun getItemCount(): Int = list.size

    inner class LabelsViewHolder(v: View): RecyclerView.ViewHolder(v)  {
        private var nameLabel: TextView = v.itemt_name
        private var planLabel: TextView = v.itemt_plan
        private var factLabel: TextView = v.itemt_fact

        fun bind(model: LabelsModel) {
            nameLabel.text = model.name
            planLabel.text = model.plan.toString()
            factLabel.text = model.fact.toString()
        }
    }
}