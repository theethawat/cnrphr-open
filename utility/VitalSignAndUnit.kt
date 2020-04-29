package com.cnr.phr_android.dashboard.monitor.utility

import com.cnr.phr_android.base.user.DataUnit
import com.cnr.phr_android.base.user.VitalsignDataType

class VitalSignAndUnit {
    fun getUnitFromVitalSing(dataType: VitalsignDataType):DataUnit{
        return when(dataType){
            VitalsignDataType.SPO2-> DataUnit.PERCENT_OF_OXYGEN_IN_HB
            VitalsignDataType.BLOOD_GLUCOSE->DataUnit.KG_PER_L
            VitalsignDataType.HEART_RATE->DataUnit.BPM
            VitalsignDataType.BLOOD_PRESSURE->DataUnit.MM_HG
            else -> DataUnit.PERCENT_OF_OXYGEN_IN_HB
        }
    }
}