package com.cnr.phr_android.dashboard.monitor.utility.entity

data class DiseaseRiskCheck(
        var diabetes:RiskLevel = RiskLevel.UNKNOWN,
        var hypoxia:RiskLevel = RiskLevel.UNKNOWN,
        var hypertension:RiskLevel = RiskLevel.UNKNOWN,
        var stroke:RiskLevel = RiskLevel.UNKNOWN,
        var coronary:RiskLevel = RiskLevel.UNKNOWN
)