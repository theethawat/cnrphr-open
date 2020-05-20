package com.cnr.phr_android.dashboard.monitor

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.dashboard.monitor.disease.DiseaseMonitorRepository
import com.cnr.phr_android.dashboard.monitor.utility.AppCalculation
import com.cnr.phr_android.dashboard.monitor.utility.entity.HealthyGroup
import com.cnr.phr_android.dashboard.monitor.utility.entity.VitalSignPersonalList
import com.cnr.phr_android.data.user.FirebaseUser
import kotlinx.coroutines.*
import timber.log.Timber

// Create by theethawat@cnr - 2020-01-28

class InfoPreviewViewModel(val userUUID: String,val userName:String,application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val currentUserCoruetines = MutableLiveData<FirebaseUser>()
    var currentUser:FirebaseUser? = null
    val userVitalSignList = MutableLiveData<VitalSignPersonalList>()
    val userObesityDescribe = MutableLiveData<String>()
    private val infoPreviewRepository = InfoPreviewRepository()
    private val obesityMonitor = ObesityMonitorRepository()
    private val appCalculation = AppCalculation()
    private lateinit var  diseaseMonitorRepository : DiseaseMonitorRepository

    fun findUserFromFirestore() {
        uiScope.launch {
            infoPreviewRepository.findUserInFirestoreCoroutines(userUUID, userName)
        }
        infoPreviewRepository.currentUser.observeForever{ repositoryUser->
           repositoryUser?.let {
               currentUserCoruetines.value = repositoryUser
               resetUser()
               getUserInformation()
               getObesityInformation()
           }
        }
    }

    private fun resetUser(){
        currentUser = currentUserCoruetines.value
    }

    private fun getUserInformation(){
        Timber.v("Get Ready for Fetching Input Section Data From Data Repository")
        diseaseMonitorRepository = DiseaseMonitorRepository()
        if(currentUser != null){
            currentUser!!.inputProgramUser?.let { diseaseMonitorRepository.fetchAllLatestValue(it) }
            diseaseMonitorRepository.coroutinePersonalVitalSign.observeForever { personalList->
                Timber.v("VitalSignList has Fetched in Repository")
                personalList?.let {
                    userVitalSignList.value = it
                }
            }
        }
        else{
            Timber.v("No User Information")
        }
    }

    private fun getObesityInformation(){
        if(currentUser != null){
            val healthyGroup:HealthyGroup = appCalculation.calculateBMI(currentUser!!.weight, currentUser!!.height)
            uiScope.launch {
                obesityMonitor.getObesityDescribe(healthyGroup)
            }
            obesityMonitor.obesityDescribe.observeForever {describe->
                describe?.let {
                    userObesityDescribe.value = it
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.v("UI Scope is Cleared")
        viewModelJob.cancel()
    }
}