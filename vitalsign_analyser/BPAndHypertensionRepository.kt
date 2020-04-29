package com.cnr.phr_android.dashboard.monitor.vitalsign_analyser

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskIndication
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import timber.log.Timber

/** Created by theethawat@cnr 2020-03-24 Use with Analysis Tab Fragment and the others*/
class BPAndHypertensionRepository(val systolic: Int, val diastolic: Int, val glucose: Int) {
    var mainIndicatorRisk = HypertensionLevel.TBA
    var diabetesRisk = false
    var cardiovascularDisease = false
    val adviceLiveData = MutableLiveData<String>()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + Job())
    private val database = FirebaseFirestore.getInstance()

    init {
        mainIndicatorRisk = calculateHypertensionLevel()
        getCoarseDiabetesFromVitalSign()
    }

    fun confirmAndRun() {
        checkLowerThreshold()
        addUserProfileFactor()
        viewModelScope.launch {
            getAdvice()
        }
    }

    fun exportRiskIndication(): List<RiskIndication> {
        val mainRiskInd = RiskIndication(mainIndicatorRisk.thaiName , mainIndicatorRisk.riskLevel)

        val diabetesInd = if (diabetesRisk)
            RiskIndication("มีโรคเบาหวานร่วม", RiskLevel.DANGER)
        else RiskIndication("ไม่มีโรคเบาหวาน", RiskLevel.SAFE)

        val cardioInd = if (cardiovascularDisease)
            RiskIndication("มีโรคเส้นเลือดหัวใจร่วม", RiskLevel.DANGER)
        else RiskIndication("ไม่มีโรคเส้นเลือดหัวใจ", RiskLevel.SAFE)

        Timber.v("Main Risk Specification is ${mainRiskInd.indication}")
        return listOf(mainRiskInd,diabetesInd,cardioInd)
    }

    fun addDiabetesFromUserProfile(inputDiabetes: Boolean) {
        if (inputDiabetes)
            diabetesRisk = true
    }

    fun addCardioVascularFromUserProfile(cardioDisease: Boolean) {
        if (cardioDisease)
            cardiovascularDisease = true
    }

    private fun addUserProfileFactor() {
        if (mainIndicatorRisk == HypertensionLevel.HIGH_NORMAL || mainIndicatorRisk == HypertensionLevel.HP1 || mainIndicatorRisk == HypertensionLevel.HP2)
            if (diabetesRisk || cardiovascularDisease)
                mainIndicatorRisk = HypertensionLevel.HP3
    }

    private fun calculateHypertensionLevel(): HypertensionLevel {
        if (systolic in HypertensionLevel.OPTIMAL.sbMin..HypertensionLevel.OPTIMAL.sbMax
                && diastolic in HypertensionLevel.OPTIMAL.dbMin..HypertensionLevel.OPTIMAL.dbMax) {
            return HypertensionLevel.OPTIMAL
        } else if (systolic in HypertensionLevel.NORMAL.sbMin..HypertensionLevel.NORMAL.sbMax
                || diastolic in HypertensionLevel.NORMAL.dbMin..HypertensionLevel.NORMAL.dbMax) {
            return HypertensionLevel.NORMAL
        } else if (systolic in HypertensionLevel.HIGH_NORMAL.sbMin..HypertensionLevel.HIGH_NORMAL.sbMax
                || diastolic in HypertensionLevel.HIGH_NORMAL.dbMin..HypertensionLevel.HIGH_NORMAL.dbMax) {
            return HypertensionLevel.HIGH_NORMAL
        } else if (systolic in HypertensionLevel.HP1.sbMin..HypertensionLevel.HP1.sbMax
                || diastolic in HypertensionLevel.HP1.dbMin..HypertensionLevel.HP1.dbMax) {
            return HypertensionLevel.HP1
        } else if (systolic in HypertensionLevel.HP2.sbMin..HypertensionLevel.HP2.sbMax
                || diastolic in HypertensionLevel.HP2.dbMin..HypertensionLevel.HP2.dbMax) {
            return HypertensionLevel.HP2
        } else if (systolic in HypertensionLevel.HP3.sbMin..HypertensionLevel.HP3.sbMax
                || diastolic in HypertensionLevel.HP3.dbMin..HypertensionLevel.HP3.dbMax) {
            return HypertensionLevel.HP3
        } else return HypertensionLevel.TBA
    }

    private fun getCoarseDiabetesFromVitalSign() {
        if (glucose >= 126)
            diabetesRisk = true
    }

    private fun checkLowerThreshold() {
        if (diabetesRisk) {
            if (mainIndicatorRisk == HypertensionLevel.NORMAL || mainIndicatorRisk == HypertensionLevel.OPTIMAL) {
                mainIndicatorRisk = HypertensionLevel.DIABETES_LOW
            }
        }
    }

    private suspend fun getAdvice() {
        withContext(Dispatchers.IO) {
            database.collection("disease").document("hypertension").get()
                    .addOnFailureListener {
                        Timber.v("Error Cannot Get Data From Firestore $it")
                    }
                    .addOnSuccessListener { snapshot ->
                        val userAdvice = snapshot.data?.get(mainIndicatorRisk.adviceRef)?.toString()
                        adviceLiveData.value = userAdvice
                    }
        }
    }

}