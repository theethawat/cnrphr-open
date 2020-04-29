package com.cnr.phr_android.dashboard.monitor.monitor_listing.statistic_tab

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.MapRangeAmount

class AverageStatViewModel(val dataType: VitalsignDataType, application: Application) : AndroidViewModel(application) {
    private var mainStatRepo: AverageStatRepository = AverageStatRepository(dataType)
    private lateinit var bpDiastolicRepo: AverageStatRepository
    val averageStatData = MutableLiveData<List<MapRangeAmount>>()
    val diastolicStatData = MutableLiveData<List<MapRangeAmount>>()

    fun observeDataSetAndWeight() {
        if (dataType == VitalsignDataType.BLOOD_PRESSURE) {
            bpDiastolicRepo = AverageStatRepository(VitalsignDataType.BLOOD_PRESSURE, BloodPressureDataType.DIASTOLIC)
            observeDiastolicDataSet()
        }
        mainStatRepo.exportedDataSet.observeForever { dataSet ->
            dataSet?.let {
                averageStatData.value = it
            }
        }
    }

    private fun observeDiastolicDataSet() {
        bpDiastolicRepo.exportedDataSet.observeForever { dataSet ->
            dataSet?.let {
                diastolicStatData.value = it
            }
        }
    }

    fun filterAge(initial: Int, finish: Int) {
        mainStatRepo.filterAge(initial, finish)
        if (dataType == VitalsignDataType.BLOOD_PRESSURE) {
            bpDiastolicRepo.filterAge(initial, finish)
        }
    }


}