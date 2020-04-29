package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab

import android.app.Application
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.adapter.MonitorDoubleAdapter
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.adapter.MonitorMainAdapter
import com.cnr.phr_android.databinding.FragmentMonitorHistoryBinding
import timber.log.Timber

/**
 * Created by theethawat@cnr 2020-03-02
 * */
class HistoryTabFragment : Fragment() {
    private lateinit var binding: FragmentMonitorHistoryBinding
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var requestDataType: VitalsignDataType
    private lateinit var userUUID: String
    private lateinit var viewModel: HistoryTabViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestDataTypeString = arguments?.getString("dataTypeString")!!
        requestDataType = getVitalSignDataType(requestDataTypeString)
        userUUID = arguments?.getString("inputUUID")!!
        Timber.v("History Fragment Enter $requestDataType & UserUUID $userUUID")
        val application = Application()
        viewModel = HistoryTabViewModel(userUUID, requestDataType, application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_monitor_history, container, false)
        adapter = getDataAdapter(requestDataType)
        binding.historyRecyclerView.adapter = adapter
        viewModel.viewModelVitalSignList.observe(viewLifecycleOwner, Observer {vitalSignList->
            Timber.v("ViewModel observer is running in Fragment History")
            vitalSignList?.let {
                Timber.v(">>> VitalSign DataList Notified Change in History Tab Fragment!!!")
                if (adapter is MonitorMainAdapter) {
                    (adapter as MonitorMainAdapter).healthData = it
                }
                if (adapter is MonitorDoubleAdapter) {
                    (adapter as MonitorDoubleAdapter).healthData = it
                }
                Handler().postDelayed({
                    if (vitalSignList.isNotEmpty()) {
                        adapter.notifyDataSetChanged()
                    }
                }, 2000)
            }
        })
        return binding.root
    }

    private fun getDataAdapter(dataType: VitalsignDataType): RecyclerView.Adapter<*> {
        return when (dataType) {
            VitalsignDataType.BLOOD_PRESSURE -> MonitorDoubleAdapter(dataType)
            VitalsignDataType.BLOOD_GLUCOSE -> MonitorMainAdapter(dataType)
            VitalsignDataType.SPO2 -> MonitorMainAdapter(dataType)
            VitalsignDataType.HEART_RATE -> MonitorMainAdapter(dataType)
            else -> MonitorMainAdapter(dataType)
        }
    }

    private fun getVitalSignDataType(dataText: String): VitalsignDataType {
        Timber.v("Get VitalSign Data Type Has called ")
        return when (dataText) {
            "BloodGlucose" -> VitalsignDataType.BLOOD_GLUCOSE
            "BloodPressure" -> VitalsignDataType.BLOOD_PRESSURE
            "Spo2" -> VitalsignDataType.SPO2
            "HeartRate" -> VitalsignDataType.HEART_RATE
            else -> VitalsignDataType.BLOOD_GLUCOSE
        }
    }
}