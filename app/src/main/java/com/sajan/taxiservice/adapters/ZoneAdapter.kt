package com.sajan.taxiservice.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sajan.taxiservice.R
import com.sajan.taxiservice.protocols.OnItemClickListener
import java.util.ArrayList

class ZoneAdapter(private val zoneArrayList: ArrayList<String>) :
    RecyclerView.Adapter<ZoneAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.row_title, parent, false)
    )

    override fun getItemCount(): Int {
        return zoneArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = zoneArrayList[position]
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#c4e3ed"))
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClicked(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }
}
