package kz.kazpost.vpn.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_acts.view.*
import kz.kazpost.vpn.R
import kz.kazpost.vpn.acts.models.ActModel
import kz.kazpost.vpn.extensions.inflate

class ActAdapter(
    private val context: Context,
    private val list: MutableList<ActModel>
): RecyclerView.Adapter<ActAdapter.ActViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActViewHolder {
        val inflatedView = parent.inflate(R.layout.item_acts, false)
        return ActViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ActViewHolder, position: Int) {
        val model = list[position]
        holder.bind(model)
    }
    override fun getItemCount(): Int = list.size

    inner class ActViewHolder(v: View): RecyclerView.ViewHolder(v)  {
        private var shpiLabel: TextView = v.itema_shpi
        private var typeLabel: TextView = v.itema_type
        private var reasonLabel: TextView = v.itema_reason
        private var violationLabel: TextView = v.itema_violations
        private var commentLabel: TextView = v.itema_comment
        fun bind(model: ActModel) {
            shpiLabel.text = "ШПИ: ".plus(model.shpi)
            typeLabel.text = "Тип: ".plus(if (model.typeAct == "51") "Акт" else "Извещение")
            val reason = when(model.typeAct){
                "51" -> context.resources.getStringArray(R.array.labels_51_reasons)[model.reasonId.toInt() - 1]
                else -> context.resources.getStringArray(R.array.labels_30_reasons)[model.reasonId.toInt() - 1]
            }
            reasonLabel.text = "Причина: ".plus(reason)
            violationLabel.text = "Состояние: ".plus(model.violationName)
            commentLabel.text = model.content
        }
    }
}