package com.cnr.phr_android.dashboard.monitor.monitor_listing.analysis_tab
/**
 * Created by theethawat 2020-03-26
 * */
data class AdviceDataSet (
        val adviceDanger:String,
        val adviceRisk:String,
        val adviceSafe:String,
        val averageValue :HashMap<String,List<Double>>,
        val averageValueDiastolic:HashMap<String,List<Double>> = hashMapOf(Pair("men", emptyList()),Pair("women", emptyList())),
        val patientDensity:HashMap<String,List<Double>>,
        val relateDisease:List<String>
)

