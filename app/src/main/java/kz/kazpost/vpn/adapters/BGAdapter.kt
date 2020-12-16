package kz.kazpost.vpn.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bg.view.*
import kz.kazpost.vpn.R
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.extensions.inflate

class BGAdapter(private val list: MutableList<ItemModel>): RecyclerView.Adapter<BGAdapter.BGViewHolder>()  {

    var tt = ""

    init {
        list.sortBy { model -> model.type }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BGViewHolder {
        val inflatedView = parent.inflate(R.layout.item_bg, false)
        return BGViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: BGViewHolder, position: Int) {
        val model = list[position]
        holder.setIsRecyclable(false)
        holder.bind(model)
    }
    override fun getItemCount(): Int = list.size

    inner class BGViewHolder(v: View): RecyclerView.ViewHolder(v)  {
        private var headerLabel: TextView = v.itemb_header
        private var shpiLabel: TextView = v.itemb_shpi
        private var actLabel: TextView = v.itemb_act
        fun bind(model: ItemModel) {
            if (tt != model.type) {
                headerLabel.visibility = View.VISIBLE
                headerLabel.text = model.typeName
                tt = model.type
            } else {
                headerLabel.visibility = View.GONE
            }
            shpiLabel.text = "ШПИ: ".plus(model.shpi)
            actLabel.text = "Акт: ".plus(if (model.hasAct) "Есть" else "Нет")
        }
    }

}