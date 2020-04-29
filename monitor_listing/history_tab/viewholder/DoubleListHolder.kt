package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.cnr.phr_android.R

class DoubleListHolder(doubleListView: View) : RecyclerView.ViewHolder(doubleListView){
    val bgValue: TextView = doubleListView.findViewById(R.id.bg_value)
    val bgValue2:TextView = doubleListView.findViewById(R.id.bg_value2)
    val bgUnit:TextView = doubleListView.findViewById(R.id.bg_unit)
    val bgDate: TextView = doubleListView.findViewById(R.id.bg_date)
    val bgMonth:TextView = doubleListView.findViewById(R.id.bg_month)
    val bgYear:TextView = doubleListView.findViewById(R.id.bg_year)
    val bgHour:TextView = doubleListView.findViewById(R.id.bg_hour)
    val bgMin:TextView = doubleListView.findViewById(R.id.bg_min)
}