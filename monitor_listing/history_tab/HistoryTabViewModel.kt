package com.cnr.phr_android.dashboard.monitor.monitor_listing.history_tab

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.monitor_listing.MonitorMainRepository
import com.cnr.phr_android.data.entity.DeviceRoomData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class HistoryTabViewModel(
        val userUUID: String,
        private val dataType: VitalsignDataType,
        application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val repository = MonitorMainRepository(dataType)
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val viewModelVitalSignList = MutableLiveData<List<DeviceRoomData>>()

    init {
        Timber.v("History Tab ViewModel is Running")
        viewModelPopulation()
    }

    private fun viewModelPopulation() {
        uiScope.launch {
            Timber.v(" >>>> View Model Population is Working in HistoryViewModel")
            val reference = repository.getReference(dataType, userUUID)
            repository.populateData(reference)
            repository.vitalSignDataList.observeForever { vitalSignList ->
                vitalSignList?.let {
                    Timber.v("VitalSignList is Change tell Back to Fragment")
                    viewModelVitalSignList.value = vitalSignList
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}