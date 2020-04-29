package com.cnr.phr_android.dashboard.monitor.utility.entity

enum class NCDS (val short:String,val label:String,val thaiLabel:String){
    DIABETES("diabetes","Diabetes","เบาหวาน"),
    HYPERTENSION("hypertension","Hypertension","ความดันโลหิตสูง"),
    HYPOXIA("hypoxia","Hypoxia","ภาวะพร่องออกซิเจนในเลือด"),
    STROKE("stroke","Celebrovascular Diseases","โรคหลอดเลือดในสมอง"),
    CORONARY("coronary","Cardiovascular Disease","โรคหลอดเลือดหัวใจ"),
    TBA("tba","Not Found","ยังไม่พบ")
}