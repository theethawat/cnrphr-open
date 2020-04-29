package com.cnr.phr_android.dashboard.monitor.monitor_listing

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.disease.VitalSignRisk
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevelTemplate
import com.cnr.phr_android.data.entity.*
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Create by theethawat@cnr - 2020-01-15
 * */

class MonitorMainViewModel(private val userVitalSignType: VitalsignDataType, private val userUUID: String, application: Application) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    val app = application
    var allData = MutableLiveData<List<DeviceRoomData>>()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val repository = MonitorMainRepository(userVitalSignType)

    private lateinit  var firstRiskCalculator:VitalSignRisk
    private lateinit var secondRiskCalculator:VitalSignRisk
    val firstSignRiskLevel = MutableLiveData<RiskLevelTemplate>()
    val secondSignRiskLevel = MutableLiveData<RiskLevelTemplate>()
    private lateinit var firebaseThisDataRef: Query

    init {
        if(userVitalSignType == VitalsignDataType.BLOOD_PRESSURE){
            firstRiskCalculator = VitalSignRisk(VitalsignDataType.BLOOD_PRESSURE,BloodPressureDataType.SYSTOLIC)
            secondRiskCalculator = VitalSignRisk(VitalsignDataType.BLOOD_PRESSURE,BloodPressureDataType.DIASTOLIC)
        }
        else{
            firstRiskCalculator = VitalSignRisk(userVitalSignType)
        }
        getData()

        // Observer
        repository.vitalSignDataList.observeForever {dataList->
            dataList?.let {
                allData.value = it
            }
        }
        firstRiskCalculator.riskLevelLiveData.observeForever { observer->
            observer?.let {
                firstSignRiskLevel.value = it
            }
        }

        if(userVitalSignType == VitalsignDataType.BLOOD_PRESSURE){
            secondRiskCalculator.riskLevelLiveData.observeForever { observer->
                observer?.let {
                    secondSignRiskLevel.value = it
                }
            }
        }

    }

    private fun getData() {
        uiScope.launch {
            firebaseThisDataRef = repository.getReference(userVitalSignType,userUUID)
            repository.populateData(firebaseThisDataRef)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.v("Monitor Main ViewModel Clear!!!")
        viewModelJob.cancel()
    }
}