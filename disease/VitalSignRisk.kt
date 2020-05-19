package com.cnr.phr_android.dashboard.monitor.disease

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevelTemplate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Created by Theethawat@cnr - 2020-02-24
 * */

class VitalSignRisk(private val dataType: VitalsignDataType,
                    private val pressureSpecialType: BloodPressureDataType = BloodPressureDataType.NO) {
    // Class Constructor
    var riskLevel = RiskLevelTemplate()

    // Coroutines
    private val coroutineJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + coroutineJob)
    val riskLevelLiveData = MutableLiveData<RiskLevelTemplate>()
    val riskLevelExport = MutableLiveData<RiskLevel>()

    // Firebase Firestore
    private val firebaseFirestore = FirebaseFirestore.getInstance()


    init {
        uiScope.launch {
            getVitalSignRiskLevel(pressureSpecialType)
        }
    }

    // Public Function
     fun resetRiskLevel(){
        riskLevel = riskLevelLiveData.value!!
    }


    fun getYourDataRisk(latestValue: Int){
        riskLevelLiveData.observeForever { riskLevelPrepareUpdate ->
                riskLevelPrepareUpdate?.let { riskLevelNewData->
                    riskLevel = riskLevelNewData
                    Timber.v("Risk Level Update!!!!")
                    riskLevelExport.value = returnCurrentDataRisk(latestValue)
                    Timber.v("Data Send To Fragment")
                }
            }
    }

    // Private Function

    private fun returnCurrentDataRisk(latestValue: Int):RiskLevel{
        return if(latestValue >= riskLevel.safeMin && latestValue <= riskLevel.safeMax){
            Timber.v("****************************************")
            Timber.v("Got Value $latestValue return Safe")
            RiskLevel.SAFE
        }
        else if(latestValue >= riskLevel.riskMin && latestValue <= riskLevel.riskMax){
            Timber.v("****************************************")
            Timber.v("Got Value $latestValue return Risk")
            RiskLevel.WARNING
        }

        else if(latestValue >= riskLevel.dangerMin && latestValue <= riskLevel.dangerMax){
            Timber.v("****************************************")
            Timber.v("Got Value $latestValue return Danger / Moderate")
            RiskLevel.MODERATE
        }
        else{
            Timber.v("*****************************")
            Timber.v("Got Value $latestValue must return unknown")
            Timber.v("Safe margin ${riskLevel.safeMin} to ${riskLevel.safeMax}")
            Timber.v("Safe margin ${riskLevel.riskMin} to ${riskLevel.riskMax}")
            Timber.v("Safe margin ${riskLevel.dangerMin} to ${riskLevel.dangerMax}")
            RiskLevel.UNKNOWN
        }
    }

    private suspend fun getVitalSignRiskLevel(pressureSpecialType: BloodPressureDataType) {
        val firestoreRef = firebaseFirestore.collection("vitalsign_analyze")
                .document(getDocumentLabel(dataType, pressureSpecialType))
        withContext(Dispatchers.IO) {
            var tempVitalSignRiskLevel: RiskLevelTemplate
            firestoreRef.get()
                    .addOnFailureListener { Timber.v("Error on Get VitalSignRiskLevel $it") }
                    .addOnSuccessListener { snapshot ->
                        Timber.v("In getVitalSign Risk Level $dataType ")
                        tempVitalSignRiskLevel = getRiskLevelFromFirestore(snapshot)
                        riskLevelLiveData.value = tempVitalSignRiskLevel
                        resetRiskLevel()
                    }
        }
    }

    private fun getDocumentLabel(dataType: VitalsignDataType, pressureSpecialType: BloodPressureDataType): String {
        return if (dataType == VitalsignDataType.BLOOD_PRESSURE) {
            if (pressureSpecialType == BloodPressureDataType.SYSTOLIC)
                "systolic"
            else
                "diastolic"
        } else {
            dataType.label
        }
    }


    private fun getRiskLevelFromFirestore(snapshot: DocumentSnapshot): RiskLevelTemplate {
        val tempOtherRiskLevel :RiskLevelTemplate
        val danger: HashMap<String, Int> = snapshot["danger"] as HashMap<String, Int>
        val risk: HashMap<String, Int> = snapshot["risk"] as HashMap<String, Int>
        val safe: HashMap<String, Int> = snapshot["safe"] as HashMap<String, Int>
//        Timber.v(snapshot["danger"].toString())
//        Timber.v(snapshot["risk"].toString())
//        Timber.v(snapshot["safe"].toString())
        tempOtherRiskLevel = RiskLevelTemplate(
                safeMin = safe["min"]!!,
                safeMax = safe["max"]!!,
                riskMin = risk["min"]!!,
                riskMax = risk["max"]!!,
                dangerMin = danger["min"]!!,
                dangerMax = danger["max"]!!
        )
        Timber.v("Get VitalSign Risk Level !")
        Timber.v(tempOtherRiskLevel.toString())
        return tempOtherRiskLevel
    }


}
