package com.cnr.phr_android.dashboard.monitor.monitor_listing

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.cnr.phr_android.base.user.VitalsignDataType

/**
 * Create by theethawat@cnr - update at 2020-01-15
 * */
class MonitorViewModelFactory (private val requestDataType: VitalsignDataType,val userUUID:String,val application: Application)
    :ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(MonitorMainViewModel::class.java)){
                return MonitorMainViewModel(requestDataType, userUUID, application) as T
            }
        throw  IllegalArgumentException("Unknown ViewModel Class")
    }
}