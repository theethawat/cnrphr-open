package com.cnr.phr_android.dashboard.monitor.monitor_listing

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.DataUnit
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.GetDateFromFirestoreData
import com.cnr.phr_android.data.entity.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Create by theethawat@cnr - 2020-03-03
 * */
class MonitorMainRepository(private val userVitalSignType: VitalsignDataType) {
    val vitalSignDataList = MutableLiveData<List<DeviceRoomData>>()
    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    fun populateData(firebaseReference: Query){
        uiScope.launch { vitalSignDataList.value = saveDataIntoList(firebaseReference) }
    }

    fun getReference(vitalSignType: VitalsignDataType,userUUID:String): Query {
        val db = FirebaseFirestore.getInstance()
        val vitalSignRef: CollectionReference = when (vitalSignType) {
            VitalsignDataType.BLOOD_PRESSURE -> {
                db.collection("blood_pressure")
            }
            VitalsignDataType.SPO2 -> {
                db.collection("spo2")
            }
            VitalsignDataType.BLOOD_GLUCOSE -> {
                db.collection("glucose")
            }
            VitalsignDataType.HEART_RATE -> {
                db.collection("heart_rate")
            }
            else -> {
                throw IllegalArgumentException("Not support Vital Sign Type")
            }
        }
        Timber.v("UUID when check for fetch:: $userUUID")
        return  vitalSignRef.whereEqualTo("ownerUUID", userUUID)
    }

    private suspend fun saveDataIntoList(firebaseReference: Query): List<DeviceRoomData> {
        val tempList = ArrayList<DeviceRoomData>()
        return withContext(Dispatchers.IO) {
            firebaseReference
                    .get().continueWith {
                        firebaseReference
                                .orderBy("measurementTime", Query.Direction.DESCENDING).limit(12).get()
                                .addOnFailureListener {
                                    Timber.v("Error : $it")
                                }
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        //Timber.v(":: ${document.data}")
                                        val tempData = getTempData(document.data)
                                        tempList.add(tempData)
                                    }
                                }
                    }
            tempList
        }
    }

    private fun getTempData(queryData: Map<String, Any>): DeviceRoomData {
        when (userVitalSignType) {
            VitalsignDataType.HEART_RATE -> {
                return HeartRate(queryData["value"].toString().toIntOrNull(),
                        DataUnit.BPM,
                        queryData["energyExpended"].toString().toIntOrNull(),
                        queryData["rrInterval"].toString().toIntOrNull(),
                        VitalsignDataType.HEART_RATE,
                        GetDateFromFirestoreData(queryData["measurementTime"].toString()).getDate())
            }
            VitalsignDataType.SPO2 -> {
                return Spo2(queryData["pulseRate"].toString().toIntOrNull(),
                        queryData["pulseOximeter"].toString().toIntOrNull()
                        ,
                        VitalsignDataType.SPO2,
                        GetDateFromFirestoreData(queryData["measurementTime"].toString()).getDate())
            }
            VitalsignDataType.BLOOD_GLUCOSE -> {
                return BloodGlucose(queryData["sequenceNumber"].toString().toIntOrNull(),
                        queryData["timeOffset"].toString().toIntOrNull(),
                        queryData["value"].toString().toFloatOrNull()?.times(100000F),
                        queryData["type"].toString(),
                        queryData["sampleLocation"].toString(), null,
                        DataUnit.KG_PER_L,
                        VitalsignDataType.BLOOD_GLUCOSE,
                        GetDateFromFirestoreData(queryData["measurementTime"].toString()).getDate())
            }
            VitalsignDataType.BLOOD_PRESSURE -> {
                return BloodPressure(queryData["systolic"].toString().toFloatOrNull(),
                        queryData["diastolic"].toString().toFloatOrNull(),
                        queryData["mean"].toString().toFloatOrNull(),
                        DataUnit.MM_HG,
                        queryData["pulse"].toString().toFloatOrNull(),
                        queryData["userId"].toString().toIntOrNull(),
                        GetDateFromFirestoreData(queryData["measurementTime"].toString()).getDate(),
                        VitalsignDataType.BLOOD_PRESSURE)
            }
            else -> {
                throw IllegalArgumentException(" Not Support Data Type") as Throwable
            }
        }
    }
}