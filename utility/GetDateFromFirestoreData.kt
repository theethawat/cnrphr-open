package com.cnr.phr_android.dashboard.monitor.utility

import timber.log.Timber
import java.util.*

//Mon Nov 11 20:30:15 GMT+07:00 2019
class GetDateFromFirestoreData(dateString:String) {

    private val date = dateString

    private var month:Int =1
    private var day:Int = 0
    private var hour:Int = 0
    private var min:Int = 0
    private var year:Int = 120

    init {
//        Timber.v("Date from String Calculation")
//        Timber.v("Date Length : ${date.length}")
//        Timber.v("Date $date")
        if(date != "null" && date.length >= 34){
           month = getMonthFromName(date.subSequence(4,7))
           Timber.v(date.length.toString())
           day = date.subSequence(8,10).toString().toInt()
           hour = date.subSequence(11,13).toString().toInt()
           min=date.subSequence(14,16).toString().toInt()
           year = date.subSequence(30,34).toString().toInt() - 1900
       }
    }



    fun getDate(): Date {
        return Date(year, month, day, hour, min)
    }

    private fun getMonthFromName(name:CharSequence):Int{
        //Timber.v("Month Name Get $name")
        return when(name){
            "Jan" -> 0
            "Feb" -> 1
            "Mar" -> 2
            "Apr" -> 3
            "May" -> 4
            "Jun" -> 5
            "Jul" -> 6
            "Aug" -> 7
            "Sep" -> 8
            "Oct" -> 9
            "Nov" -> 10
            "Dec" -> 11
            else -> 0 //MAGIC CODE
        }
    }
}
