package com.cnr.phr_android.dashboard.monitor.monitor_listing

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.cnr.phr_android.dashboard.monitor.monitor_listing.analysis_tab.AnalysisTabFragment
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.HistoryTabFragment
import com.cnr.phr_android.dashboard.monitor.monitor_listing.statistic_tab.StatisticFragment

class MonitorPageAdapter(monitorFragmentManager: FragmentManager,
                         val position: Int,
                         private val inputUUID:String,
                         private val dataTypeString:String
) : FragmentStatePagerAdapter(monitorFragmentManager) {
    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putString("inputUUID",inputUUID)
        bundle.putString("dataTypeString",dataTypeString)
        return when (position) {
            0 -> {
                val analysisFrag = AnalysisTabFragment()
                analysisFrag.arguments = bundle
                analysisFrag
            }
            1 -> {
                val historyFrag = HistoryTabFragment()
                historyFrag.arguments = bundle
                historyFrag
            }
            2 -> {
                val statisticFrag = StatisticFragment()
                statisticFrag.arguments = bundle
                statisticFrag
            }
            else -> {
                AnalysisTabFragment()
            }
        }
    }

    override fun getCount(): Int {
        return position
    }
}

