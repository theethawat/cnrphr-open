package com.cnr.phr_android.dashboard.monitor.utility.entity

data class RiskLevelTemplate (
        val safeMin:Int = 0,
        val safeMax:Int = 0,
        val riskMin:Int = 0,
        val riskMax:Int = 0,
        val dangerMin:Int = 0,
        val dangerMax:Int = 0
)