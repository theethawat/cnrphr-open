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
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val repository = MonitorMainRepository(userVitalSignType)

    private lateinit  var firstRiskCalculator:VitalSignRisk
    private lateinit var secondRiskCalculator:VitalSignRisk

    var allData = MutableLiveData<List<DeviceRoomData>>()
    val firstSignRiskLevel = MutableLiveData<RiskLevelTemplate>()
    val secondSignRiskLevel = MutableLiveData<RiskLevelTemplate>()
    val readyStatus = MutableLiveData<Boolean>()
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
        observeDataFetching()
    }

    private fun observeDataFetching(){
        repository.vitalSignDataList.observeForever {dataList->
            dataList?.let {
                Timber.v("Set From Observe Data Fetching")
                allData.value = it
                if(userVitalSignType == VitalsignDataType.BLOOD_PRESSURE){
                    if(firstSignRiskLevel.value != null && secondSignRiskLevel.value != null){
                        Timber.v("Set Ready Status as True")
                        readyStatus.value = true
                    }
                }
                else{
                    if(firstSignRiskLevel.value != null)
                        readyStatus.value = true
                    Timber.v("Set Ready Status as True")
                }
            }
        }

        firstRiskCalculator.riskLevelLiveData.observeForever { observer->
            observer?.let {
                firstSignRiskLevel.value = it
                Timber.v("Set From FirstRiskCalculate ")
                if(!allData.value.isNullOrEmpty()){
                    Timber.v("Set Ready Status as True")
                    readyStatus.value = true
                }
            }
        }

        if(userVitalSignType == VitalsignDataType.BLOOD_PRESSURE){
            secondRiskCalculator.riskLevelLiveData.observeForever { observer->
                observer?.let {
                    Timber.v("Set From Secound Risk")
                    secondSignRiskLevel.value = it
                    if(firstSignRiskLevel.value != null && !(allData.value.isNullOrEmpty())){
                        Timber.v("Set Ready Status as True")
                        readyStatus.value = true
                    }
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