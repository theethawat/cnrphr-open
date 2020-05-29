package com.cnr.phr_android.dashboard.monitor.disease

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.DiseaseRiskTranslator
import com.cnr.phr_android.dashboard.monitor.utility.entity.VitalSignPersonalList
import com.cnr.phr_android.data.user.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Suspending Fun for Working on Coroutine with cloud Firestore
 * created by theethawat@cnr - 2020-02-20
 * */

class DiseaseMonitorRepository {

    val coroutineUserData = MutableLiveData<FirebaseUser>()
    val coroutinePersonalVitalSign = MutableLiveData<VitalSignPersonalList>()
    val spo2 = MutableLiveData<Int>()
    val systolic = MutableLiveData<Int>()
    val diastolic = MutableLiveData<Int>()
    val glucose = MutableLiveData<Int>()
    val heartRate = MutableLiveData<Int>()
    val diseaseRiskTranslate  = DiseaseRiskTranslator()
    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun fetchPersonalDataCoroutines(userUUID: String) {
        Timber.v("Coroutine Initial Personal Data is Calling")
        val userDataRef = firestore.collection("user")
        var userDataInFirestore: FirebaseUser?
        withContext(Dispatchers.IO) {
            userDataRef.whereEqualTo("uuid", userUUID).limit(1).get()
                    .addOnFailureListener {
                        Timber.v("Error on Finding User in Firestore : $it")
                    }
                    .addOnSuccessListener { snapshot ->
                        for (document in snapshot) {
                            userDataInFirestore = FirebaseUser(
                                    displayName = document.data["displayName"].toString(),
                                    inputProgramUser = document.data["inputProgramUser"]!!.toString(),
                                    uuid = document.data["uuid"]!!.toString(),
                                    bDay = document.data["bDay"]!!.toString().toInt(),
                                    bMonth = document.data["bMonth"]!!.toString().toInt(),
                                    bYear = document.data["bYear"]!!.toString().toInt(),
                                    height = document.data["height"]!!.toString().toFloat(),
                                    weight = document.data["weight"]!!.toString().toFloat(),
                                    userSex = document.data["user_sex"]!!.toString(),
                                    coronary = document.data["coronary"]!!.toString().toBoolean(),
                                    kidney = document.data["kidney"]!!.toString().toBoolean(),
                                    diabetes = document.data["diabetes"]!!.toString().toBoolean(),
                                    adminStatus = document.data["adminStatus"]!!.toString().toBoolean(),
                                    isCoronary = diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isCoronary"]!!.toString()),
                                    isHypertension = diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isHypertension"]!!.toString()) ,
                                    isHypoxia = diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isHypoxia"]!!.toString()) ,
                                    isDiabetes = diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isDiabetes"]!!.toString())
                            )
                            coroutineUserData.value = userDataInFirestore
                        }
                    }
        }
    }

    suspend fun fetchPersonalDataFromInputSectionUser(userUUID: String) {
        Timber.v("Coroutine Initial Personal Data is Calling")
        val userDataRef = firestore.collection("user")
        var userDataInFirestore: FirebaseUser?
        withContext(Dispatchers.IO) {
            userDataRef.whereEqualTo("inputProgramUser", userUUID).limit(1).get()
                    .addOnFailureListener {
                        Timber.v("Error on Finding User in Firestore : $it")
                    }
                    .addOnSuccessListener { snapshot ->
                        for (document in snapshot) {
                            userDataInFirestore = FirebaseUser(
                                    displayName = document.data["displayName"].toString(),
                                    inputProgramUser = document.data["inputProgramUser"]!!.toString(),
                                    uuid = document.data["uuid"]!!.toString(),
                                    bDay = document.data["bday"]!!.toString().toInt(),
                                    bMonth = document.data["bmonth"]!!.toString().toInt(),
                                    bYear = document.data["byear"]!!.toString().toInt(),
                                    height = document.data["height"]!!.toString().toFloat(),
                                    weight = document.data["weight"]!!.toString().toFloat(),
                                    userSex = document.data["user_sex"]!!.toString(),
                                    coronary = document.data["coronary"]!!.toString().toBoolean(),
                                    kidney = document.data["kidney"]!!.toString().toBoolean(),
                                    diabetes = document.data["diabetes"]!!.toString().toBoolean(),
                                    adminStatus = document.data["adminStatus"]!!.toString().toBoolean()
                            )
                            coroutineUserData.value = userDataInFirestore
                        }
                    }
        }
    }

    fun fetchAllLatestValue(inputSectionUUID: String) {
        uiScope.launch {
            fetchLatestVitalSignCoroutines(inputSectionUUID, "spo2")
            fetchLatestVitalSignCoroutines(inputSectionUUID, "glucose")
            fetchLatestVitalSignCoroutines(inputSectionUUID, "systolic")
            fetchLatestVitalSignCoroutines(inputSectionUUID, "diastolic")
            fetchLatestVitalSignCoroutines(inputSectionUUID, "heart_rate")
        }

    }

    private fun refreshVitalSignTempValue() {
        val tempVitalSignPersonalList = VitalSignPersonalList(
                spo2 = spo2.value, glucose = glucose.value, heartRate = heartRate.value, diastolic = diastolic.value, systolic = systolic.value
        )
        Timber.v("VitalSign Personal List => $tempVitalSignPersonalList")
        Timber.v("---------------------------")
        if (diastolic.value != null && systolic.value != null
                && glucose.value != null && spo2.value != null
                && heartRate.value != null) {
            coroutinePersonalVitalSign.value = tempVitalSignPersonalList
        }
    }

    suspend fun fetchLatestVitalSignCoroutines(inputSectionUUID: String, label: String) {
        withContext(Dispatchers.IO) {
            var fetchedDataResult: Int
            val vitalSignCollectionRef: CollectionReference = if (label == "systolic" || label == "diastolic") {
                firestore.collection("blood_pressure")
            } else {
                firestore.collection(label)
            }
            Timber.v(vitalSignCollectionRef.toString())
        vitalSignCollectionRef.whereEqualTo("ownerUUID", inputSectionUUID).orderBy("measurementTime",Query.Direction.DESCENDING).get()
            //   vitalSignCollectionRef.orderBy("measurementTime", Query.Direction.DESCENDING).get().continueWith {
//                   vitalSignCollectionRef.whereEqualTo("ownerUUID", inputSectionUUID).limit(1).get()

                           .addOnFailureListener {
                               Timber.v("Cannot Get Latest Vital Sign Data From Firestore : $it")
                           }
                           .addOnSuccessListener { snapshot ->
                               Timber.v("Snapshot is $snapshot ")
                               if (snapshot.isEmpty) {
                                   Timber.v("This Vital Sign  $label has no data")
                                   fetchedDataResult = 0
                               } else {
                                   Timber.v("Snapshot First Value is ${snapshot.first()}")
                                   fetchedDataResult = getDataSpecificOnType(label, snapshot.first())
                                   Timber.v("Data Result of $label is $fetchedDataResult")
                               }
                               when (label) {
                                   "heart_rate" -> {
                                       heartRate.value = fetchedDataResult
                                   }
                                   "glucose" -> {
                                       glucose.value = fetchedDataResult
                                   }
                                   "spo2" -> {
                                       spo2.value = fetchedDataResult
                                   }
                                   "systolic" -> {
                                       systolic.value = fetchedDataResult
                                   }
                                   "diastolic" -> {
                                       diastolic.value = fetchedDataResult
                                   }
                               }
                               refreshVitalSignTempValue()
                           }
               }
    }

    private fun getDataSpecificOnType(dataType: String, document: QueryDocumentSnapshot): Int {
        Timber.v("Program has Called getDataSpecificOnType")
        var vitalSignTempValue: Int?
        when (dataType) {
            "heart_rate" -> {
                vitalSignTempValue = document.data["value"].toString().toIntOrNull()
            }
            "spo2" -> {
                vitalSignTempValue = document.data["pulseOximeter"].toString().toIntOrNull()
            }
            "glucose" -> {
               // Timber.v("!!! Blood Glucose Value !!!")
               // Timber.v(document.data["value"].toString())
                val glucoseTempValue = document.data["value"].toString().toFloatOrNull()
                vitalSignTempValue = if (glucoseTempValue != null) {
                    (100000 * glucoseTempValue).toInt()
                } else null
            }
            "systolic" -> {
                vitalSignTempValue = document.data["systolic"].toString().toFloat().toInt()
            }
            "diastolic" -> {
                vitalSignTempValue = document.data["diastolic"].toString().toFloat().toInt()
            }
            else -> vitalSignTempValue = 0
        }
        Timber.v("Vital Sign Latest Value of $dataType is $vitalSignTempValue")
        return vitalSignTempValue ?: 0
    }

}
