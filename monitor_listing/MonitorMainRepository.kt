package com.cnr.phr_android.dashboard.monitor.monitor_listing

import android.arch.lifecycle.MutableLiveData
import android.os.Build
import android.support.annotation.RequiresApi
import com.cnr.phr_android.base.user.DataUnit
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.GetDateFromFirestoreData
import com.cnr.phr_android.dashboard.monitor.utility.entity.TimeFilter
import com.cnr.phr_android.data.entity.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import com.jakewharton.threetenabp.*
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Create by theethawat@cnr - 2020-03-03
 * */
class MonitorMainRepository(private val userVitalSignType: VitalsignDataType) {
    val vitalSignDataList = MutableLiveData<List<DeviceRoomData>>()
    var backupVitalSignList:List<DeviceRoomData>? = null
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
                                .orderBy("measurementTime", Query.Direction.DESCENDING).get()
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

    @RequiresApi(Build.VERSION_CODES.O)
     fun filterData(filter:TimeFilter){
        Timber.v("Coming on Filter Data Function")

        val today = LocalDateTime.now()
        val lastWeek = today.minusDays(7)
        val lastTwoWeek = today.minusDays(14)
        val lastMonth = today.minusMonths(1)
        val lastThreeMonth = today.minusMonths(3)
        val lastYear = today.minusYears(1)
        if(backupVitalSignList == null){
            backupVitalSignList = vitalSignDataList.value
        }
        if(!backupVitalSignList.isNullOrEmpty()){
            val data:List<DeviceRoomData> = backupVitalSignList!!
           val result= when(filter){
               TimeFilter.ALL-> data
               TimeFilter.WEEK-> data.takeWhile { it.measurementTime.after( Date.from(lastWeek.atZone(ZoneId.systemDefault()).toInstant()) )}
               TimeFilter.TWO_WEEK-> data.takeWhile { it.measurementTime.after( Date.from(lastTwoWeek.atZone(ZoneId.systemDefault()).toInstant()) )}
               TimeFilter.MONTH-> data.takeWhile { it.measurementTime.after( Date.from(lastMonth.atZone(ZoneId.systemDefault()).toInstant()) )}
               TimeFilter.THREE_MONTH-> data.takeWhile { it.measurementTime.after( Date.from(lastThreeMonth.atZone(ZoneId.systemDefault()).toInstant()) )}
               TimeFilter.YEAR-> data.takeWhile { it.measurementTime.after( Date.from(lastYear.atZone(ZoneId.systemDefault()).toInstant()) )}
           }
            Timber.v("Filter Output")
            Timber.v(result.toString())
            vitalSignDataList.value = result
        }
    }
}