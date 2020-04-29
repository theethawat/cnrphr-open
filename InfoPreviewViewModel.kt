package com.cnr.phr_android.dashboard.monitor

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.dashboard.monitor.disease.DiseaseMonitorRepository
import com.cnr.phr_android.dashboard.monitor.utility.entity.HealthyGroup
import com.cnr.phr_android.dashboard.monitor.utility.entity.VitalSignPersonalList
import com.cnr.phr_android.data.user.FirebaseUser
import kotlinx.coroutines.*
import timber.log.Timber

// Create by theethawat@cnr - 2020-01-28

class InfoPreviewViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val currentUser = MutableLiveData<FirebaseUser>()
    val userVitalSignList = MutableLiveData<VitalSignPersonalList>()
    val userObesityDescribe = MutableLiveData<String>()
    private val infoPreviewRepository = InfoPreviewRepository()
    private val obesityMonitor = ObesityMonitorRepository()
    private lateinit var  diseaseMonitorRepository : DiseaseMonitorRepository

    fun findUserFromFirestore(userUUID: String, userName: String) {
        uiScope.launch {
            infoPreviewRepository.findUserInFirestoreCoroutines(userUUID, userName)
        }
        infoPreviewRepository.currentUser.observeForever{ repositoryUser->
           repositoryUser?.let {
               currentUser.value = repositoryUser
           }
        }
    }

    fun getUserInformation(inputSectionUUID:String){
        Timber.v("Get Ready for Fetching Input Section Data From Data Repository")
        diseaseMonitorRepository = DiseaseMonitorRepository()
        diseaseMonitorRepository.fetchAllLatestValue(inputSectionUUID)
        diseaseMonitorRepository.coroutinePersonalVitalSign.observeForever { personalList->
            Timber.v("VitalSignList has Fetched in Repository")
            personalList?.let {
                userVitalSignList.value = it
            }
        }
    }

    fun getObesityInformation(healthyGroup: HealthyGroup){
        uiScope.launch {
            obesityMonitor.getObesityDescribe(healthyGroup)
        }
        obesityMonitor.obesityDescribe.observeForever {describe->
            describe?.let {
                userObesityDescribe.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.v("UI Scope is Cleared")
        viewModelJob.cancel()
    }
}