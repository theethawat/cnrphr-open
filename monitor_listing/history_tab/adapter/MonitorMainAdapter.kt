package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.DataUnit
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.ThaiMonth
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.viewholder.SingleListHolder
import com.cnr.phr_android.data.entity.*

/**
 * Create by theethawat@cnr - 2020-11-01
 * */
 class MonitorMainAdapter(inputDataType: VitalsignDataType) : RecyclerView.Adapter<SingleListHolder>(){
    private var requestDataType = inputDataType
    var healthData = listOf<DeviceRoomData>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return healthData.size
    }

    override fun onBindViewHolder(holder: SingleListHolder, position: Int) {
        val item = healthData[position]
        holder.bgDate.text = item.measurementTime.date.toString()
        holder.bgUnit.text = getUnit()
        holder.bgValue.text = getValue(item).toString()
        holder.bgMonth.text = ThaiMonth[(item.measurementTime.month)]
        holder.bgYear.text = item.measurementTime.year.plus(1900).toString()
        holder.bgHour.text = item.measurementTime.hours.toString()
        holder.bgMin.text = getMiniute(item.measurementTime.minutes.toString())
    }

    private fun getMiniute(string: String):String{
        return when (string.length) {
            2 -> {
                string
            }
            1 -> {
                "0$string"
            }
            else -> {
                "00"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_single_value,parent,false)
        return SingleListHolder(view)
    }

    private fun getUnit():String{
        return when(this.requestDataType) {
            VitalsignDataType.BLOOD_GLUCOSE -> DataUnit.KG_PER_L.label
            VitalsignDataType.BLOOD_PRESSURE -> DataUnit.MM_HG.label
            VitalsignDataType.SPO2->DataUnit.PERCENT_OF_OXYGEN_IN_HB.label
            VitalsignDataType.HEART_RATE -> DataUnit.BPM.label
            else -> "No Unit"
        }
    }

    private fun getValue(item:DeviceRoomData): Int? {
        when (requestDataType) {
            VitalsignDataType.SPO2 -> {
                return (item as Spo2).pulseOximeter
            }
            VitalsignDataType.HEART_RATE -> {
                return (item as HeartRate).value
            }
            VitalsignDataType.BLOOD_GLUCOSE -> {
                return  (item as BloodGlucose).value?.toInt()
            }
            VitalsignDataType.BLOOD_PRESSURE -> {
                //TODO("Change to Double Value Display Item")
                return (item as BloodPressure).mean?.toInt()
            }
            else -> {
                throw IllegalArgumentException("No Supported Value") as Throwable
            }
        }
    }
}