package com.cnr.phr_android.dashboard.monitor.vitalsign_analyser

import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel

/**
 * Created by theethawat@cnr - 2020-03-24
 * Work from home due to novel Corona virus 2019 Outbreak - COVID-19
 * sb is short for Systolic Blood Pressure
 * db is short for Diastolic Blood Pressure
 *    Reference from The Heart Association of Thailand under the royal patronage in 2019 Guildlines
 *  http://www.thaiheart.org/images/column_1563846428/Thai%20HT%20Guideline%202019.pdf
 * */

enum class HypertensionLevel (val levelName:String,val thaiName:String,val sbMin:Int,val sbMax:Int,val dbMin:Int,val dbMax:Int,val adviceRef:String,val riskLevel: RiskLevel){
    OPTIMAL("Optimal","ระดับความดันโลหิตเหมาะสม",0,119,0,79,"advice_a",RiskLevel.SAFE),
    NORMAL("Normal","ระดับความดันโลหิตปกติ",120,129,80,8,"advice_b",RiskLevel.SAFE),
    HIGH_NORMAL("High Normal","ระดับความดันโลหิตในเกณฑ์เกือบสูง",130,139,85,89,"advice_c",RiskLevel.WARNING),
    HP1("Possible Hypertension", "อาจเป็นโรคความดันโลหิตสูง",140,159,90,99,"advice_d",RiskLevel.WARNING),
    HP2("Probable Hypertension", "น่าจะเป็นโรคความดันโลหิตสูง", 160,179,100,109,"advice_e",RiskLevel.DANGER),
    HP3("Definite Hypertension","เป็นโรคความดันโลหิตสูง",180,1000,110,1000,"advice_f",RiskLevel.DANGER),
    DIABETES_LOW("Below Threshold","ความดันโลหิตต่ำ",0,129,0,69,"advice_g",RiskLevel.WARNING),
    TBA("Unknown","ไม่มีข้อมูล",0,0,0,0,"advice_no",RiskLevel.UNKNOWN)
}
// 1000 is Magic Number
// Risk Level use only Safe,Danger,Warning and Unknown