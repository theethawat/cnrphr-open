package com.cnr.phr_android.dashboard.monitor.utility.entity

enum class RiskLevel(val label:String, val thaiLabel:String){
    SAFE("Normal","ปกติ"),
    WARNING("Warning","ควรระมัดระวัง"),
    MODERATE("Moderate Risk","เสี่ยงต่อการเป็นโรค"),
    MASSIVE("High Risk","เสี่ยงสํูงต่อการเป็นโรค"),
    DANGER("Danger","อันตราย หรือ เป็นโรคแล้ว"),
    SUBSTANDARD("Substandard","ต่ำกว่าเกณฑ์มาตรฐาน"),
    UNKNOWN("Unknown Data","ไม่สามารถวิเคราะห์ได้")
}