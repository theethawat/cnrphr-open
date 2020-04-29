package com.cnr.phr_android.dashboard.monitor.disease

import android.app.Application
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.dashboard.monitor.utility.AppCalculation
import com.cnr.phr_android.databinding.FragmentDiseasesMonitorBinding
import timber.log.Timber

/**
 * Created by theethawat@cnr -2020-02-13
 * */
class DiseaseMonitorFragment :Fragment(){
    private lateinit var binding:FragmentDiseasesMonitorBinding
    private lateinit var userUUID:String
    private lateinit var viewModel:DiseaseMonitorViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userUUID = arguments?.getString("userUUID")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_diseases_monitor,container,false)
        binding.allDiseaseToolbar.title = "Disease Monitor"
        val application = Application()
        val calculation =AppCalculation()
        val adapter = DiseaseMonitorAdapter()
        binding.diseaseRecyclerview.adapter = adapter
        viewModel = DiseaseMonitorViewModel(userUUID,application)
        viewModel.personalVitalSign.observe(viewLifecycleOwner, Observer { vitalSign->
            vitalSign?.let {personalValue->
                Timber.v("Vital Sign has fetched!")
                binding.valueSpo2.text = personalValue.spo2.toString()
                binding.valueDiastolic.text = personalValue.diastolic.toString()
                binding.valueGlucose.text = personalValue.glucose.toString()
                binding.valueHeartrate.text = personalValue.heartRate.toString()
                binding.valueSystolic.text = personalValue.systolic.toString()
                binding.userName.text = viewModel.userData.value!!.displayName.toString()
                val weight = viewModel.userData.value!!.weight
                val height=viewModel.userData.value!!.height
                val userHealthy = calculation.calculateBMI(weight, height)
                binding.valueBodyWeight.text =  userHealthy.name
            }
        })

        viewModel.diseaseDataList.observe(viewLifecycleOwner, Observer { diseaseObserver->
            diseaseObserver?.let {
                adapter.data = it
            }
        })

        return binding.root
    }
}

private operator fun CharSequence.invoke(s: String) {

}
