package com.cnr.phr_android.dashboard.monitor.utility

import android.graphics.Color
import android.widget.TextView
import com.cnr.phr_android.R
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel

/**
 * Create by theethawat@cnr 2020-03-04
 * */
class RiskColor {
    fun getRiskColor(risk:RiskLevel):Int{
        return when (risk) {
            RiskLevel.SAFE -> {
                Color.argb(255,50,205,50)
            }
            RiskLevel.WARNING -> {
                Color.argb(255,255,215,0)
            }
            RiskLevel.DANGER -> {
                Color.argb(255,255,69,0)
            }
            RiskLevel.MODERATE -> {
                Color.argb(255,255,69,0)
            }
            RiskLevel.MASSIVE -> {
                Color.argb(255,220,20,60)
            }
            RiskLevel.SUBSTANDARD -> {
                Color.argb(255,147,112,219)
            }
            else -> {
                Color.argb(255,64,224,208)
            }
        }
    }
}