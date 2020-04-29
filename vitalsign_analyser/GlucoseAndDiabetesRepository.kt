package com.cnr.phr_android.dashboard.monitor.vitalsign_analyser

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.disease.VitalSignRisk
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskIndication
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Create by theethawat@cnr - 2020-04
 * */
class GlucoseAndDiabetesRepository(val glucose: Int, val systolic: Int, val diastolic: Int, val diabetes: Boolean) {

    private val glucoseRiskAnalyse = VitalSignRisk(VitalsignDataType.BLOOD_GLUCOSE)
    private val bloodPressureAnalyse = VitalSignRisk(VitalsignDataType.BLOOD_PRESSURE, BloodPressureDataType.SYSTOLIC)
    private var diabetesRisk: RiskLevel = RiskLevel.UNKNOWN
    private var diabetesHTRisk: RiskLevel = RiskLevel.UNKNOWN
    private lateinit var htIndication: RiskIndication
    private lateinit var  inputDiabetesRiskIndication:RiskIndication
    private lateinit var diabetesRiskIndication: RiskIndication
    val adviceLiveData = MutableLiveData<String>()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + Job())
    private val database = FirebaseFirestore.getInstance()

    init {
        bloodPressureAnalyse.riskLevelLiveData.observeForever { bpRisk ->
            bpRisk?.let {
                Timber.v("Ready For Run Hypertension Analysis in Diabetes Repository")
                bloodPressureAnalyse.resetRiskLevel()
                getHTRisk()
                glucoseRiskAnalyse.riskLevelLiveData.observeForever { glucoseRisk->
                    glucoseRisk?.let {
                        Timber.v("Ready for Analysing Diabetes")
                        glucoseRiskAnalyse.resetRiskLevel()
                        mainAnalyze()

                    }
                }
            }
        }
    }

    private fun getHTRisk(){
        diabetesHTRisk = if (systolic == 0) {
            Timber.v("Systolic is 0")
            RiskLevel.UNKNOWN
        } else {
            bloodPressureAnalyse.getYourDataRisk(systolic)
        }
        Timber.v("In Get Hypertension Risk Running")
        Timber.v("Add Data For Systolic $systolic")
        htIndication = if (diabetesHTRisk == RiskLevel.SAFE || diabetesHTRisk == RiskLevel.SUBSTANDARD) {
            RiskIndication("ความดันโลหิตอยู่ในเกณฑ์ปกติ", RiskLevel.SAFE)
        } else if (diabetesHTRisk == RiskLevel.MODERATE || diabetesHTRisk == RiskLevel.WARNING || diabetesHTRisk == RiskLevel.MASSIVE || diabetesHTRisk == RiskLevel.DANGER) {
            RiskIndication("ความดันโลหิตสูงกว่าปกติ", RiskLevel.DANGER)
        } else {
            Timber.v("Hypertension Risk $diabetesHTRisk")
            RiskIndication("ไม่มีข้อมูล", RiskLevel.UNKNOWN)
        }
    }

    private fun mainAnalyze() {
        Timber.v("Diabetes Risk Analysis is now Runnning")
        diabetesRisk = glucoseRiskAnalyse.getYourDataRisk(glucose)
        diabetesRisk = addHTForDiabetes(diabetesHTRisk)

        inputDiabetesRiskIndication = if(diabetes){
            RiskIndication("มีประวัติเป็นโรคเบาหวาน",RiskLevel.DANGER)
        } else{
            RiskIndication("ไม่มีประวัติเป็นโรคเบาหวาน",RiskLevel.SAFE)
        }

        diabetesRiskIndication =
                if(diabetes){
                    if(diabetesRisk == RiskLevel.SAFE)
                    { viewModelScope.launch { getAdvice("advice_a") }
                        RiskIndication("ผลอยู่ในเกณฑ์ดี",RiskLevel.SAFE)
                    }
                    else if(diabetesRisk == RiskLevel.WARNING){
                        viewModelScope.launch { getAdvice("advice_b") }
                        RiskIndication("มีความเสี่ยงสูง ต้องควบคุมอาหาร",RiskLevel.DANGER)
                    }
                    else if(diabetesRisk == RiskLevel.DANGER ){
                        viewModelScope.launch { getAdvice("advice_c") }
                        RiskIndication("ระดับน้ำตาลสูงเกินไป ควรพบแพทย์",RiskLevel.DANGER)
                    }
                    else if(diabetesRisk == RiskLevel.MODERATE || diabetesRisk == RiskLevel.MASSIVE){
                        viewModelScope.launch { getAdvice("advice_d") }
                        RiskIndication("ควรอยู่ภายใต้การดูแลของแพทย์",RiskLevel.DANGER)
                    }
                    else{
                        viewModelScope.launch { getAdvice("advice_e") }
                        RiskIndication("ไม่ทราบข้อมูล",RiskLevel.SAFE)
                    }
                }else{
                    if(diabetesRisk == RiskLevel.SAFE){
                        viewModelScope.launch { getAdvice("advice_f") }
                        RiskIndication("ผลอยู่ในเกณฑ์ดีมาก",RiskLevel.SAFE)
                    }
                    else if(diabetesRisk == RiskLevel.WARNING){
                        viewModelScope.launch { getAdvice("advice_g") }
                        RiskIndication("มีความเสี่ยงต่อการเป็นเบาหวาน",RiskLevel.DANGER)
                    }
                    else if(diabetesRisk == RiskLevel.DANGER ){
                        viewModelScope.launch { getAdvice("advice_h") }
                        RiskIndication("ระดับน้ำตาลสูงเกินไป อาจจะเป็นเบาหวาน",RiskLevel.DANGER)
                    }
                    else if(diabetesRisk == RiskLevel.MODERATE || diabetesRisk == RiskLevel.MASSIVE){
                        viewModelScope.launch { getAdvice("advice_i") }
                        RiskIndication("ควรพบแพทย์ เพื่อตรวจอย่างละเอียด",RiskLevel.DANGER)
                    }
                    else{
                        viewModelScope.launch { getAdvice("advice_e") }
                        RiskIndication("ไม่ทราบข้อมูล",RiskLevel.SAFE)
                    }
                }
    }

    private fun addHTForDiabetes(htRisk:RiskLevel):RiskLevel{
        Timber.v("In Add Hypertension for Diabetes")
        return if(htRisk == RiskLevel.UNKNOWN || htRisk == RiskLevel.SAFE || htRisk == RiskLevel.SUBSTANDARD){
            diabetesRisk
        }
        else if(htRisk == RiskLevel.WARNING){
            addOneRisk(diabetesRisk)
        }
        else if(htRisk == RiskLevel.WARNING || htRisk == RiskLevel.DANGER || htRisk == RiskLevel.MODERATE || htRisk == RiskLevel.MASSIVE){
            addOneRisk(addOneRisk(diabetesRisk))
        }
        else
            RiskLevel.UNKNOWN
    }

    private fun addOneRisk(risk:RiskLevel):RiskLevel{
        Timber.v("In Add One Risk")
        return if(risk == RiskLevel.SUBSTANDARD || risk == RiskLevel.SAFE){
            RiskLevel.WARNING
        }
        else if(risk == RiskLevel.WARNING)
            RiskLevel.DANGER
        else if(risk == RiskLevel.DANGER)
            RiskLevel.MODERATE
        else if(risk == RiskLevel.MODERATE)
            RiskLevel.MASSIVE
        else if (risk == RiskLevel.MASSIVE)
            RiskLevel.MASSIVE
        else
            RiskLevel.UNKNOWN
    }

    fun exportRiskIndication():List<RiskIndication>{
        Timber.v("Export Risk Indication")
        Timber.v("Diabetes Risk Indication $diabetesRiskIndication")
        Timber.v("Input Diabetes Risk $inputDiabetesRiskIndication")
        Timber.v("Hypertension Risk $htIndication")
        return listOf(diabetesRiskIndication,inputDiabetesRiskIndication,htIndication)
    }

    private suspend fun getAdvice(adviceLabel:String) {
        withContext(Dispatchers.IO) {
            database.collection("disease").document("diabetes").get()
                    .addOnFailureListener {
                        Timber.v("Error Cannot Get Data From Firestore $it")
                    }
                    .addOnSuccessListener { snapshot ->
                        val userAdvice = snapshot.data?.get(adviceLabel)?.toString()
                        adviceLiveData.value = userAdvice
                    }
        }
    }
}