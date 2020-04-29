package com.cnr.phr_android.dashboard.monitor.utility

import com.cnr.phr_android.dashboard.monitor.utility.entity.HealthyGroup
import com.cnr.phr_android.dashboard.monitor.utility.entity.Sex
import timber.log.Timber
import java.util.*

class AppCalculation{
    fun calculateAge(bDay:Int,bMonth:Int,bYear:Int):Int{
        val currentDate = Date()
        val cDay = currentDate.day
        val cMonth = currentDate.month + 1
        val cYear = currentDate.year + 1900
        val age:Int
        age = if(cMonth > bMonth){
            cYear - bYear
        }
        else if(cMonth < bMonth){
            cYear - bYear - 1
        }
        else{
            if(cDay < bDay){
                cYear - bYear -1
            }
            else{
                cYear - bYear
            }
        }
        return age
    }

    fun calculateBMI(weight:Float,height:Float): HealthyGroup {
        val metreHeight = height / 100
        val bmi = weight / (metreHeight * metreHeight)
        return when {
            bmi < 18.5 -> {
                HealthyGroup.UNDERWEIGHT
            }
            bmi < 24.9 -> {
                HealthyGroup.NORMAL
            }
            bmi < 29.9 -> {
                HealthyGroup.OVERSIZE
            }
            else -> {
                HealthyGroup.OBESITY
            }
        }
    }

    fun transfromStringToSex(sexString:String):Sex{
        Timber.v("Gender String $sexString")
        return when (sexString) {
            "Male" -> Sex.MALE
            "Female" -> Sex.FEMALE
            else -> Sex.TBA
        }
    }
}