package com.cnr.phr_android.dashboard.monitor.disease

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.NCDS
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.cnr.phr_android.dashboard.monitor.utility.entity.VitalSignPersonalList
import com.cnr.phr_android.data.user.FirebaseUser
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Created by theethawat@cnr - 2020-02-13
 * */
class DiseaseMonitorViewModel(val userUUID: String, application: Application) : AndroidViewModel(application) {

    // Monitor Repository
    private val diseaseMonitorRepository = DiseaseMonitorRepository()
    val diseaseDataList = MutableLiveData<List<NCDSDisease>>()

    // Disease
    private var hypoxiaDisease = NCDSDisease(NCDS.HYPOXIA)
    private var coronaryDisease = NCDSDisease(NCDS.CORONARY)
    private var strokeDisease = NCDSDisease(NCDS.STROKE)
    private var hypertensionDisease = NCDSDisease(NCDS.HYPERTENSION)
    private var diabetesDisease = NCDSDisease(NCDS.DIABETES)

    // VitalSign
    private var systolicRiskLevel = VitalSignRisk(VitalsignDataType.BLOOD_PRESSURE, BloodPressureDataType.SYSTOLIC)
    private var diastolicRiskLevel = VitalSignRisk(VitalsignDataType.BLOOD_PRESSURE, BloodPressureDataType.DIASTOLIC)
    private var glucoseRiskLevel = VitalSignRisk(VitalsignDataType.BLOOD_GLUCOSE)
    private var spo2RiskLevel = VitalSignRisk(VitalsignDataType.SPO2)

    // Coroutines Essential
    private val coroutineJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    // Public Data that other class can called
    val userData = MutableLiveData<FirebaseUser>()
    val personalVitalSign = MutableLiveData<VitalSignPersonalList>()

    init {
        fetchPersonalData()
        diseaseMonitorRepository.coroutineUserData.observeForever { coroutineUserData ->
            coroutineUserData?.let {
                Timber.v(">>>>>>>>>>   Fetching User Data Success <<<<<<<<<<<<<<<<")
                this.userData.value = coroutineUserData
                fetchLatestVitalSign()
            }
        }
        diseaseMonitorRepository.coroutinePersonalVitalSign.observeForever { data ->
            data?.let { personalList ->
                Timber.v(">>>>>>>>>> Loading VitalSign Data Success <<<<<<<<< ")
                personalVitalSign.value = personalList
                runAnalysing()
            }
        }
    }

    private fun fetchPersonalData() {
        runBlocking {
            diseaseMonitorRepository.fetchPersonalDataCoroutines(userUUID)
        }
    }

    private fun fetchLatestVitalSign() {
        if (userData.value?.inputProgramUser != null) {
            Timber.v("User Input Section UUID => ${userData.value!!.inputProgramUser}")
            userData.value!!.inputProgramUser?.let {
                diseaseMonitorRepository.fetchAllLatestValue(it)
            }
        }
    }

    private fun runAnalysing() {
        Timber.v("============== Analysis Running =================")
        val userLatestVitalsignData = personalVitalSign.value!!

        // Send Data For Analysis
        val systolicRisk = systolicRiskLevel.getYourDataRisk(userLatestVitalsignData.systolic!!.toInt())
        val diastolicRisk = diastolicRiskLevel.getYourDataRisk(userLatestVitalsignData.diastolic!!.toInt())
        val glucoseRisk = glucoseRiskLevel.getYourDataRisk(userLatestVitalsignData.glucose!!.toInt())
        val spo2Risk = spo2RiskLevel.getYourDataRisk(userLatestVitalsignData.spo2!!.toInt())

        val vitalSignRiskGroup: List<RiskLevel> =
                listOf(systolicRisk, diastolicRisk, glucoseRisk, spo2Risk)
        val diseaseGroup: List<NCDSDisease> = listOf(hypoxiaDisease, hypertensionDisease, coronaryDisease, strokeDisease, diabetesDisease)
        val vitalSignAndLevel: Map<RiskLevel, VitalsignDataType> =
                mapOf(spo2Risk to VitalsignDataType.SPO2,
                        diastolicRisk to VitalsignDataType.BLOOD_PRESSURE,
                        systolicRisk to VitalsignDataType.BLOOD_PRESSURE,
                        glucoseRisk to VitalsignDataType.BLOOD_GLUCOSE
                )

        for (disease in diseaseGroup) {
                for (vitalSignRisk in vitalSignRiskGroup) {
                    vitalSignAndLevel[vitalSignRisk]?.let {
                        disease.checkAndAddRisk(it, vitalSignRisk)
                    }
                }
            disease.addWeightOrOverAge(userData.value!!.weight, userData.value!!.height)
        }


        Timber.v("Your Data Analysis Risk is Ready")
        Timber.v(userLatestVitalsignData.toString())
        Timber.v("---------------------------")
        Timber.v(vitalSignRiskGroup.toString())
        Timber.v("------------------------")

        Timber.v("Hypoxia -> ${hypoxiaDisease.risk} ")
        Timber.v("Hypertension -> ${hypertensionDisease.risk} ")
        Timber.v("Diabetes -> ${diabetesDisease.risk} ")
        Timber.v("Coronary -> ${coronaryDisease.risk} ")
        Timber.v("Stroke -> ${strokeDisease.risk} ")
        Timber.v("--------------------------")

        val tempDiseaseDataList: List<NCDSDisease> = listOf(
                hypertensionDisease, hypoxiaDisease, coronaryDisease, strokeDisease, diabetesDisease
        )
        diseaseDataList.value = tempDiseaseDataList
    }

    override fun onCleared() {
        super.onCleared()
        Timber.v("-- Coroutine Cancel --")
        coroutineJob.cancel()
    }

}