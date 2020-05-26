package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.adapter.MonitorDoubleAdapter
import com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab.adapter.MonitorMainAdapter
import com.cnr.phr_android.dashboard.monitor.utility.entity.TimeFilter
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
    private var timeFilter:TimeFilter = TimeFilter.ALL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestDataTypeString = arguments?.getString("dataTypeString")!!
        requestDataType = getVitalSignDataType(requestDataTypeString)
        userUUID = arguments?.getString("inputUUID")!!
        Timber.v("History Fragment Enter $requestDataType & UserUUID $userUUID")
        val application = Application()
        viewModel = HistoryTabViewModel(userUUID, requestDataType, application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_monitor_history, container, false)
        adapter = getDataAdapter(requestDataType)
        binding.historyRecyclerView.adapter = adapter
        observeDataAdapter()
        createSpinner()
        binding.buttonReset.setOnClickListener {
            viewModel.requestDataFilter(timeFilter)
            observeDataAdapter()
        }
        return binding.root
    }

    // Spinner Inner class
    inner class SpinnerActivity:Activity(),AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            timeFilter = when(pos){
                0->TimeFilter.ALL
                1->TimeFilter.WEEK
                2->TimeFilter.TWO_WEEK
                3->TimeFilter.MONTH
                4->TimeFilter.THREE_MONTH
                5->TimeFilter.YEAR
                else-> TimeFilter.ALL
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }




    private fun createSpinner(){
        val spinner: Spinner = binding.spinnerFilter
        ArrayAdapter.createFromResource(this.context!!,R.array.time_filter,android.R.layout.simple_spinner_item)
                .also { adapter->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
        spinner.onItemSelectedListener = this.SpinnerActivity()
    }


    private fun observeDataAdapter(){
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
//                    if (vitalSignList.isNotEmpty()) {
                        adapter.notifyDataSetChanged()
//                    }
                }, 2000)
            }
        })
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