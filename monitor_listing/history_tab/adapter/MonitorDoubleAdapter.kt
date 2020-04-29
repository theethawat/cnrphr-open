package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.DataUnit
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.ThaiMonth
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.viewholder.DoubleListHolder
import com.cnr.phr_android.data.entity.*

/**
 * Create by theethawat@cnr - 2020-01-20
 * */
 class MonitorDoubleAdapter(inputDataType: VitalsignDataType) : RecyclerView.Adapter<DoubleListHolder>(){
    private var requestDataType = inputDataType
    var healthData = listOf<DeviceRoomData>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return healthData.size
    }

    override fun onBindViewHolder(holder: DoubleListHolder, position: Int) {
        val item = healthData[position]
        holder.bgDate.text = item.measurementTime.date.toString()
        holder.bgUnit.text = getUnit()
        holder.bgValue.text = getValue1(item).toString()
        holder.bgMonth.text = ThaiMonth[(item.measurementTime.month)]
        holder.bgYear.text = item.measurementTime.year.plus(1900).toString()
        holder.bgHour.text = item.measurementTime.hours.toString()
        holder.bgMin.text = getMiniute(item.measurementTime.minutes.toString())
        holder.bgValue2.text = getValue2(item).toString()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoubleListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_double_value,parent,false)
        return DoubleListHolder(view)
    }

    private fun getUnit():String{
        return when(this.requestDataType) {
            VitalsignDataType.BLOOD_PRESSURE -> DataUnit.MM_HG.label
            else -> "No Unit"
        }
    }

    private fun getValue1(item:DeviceRoomData): Int? {
        when (requestDataType) {
            VitalsignDataType.BLOOD_PRESSURE -> {
                //TODO("Change to Double Value Display Item")
                return (item as BloodPressure).systolic?.toInt()
            }
            else -> {
                throw IllegalArgumentException("No Supported Value")
            }
        }
    }
    private fun getValue2(item:DeviceRoomData): Int? {
        when (requestDataType) {
            VitalsignDataType.BLOOD_PRESSURE -> {
                //TODO("Change to Double Value Display Item")
                return (item as BloodPressure).diastolic?.toInt()
            }
            else -> {
                throw IllegalArgumentException("No Supported Value")
            }
        }
    }
}