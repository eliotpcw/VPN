package kz.kazpost.vpn.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.item_labels.view.*
import kz.kazpost.vpn.R
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.extensions.inflate

interface AcceptInvoiceAdapterInterface {
    fun onFactChanged(sum: Int, isSavable: Boolean)
}

class AcceptInvoiceAdapter(
    private val list: MutableList<LabelsModel>,
    private val delegate: AcceptInvoiceAdapterInterface
):
    RecyclerView.Adapter<AcceptInvoiceAdapter.LabelsViewHolder>()  {

    var isEdited = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelsViewHolder {
        val inflatedView = parent.inflate(R.layout.item_labels, false)
        for (i in 0..list.size) {
            isEdited.add(0)
        }
        return LabelsViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: LabelsViewHolder, position: Int) {
        val model = list[position]
        holder.bind(model)
        holder.factEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().toIntOrNull() ?: 0 <= list[position].plan) {
                    list[position].fact = s.toString().toIntOrNull() ?: 0
                    if (s.toString().toIntOrNull() == null) {
                        isEdited[position] = 0
                    } else {
                        isEdited[position] = 1
                    }
                } else {
                    list[position].fact = 0
                    holder.factEdit.text = null
                    isEdited[position] = 0
                }
                var sum = 0
                list.forEach {
                    sum += it.fact
                }
                var isE = 0
                isEdited.forEach {
                    isE += it
                }
                delegate.onFactChanged(sum, list.size == isE)
            }
        })
    }
    override fun getItemCount(): Int = list.size

    inner class LabelsViewHolder(v: View): RecyclerView.ViewHolder(v)  {
        private var nameLabel: TextView = v.iteml_name
        private var planLabel: TextView = v.iteml_plan
        var factEdit: TextInputEditText = v.iteml_fact

        fun bind(model: LabelsModel) {
            nameLabel.text = model.name
            planLabel.text = model.plan.toString()
            factEdit.text = null
            factEdit.clearFocus()
        }
    }
}

