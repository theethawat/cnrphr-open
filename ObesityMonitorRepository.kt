package com.cnr.phr_android.dashboard.monitor

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.dashboard.monitor.utility.entity.HealthyGroup
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber

class ObesityMonitorRepository {
    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    val obesityDescribe = MutableLiveData<String>()

    suspend fun getObesityDescribe(healthStatus:HealthyGroup){
        val firestore = FirebaseFirestore.getInstance()
        val label = firestore.collection("disease").document("obesity")
        var tempDescribe:String
        label.get()
                .addOnFailureListener {
                    Timber.v("Error! On Obesity Monitor,$it")
                }
                .addOnSuccessListener { snapshot->
                    tempDescribe = when (healthStatus) {
                        HealthyGroup.UNDERWEIGHT -> {
                            snapshot.data?.get("advice_a").toString()
                        }
                        HealthyGroup.NORMAL -> {
                            snapshot.data?.get("advice_b").toString()
                        }
                        HealthyGroup.OVERSIZE -> {
                            snapshot.data?.get("advice_c").toString()
                        }
                        HealthyGroup.OBESITY -> {
                            snapshot.data?.get("advice_d").toString()
                        }
                    }
                    obesityDescribe.value = tempDescribe
                }
    }
}