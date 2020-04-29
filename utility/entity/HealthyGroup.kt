package com.cnr.phr_android.dashboard.monitor.utility.entity

enum class HealthyGroup(val label:String,val thaiName:String){
    UNDERWEIGHT("Underweight","น้ำหนักต่ำกว่าเกณฑ์"),
    NORMAL("Normal","น้ำหนักปกติ"),
    OVERSIZE("Oversize","ภาวะน้ำหนักเกิน"),
    OBESITY("Obesity","ภาวะโรคอ้วนอันตราย")
}