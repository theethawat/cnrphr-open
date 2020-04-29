package com.cnr.phr_android.dashboard.monitor.monitor_listing.analysis_tab

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by theethawat@cnr - 2020-03-05
 * */
class VitalsignAdviceRepository {
    val advice = MutableLiveData<AdviceDataSet>()
    suspend fun getAdvice(dataType: VitalsignDataType){
        val dataTypeString = getDataTypeDBReference(dataType)
        val firebaseCollection = FirebaseFirestore.getInstance().collection("vitalsign_advice")
       // val adviceDocument = getAdviceDocument(riskLevel)
        withContext(Dispatchers.IO){
            firebaseCollection.document(dataTypeString).get()
                    .addOnFailureListener {
                        Timber.v("Error on Getting Advice $it")
                    }
                    .addOnSuccessListener { snapshot->
                        Timber.v(snapshot.toString())
                        if(dataType != VitalsignDataType.BLOOD_PRESSURE){
                            val  vitalSignAdviceSet = AdviceDataSet(
                                    adviceDanger = snapshot.data!!["advice_danger"].toString(),
                                    adviceRisk = snapshot.data!!["advice_risk"].toString(),
                                    adviceSafe = snapshot.data!!["advice_safe"].toString(),
                                    averageValue = snapshot.data!!["average_value"] as HashMap<String,List<Double>>,
                                    patientDensity = snapshot.data!!["density"] as HashMap<String,List<Double>>,
                                    relateDisease = snapshot.data!!["disease"] as List<String>
                            )
                            advice.value = vitalSignAdviceSet
                        }
                        else{
                            val  vitalSignAdviceSet = AdviceDataSet(
                                    adviceDanger = snapshot.data!!["advice_danger"].toString(),
                                    adviceRisk = snapshot.data!!["advice_risk"].toString(),
                                    adviceSafe = snapshot.data!!["advice_safe"].toString(),
                                    averageValue = snapshot.data!!["average_value"] as HashMap<String,List<Double>>,
                                    averageValueDiastolic = snapshot.data!!["average_value_2"] as HashMap<String,List<Double>>,
                                    patientDensity = snapshot.data!!["density"] as HashMap<String,List<Double>>,
                                    relateDisease = snapshot.data!!["disease"] as List<String>
                            )
                            advice.value = vitalSignAdviceSet
                        }

                    }
        }
    }


    private fun getDataTypeDBReference(dataType: VitalsignDataType):String{
        return when(dataType){
            VitalsignDataType.BLOOD_PRESSURE->"blood_pressure"
            VitalsignDataType.HEART_RATE->"heart_rate"
            VitalsignDataType.BLOOD_GLUCOSE->"glucose"
            VitalsignDataType.SPO2->"spo2"
            else->"unknown"
        }
    }



}