package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.cnr.phr_android.R

class SingleListHolder(singleListView: View) : RecyclerView.ViewHolder(singleListView){
    val bgValue: TextView = singleListView.findViewById(R.id.bg_value)
    val bgUnit:TextView = singleListView.findViewById(R.id.bg_unit)
    val bgDate: TextView = singleListView.findViewById(R.id.bg_date)
    val bgMonth:TextView = singleListView.findViewById(R.id.bg_month)
    val bgYear:TextView = singleListView.findViewById(R.id.bg_year)
    val bgHour:TextView = singleListView.findViewById(R.id.bg_hour)
    val bgMin:TextView = singleListView.findViewById(R.id.bg_min)
}