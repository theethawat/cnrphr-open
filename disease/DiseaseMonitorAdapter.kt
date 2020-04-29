package com.cnr.phr_android.dashboard.monitor.disease

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel

class DiseaseMonitorAdapter : RecyclerView.Adapter<DiseaseMonitorViewHolder>(){
   var data = listOf<NCDSDisease>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: DiseaseMonitorViewHolder, position: Int) {
        val item = data[position]
        holder.diseaseName.text = item.diseaseName.thaiLabel
        holder.diseaseDescribe.text = item.describe
        holder.diseaseRisk.text = item.risk.thaiLabel
        if(item.risk == RiskLevel.MODERATE){
            holder.diseaseRisk.setTextColor(Color.parseColor("#FBC02D"))
        }
        else if(item.risk == RiskLevel.DANGER || item.risk == RiskLevel.MASSIVE){
            holder.diseaseRisk.setTextColor(Color.RED)
        }
        else if(item.risk == RiskLevel.SAFE){
            holder.diseaseRisk.setTextColor(Color.GREEN)
        }
//        holder.moreInfoButton.setOnClickListener {
//            TODO("Not Implement")
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseMonitorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_disease_value,parent,false)
        return DiseaseMonitorViewHolder(view)
    }
}