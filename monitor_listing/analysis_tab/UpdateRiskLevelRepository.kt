package com.cnr.phr_android.dashboard.monitor.monitor_listing.analysis_tab

import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.disease.NCDSDisease
import com.cnr.phr_android.dashboard.monitor.utility.entity.NCDS
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import timber.log.Timber

//  Create by theethawat@cnr - 2020-05-29
class UpdateRiskLevelRepository {

    private val firestoreRef = FirebaseFirestore.getInstance()
    val uiScope = CoroutineScope(Job() + Dispatchers.Main)

    suspend fun updateDatabaseRiskLevel(userUUID: String, disease: NCDS, riskLevel: RiskLevel) {
        val dataTypeField = dataTypeFieldTranslation(disease)
        val riskIndicator = riskAndLetterTranslator(riskLevel)
        withContext(Dispatchers.IO) {
            firestoreRef.collection("user").whereEqualTo("inputProgramUser", userUUID).limit(1).get()
                    .addOnFailureListener { err ->
                        Timber.v("Error Found on Fetch document to update risk $err")
                    }
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            Timber.v("No User Data Found")
                        } else {
                            uiScope.launch { settingDataOnID(snapshot.first().id,dataTypeField,riskIndicator) }
                        }
                    }
        }
    }

    private suspend fun settingDataOnID(docID: String, diseaseType: String, riskIndicator: String) {
        withContext(Dispatchers.IO) {
            firestoreRef.collection("user").document(docID).update(
                    mapOf(
                            diseaseType to riskIndicator
                    )
            )
                    .addOnFailureListener { err ->
                        Timber.v("Error on Putting new RiskLevel Data $err ")
                    }
                    .addOnSuccessListener {
                        Timber.v("Success Putting New RiskLevelData to Database")
                    }
        }
    }

    private fun dataTypeFieldTranslation(dataType: NCDS): String {
        //TODO(" More than 1 Factor")
        return when (dataType) {
            NCDS.DIABETES -> "isDiabetes"
            NCDS.HYPERTENSION -> "isHypertension"
            NCDS.CORONARY -> "isCoronary"
            NCDS.HYPOXIA -> "isHypoxia"
            else -> TODO("Not Implement")
        }
    }

    private fun riskAndLetterTranslator(riskLevel: RiskLevel): String {
        return when (riskLevel) {
            RiskLevel.DANGER -> "D"
            RiskLevel.MASSIVE -> "D"
            RiskLevel.MODERATE -> "D"
            RiskLevel.SAFE -> "S"
            RiskLevel.SUBSTANDARD -> "S"
            RiskLevel.UNKNOWN -> "S"
            RiskLevel.WARNING -> "R"
        }
    }

}