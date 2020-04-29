package  com.cnr.phr_android.dashboard.monitor.disease

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.AppCalculation
import com.cnr.phr_android.dashboard.monitor.utility.entity.HealthyGroup
import com.cnr.phr_android.dashboard.monitor.utility.entity.NCDS
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import timber.log.Timber

class NCDSDisease(val disease:NCDS) {

    // Coroutine
    private val coroutineJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + coroutineJob)

    // Firebase Firestore
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val ref = firebaseFirestore.collection("health_analyze").document("disease_risk")
    private val diseaseFactor = MutableLiveData<List<VitalsignDataType>> ()

    // Class Constructor
    val diseaseName = disease
    var describe:String? = disease.label
    var imageResult:Int = R.drawable.circular_dark_btn
    var risk: RiskLevel = RiskLevel.SAFE
    private val describeLiveData = MutableLiveData<String>()

    lateinit var factor :List<VitalsignDataType>

    init {
        uiScope.launch {
            getFactorOfDisease()
            getDescription()
        }
    }

    //Public Function
    fun addWeightOrOverAge(weight:Float,height:Float) {
        val calculator = AppCalculation()
        val healthyGroup = calculator.calculateBMI(weight,height)
        if(healthyGroup == HealthyGroup.OVERSIZE || healthyGroup == HealthyGroup.OBESITY){
            addRisk()
        }
    }
    fun refreshFactor(){ factor = diseaseFactor.value!! }

   // Private Function
    private suspend fun getFactorOfDisease(){
        withContext(Dispatchers.IO){
            var tempFactor:List<VitalsignDataType>
            var dataTypeStringList:List<String>
            ref.get()
                    .addOnFailureListener { Timber.v("Not Success fetching $it") }
                    .addOnSuccessListener { snapshot->
                        Timber.v("Getting Cause of Disease from Cloud Firestore")
                        dataTypeStringList = snapshot.get(disease.short) as List<String>
                        tempFactor = getVitalSignDataTypeFromString(dataTypeStringList)
                        diseaseFactor.value = tempFactor
                        refreshFactor()
                    }
        }
    }

    fun checkAndAddRisk(dataType: VitalsignDataType,riskLevel: RiskLevel) {
        Timber.v("Value Coming on Check and Add Risk of $riskLevel")
        for (requestRiskFactor in factor) {
            if (dataType == requestRiskFactor){
                if(riskLevel == RiskLevel.WARNING)
                    addRisk()
                if(riskLevel == RiskLevel.MODERATE)
                    addMoreRisk()
                break
            }
        }
    }

    // Private function
    private fun addRisk() {
        risk = when (risk) {
            RiskLevel.UNKNOWN -> RiskLevel.WARNING
            RiskLevel.SAFE -> RiskLevel.WARNING
            RiskLevel.WARNING -> RiskLevel.MODERATE
            RiskLevel.MODERATE -> RiskLevel.DANGER
            RiskLevel.DANGER -> RiskLevel.MASSIVE
            else -> this.risk
        }
    }

    private fun addMoreRisk() {
        risk = when (risk) {
            RiskLevel.UNKNOWN -> RiskLevel.WARNING
            RiskLevel.SAFE -> RiskLevel.MODERATE
            RiskLevel.WARNING -> RiskLevel.DANGER
            RiskLevel.MODERATE -> RiskLevel.MASSIVE
            RiskLevel.DANGER -> RiskLevel.MASSIVE
            else -> this.risk
        }
    }

    private fun getVitalSignDataTypeFromString(stringList: List<String>): List<VitalsignDataType> {
        val dataTypeList = ArrayList<VitalsignDataType>()
        for (word in stringList) {
            when (word) {
                "systolic" -> dataTypeList.add(VitalsignDataType.BLOOD_PRESSURE)
                "diastolic" -> dataTypeList.add(VitalsignDataType.HEART_RATE)
                "glucose" -> dataTypeList.add(VitalsignDataType.BLOOD_GLUCOSE)
                "heart_rate" -> dataTypeList.add(VitalsignDataType.HEART_RATE)
            }
        }
        return dataTypeList
    }

    private suspend fun getDescription(){
        withContext(Dispatchers.IO){
            val firebaseLabel =  firebaseFirestore.collection("disease").document(disease.short)
            firebaseLabel.get()
                    .addOnFailureListener {
                        Timber.v("Error On Receive Data From Firestore $it")
                    }
                    .addOnSuccessListener {snapshot->
                        describeLiveData.value = snapshot["description"].toString()
                        refreshDescription()
                    }
        }
    }

    fun refreshDescription(){
        describe = describeLiveData.value
    }

}