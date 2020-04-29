package com.cnr.phr_android.dashboard.monitor.utility.entity

data class VitalSignRiskCheck(
        var heartRate:RiskLevel = RiskLevel.UNKNOWN,
        var spo2:RiskLevel = RiskLevel.UNKNOWN,
        var systolic:RiskLevel = RiskLevel.UNKNOWN,
        var diastolic:RiskLevel = RiskLevel.UNKNOWN,
        var glucose:RiskLevel = RiskLevel.UNKNOWN)