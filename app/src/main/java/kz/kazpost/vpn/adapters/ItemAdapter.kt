package kz.kazpost.vpn.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_items.view.*
import kz.kazpost.vpn.R
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.extensions.inflate
import kz.kazpost.vpn.utils.BaseAdapter
import kz.kazpost.vpn.utils.BaseViewHolder

interface OnItemClickListener{
    fun onItemClicked(model: ItemModel)
}

class ItemAdapter(
     private val listener: OnItemClickListener?
):  BaseAdapter<ItemModel, ItemAdapter.ItemsViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val inflatedView = parent.inflate(R.layout.item_items, false)
        return ItemsViewHolder(inflatedView)
    }

    inner class ItemsViewHolder(v: View): BaseViewHolder<ItemModel>(v)  {
        private var typeLabel: TextView = v.itemi_type
        private var shpiLabel: TextView = v.itemi_shpi
        private var fromLabel: TextView = v.itemi_from
        private var toLabel: TextView = v.itemi_to
        private var actLabel: TextView = v.itemi_act

        override fun bind(item: ItemModel) {
            typeLabel.text = item.typeName
            shpiLabel.text = item.shpi
            fromLabel.text = item.fromName
            toLabel.text = item.toName
            actLabel.text = if (item.hasAct) "Есть" else "Нет"

            itemView.setOnClickListener {
                listener?.onItemClicked(item)
            }
        }
    }

}