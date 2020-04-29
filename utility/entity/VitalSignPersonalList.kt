package com.cnr.phr_android.dashboard.monitor.utility.entity

data class VitalSignPersonalList(
        var heartRate:Int? = null,
        var spo2:Int? = null,
        var systolic:Int? = null,
        var diastolic:Int? = null,
        var glucose:Int? = null
)