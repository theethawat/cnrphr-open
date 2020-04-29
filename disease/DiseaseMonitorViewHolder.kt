package com.cnr.phr_android.dashboard.monitor.disease

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.cnr.phr_android.R

class DiseaseMonitorViewHolder (diseaseView: View):RecyclerView.ViewHolder(diseaseView){
    var diseaseImage:ImageView = diseaseView.findViewById(R.id.disease_image)
    val diseaseName:TextView = diseaseView.findViewById(R.id.title_disease)
    val diseaseDescribe:TextView = diseaseView.findViewById(R.id.disease_status_describe)
    var moreInfoButton:Button = diseaseView.findViewById(R.id.button_more_info)
    var diseaseRisk:TextView = diseaseView.findViewById(R.id.value_risk_level)
}