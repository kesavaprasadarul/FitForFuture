package com.bosch.vaehiclefitness.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bosch.vaehiclefitness.R
import kotlinx.android.synthetic.main.item_certificate.view.*

class CertificateAdapter(private val dataMap: HashMap<String, String>) :
    RecyclerView.Adapter<CertificateAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_certificate, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataMap.keys.elementAt(position), dataMap.values.elementAt(position))
    }

    override fun getItemCount(): Int {
        return dataMap.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data1: String, data2: String) {
            itemView.tvTestName?.text = data1
            itemView.tvTestResult?.text = data2
        }
    }
}