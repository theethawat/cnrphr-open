package com.cnr.phr_android.dashboard.monitor.monitor_listing.analysis_tab

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.disease.DiseaseMonitorRepository
import com.cnr.phr_android.dashboard.monitor.disease.VitalSignRisk
import com.cnr.phr_android.dashboard.monitor.utility.AppCalculation
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskIndication
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.cnr.phr_android.dashboard.monitor.utility.entity.Sex
import com.cnr.phr_android.dashboard.monitor.vitalsign_analyser.BPAndHypertensionRepository
import com.cnr.phr_android.dashboard.monitor.vitalsign_analyser.GlucoseAndDiabetesRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by theethawat@cnr - 2020-03-04
 * */

class AnalysisTabViewModel(val dataType: VitalsignDataType, val userUUID: String, application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val monitorRepository = DiseaseMonitorRepository()
    private val adviceRepository = VitalsignAdviceRepository()
    private val appCalculation = AppCalculation()
    private lateinit var riskAnalysis1:VitalSignRisk
    private lateinit var riskAnalysis2:VitalSignRisk
    private var riskLevelOf1:RiskLevel? = null
    private var riskLevelOf2:RiskLevel? = null
    private lateinit var adviceSet: AdviceDataSet
    private lateinit var userPersonalData: com.cnr.phr_android.data.user.FirebaseUser

    // Exported Data
    val firstValue = MutableLiveData<Int>()
    val secondValue = MutableLiveData<Int>()
    val dataAdvice = MutableLiveData<String>()
    val adviceOnDisease = MutableLiveData<String>()
    var diseaseIndicator: List<RiskIndication> = emptyList()
    val patientDensity = MutableLiveData<Float>()
    val averageValue = MutableLiveData<Float>()
    val averageValue2 = MutableLiveData<Float>()

    // Gender and Age in Database not direct method to connect
    private var userAge = 0
    private var sex = Sex.TBA

    init {
        initialRiskAnalysis()
        fetchLatestValueInDataType()
        observeVitalSignData()
        populatePersonalData()
        observePersonalData()
    }

    private fun initialRiskAnalysis(){
        if (dataType == VitalsignDataType.BLOOD_PRESSURE) {
            riskAnalysis1 = VitalSignRisk(dataType, BloodPressureDataType.SYSTOLIC)
            riskAnalysis2 = VitalSignRisk(dataType, BloodPressureDataType.DIASTOLIC)
        } else {
            riskAnalysis1 = VitalSignRisk(dataType)
        }
    }

    private fun observeRiskLevel(){
        Timber.v("Observe Risk Level Data ")
        if(dataType == VitalsignDataType.BLOOD_PRESSURE){
            Timber.v("Observe Available for Blood Pressure")
            riskAnalysis1.riskLevelExport.observeForever { riskLevel->
                riskLevel?.let{
                    riskLevelOf1 = it
                    getDataAdvice(it)
                }
            }
            riskAnalysis2.riskLevelExport.observeForever { riskLevel->
                riskLevel?.let{
                    riskLevelOf2 = it
                    getDataAdvice(it)
                }
            }
        }
        else{
            Timber.v("Observe Available for Data that's not Blood Pressure ")
            riskAnalysis1.riskLevelExport.observeForever { riskLevel->
                riskLevel?.let{
                    riskLevelOf1 = it
                    getDataAdvice(it)
                }
            }
        }
        Timber.v("Success Observe Data From VitalSignRisk")
    }


    /******* LATEST DATA *******/
    private fun fetchLatestValueInDataType() {
        uiScope.launch {
            when (dataType) {
                VitalsignDataType.HEART_RATE -> {
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "heart_rate")
                }
                VitalsignDataType.BLOOD_GLUCOSE -> {
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "glucose")
                    // BP in Diabetes
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "systolic")
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "diastolic")
                }
                VitalsignDataType.SPO2 -> {
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "spo2")
                }
                VitalsignDataType.BLOOD_PRESSURE -> {
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "systolic")
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "diastolic")
                    // Fot Diabetes Hypertension Detection
                    monitorRepository.fetchLatestVitalSignCoroutines(userUUID, "glucose")
                }
                else -> throw  IllegalArgumentException()
            }
        }
    }

    private fun observeVitalSignData() {
        if (dataType != VitalsignDataType.BLOOD_PRESSURE) {
            observeSingleData()
        } else {
            observeDoubleData()
        }
        observeRiskLevel()
    }

    private fun observeSingleData() {
        val vitalSignObserver = when (dataType) {
            VitalsignDataType.SPO2 -> monitorRepository.spo2
            VitalsignDataType.BLOOD_GLUCOSE -> monitorRepository.glucose
            VitalsignDataType.HEART_RATE -> monitorRepository.heartRate
            else -> TODO("Make default type implementation")
        }
        vitalSignObserver.observeForever { dataValue ->
            dataValue?.let {
                firstValue.value = it
                riskAnalysis1.getYourDataRisk(it)
            }
        }
    }

    private fun observeDoubleData() {
        monitorRepository.systolic.observeForever { systolic ->
            systolic?.let {
                firstValue.value = it
                riskAnalysis1.getYourDataRisk(it)
            }
        }
        monitorRepository.diastolic.observeForever { diastolic ->
            diastolic?.let {
                secondValue.value = it
                riskAnalysis2.getYourDataRisk(it)
            }
        }
    }

    /******  GET ADVICE  *****/
    fun getDataAdvice(vitalSignRiskLevel: RiskLevel) {
        uiScope.launch {
            adviceRepository.getAdvice(dataType)
            observeAdvice(vitalSignRiskLevel)
        }
    }

    private fun observeAdvice(riskLevel: RiskLevel) {
        Timber.v("Get Advice is now Subscribe")
        adviceRepository.advice.observeForever { advice ->
            Timber.v("Observation of Advice")
            advice?.let { receiveAdviceSet ->
                Timber.v("Get Advice Set")
                val adviceValue = getAdviceFromRiskLevel(riskLevel, receiveAdviceSet)
                dataAdvice.value = adviceValue
                adviceSet = receiveAdviceSet
                runAnalysisModule()
            }
        }
    }

    private fun getAdviceFromRiskLevel(riskLevel: RiskLevel, receiveAdviceSet: AdviceDataSet): String {
        return when (riskLevel) {
            RiskLevel.SUBSTANDARD -> receiveAdviceSet.adviceSafe
            RiskLevel.SAFE -> receiveAdviceSet.adviceSafe
            RiskLevel.MODERATE -> receiveAdviceSet.adviceDanger
            RiskLevel.MASSIVE -> receiveAdviceSet.adviceDanger
            RiskLevel.DANGER -> receiveAdviceSet.adviceDanger
            RiskLevel.WARNING -> receiveAdviceSet.adviceRisk
            RiskLevel.UNKNOWN -> receiveAdviceSet.adviceSafe
        }
    }

    /************  PERSONAL DATA ***********/
    private fun populatePersonalData() {
        uiScope.launch {
            monitorRepository.fetchPersonalDataFromInputSectionUser(userUUID)
        }
    }

    private fun observePersonalData() {
        monitorRepository.coroutineUserData.observeForever { personalData ->
            personalData?.let { info ->
                sex = appCalculation.transfromStringToSex(info.userSex)
                userAge = appCalculation.calculateAge(info.bDay, info.bMonth, info.bYear)
                userPersonalData = info
            }
        }
    }


    /*********** DATA ANALYSIS **************/

    private fun runAnalysisModule() {
        getAgeRangePopulation()
        selectSpecificTool()
    }

    private fun getAgeRangePopulation() {
        Timber.v("User Gender $sex age $userAge")
        val ageIndex = getAgeArrayIndex()
        Timber.v("On Get Age Range Population")
        Timber.v(adviceSet.toString())
        val averageValue = adviceSet.averageValue
        val densityValue = adviceSet.patientDensity

        val averageValueOnSex = if (sex == Sex.MALE) {
            averageValue["men"]
        } else {
            averageValue["women"]
        }

        val densityValueOnSex = if (sex == Sex.MALE) {
            Timber.v("Select Man")
            densityValue["men"]
        } else {
            Timber.v("Select Women")
            densityValue["women"]
        }
        Timber.v(averageValueOnSex.toString())
        Timber.v(ageIndex.toString())

        if (densityValueOnSex != null) {
            Timber.v(densityValueOnSex[ageIndex].toString())
            this.patientDensity.value = densityValueOnSex[ageIndex].toFloat()
        }

        if (averageValueOnSex != null) {
            Timber.v(averageValueOnSex[ageIndex].toString())
            this.averageValue.value = averageValueOnSex[ageIndex].toFloat()
        }

        if (dataType == VitalsignDataType.BLOOD_PRESSURE) {
            val averageValue2 = adviceSet.averageValueDiastolic
            val averageValue2OnSex = if (sex == Sex.MALE) {
                averageValue2["men"]
            } else {
                averageValue2["women"]
            }
            //Return Diastolic BP
            if (averageValue2OnSex != null) {
                Timber.v(averageValue2OnSex[ageIndex].toString())
                this.averageValue2.value = averageValue2OnSex[ageIndex].toFloat()
            }
        }
    }

    private fun getAgeArrayIndex(): Int {
        return when {
            userAge <= 29 -> 0
            userAge in 30..44 -> 1
            userAge in 45..59 -> 2
            userAge in 60..69 -> 3
            userAge in 70..79 -> 4
            else -> 5
        }
    }

    private fun selectSpecificTool() {
        if (dataType == VitalsignDataType.BLOOD_PRESSURE) {
            if (firstValue.value == null || secondValue.value == null || monitorRepository.glucose.value == null) {
                Timber.v("Waiting")
            } else {
                val bpAndHypertensionRepo = BPAndHypertensionRepository(firstValue.value!!, secondValue.value!!, monitorRepository.glucose.value!!)
                bpAndHypertensionRepo.addDiabetesFromUserProfile(userPersonalData.diabetes)
                bpAndHypertensionRepo.addCardioVascularFromUserProfile(userPersonalData.coronary)
                bpAndHypertensionRepo.confirmAndRun()
                bpAndHypertensionRepo.adviceLiveData.observeForever { advice ->
                    advice?.let {
                        diseaseIndicator = bpAndHypertensionRepo.exportRiskIndication()
                        adviceOnDisease.value = it
                    }
                }
            }

        } else if (dataType == VitalsignDataType.BLOOD_GLUCOSE) {
            if (firstValue.value == null || monitorRepository.systolic.value == null || monitorRepository.diastolic.value == null) {
                Timber.v("Waiting")
            } else {
                val glucoseDiabetesRepo = GlucoseAndDiabetesRepository(firstValue.value!!, monitorRepository.systolic.value!!, monitorRepository.diastolic.value!!, userPersonalData.diabetes)
                glucoseDiabetesRepo.adviceLiveData.observeForever { advice ->
                    advice?.let {
                        diseaseIndicator = glucoseDiabetesRepo.exportRiskIndication()
                        adviceOnDisease.value = it
                    }
                }
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}