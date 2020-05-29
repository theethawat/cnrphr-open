package com.cnr.phr_android.dashboard.monitor

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.dashboard.monitor.utility.DiseaseRiskTranslator
import com.cnr.phr_android.dashboard.monitor.utility.entity.Sex
import com.cnr.phr_android.data.user.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class InfoPreviewRepository {
    val currentUser = MutableLiveData<FirebaseUser>()
    val db = FirebaseFirestore.getInstance()
    val diseaseRiskTranslate = DiseaseRiskTranslator()

    suspend fun findUserInFirestoreCoroutines(userUUID: String, userName: String) {
        val userReference = db.collection("user")
        withContext(Dispatchers.IO) {
            var userFromFirestore: FirebaseUser?
            val dataToAddUser = FirebaseUser(userName, userUUID, null)
            userReference.whereEqualTo("uuid", userUUID).limit(1).get()
                    .addOnFailureListener {
                        Timber.v("Cannot Access Cloud Firestore  $it")
                    }
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            userReference.add(dataToAddUser)
                                    .addOnFailureListener {
                                        Timber.v("Fail to Create a new user record in Database : $it ")
                                    }
                                    .addOnSuccessListener { createdDocRef ->
                                        Timber.v("Success Create new User to DB it located at id : ${createdDocRef.id} ")
                                        val createDocFetching = createdDocRef.get()
                                        createDocFetching.addOnFailureListener {
                                            Timber.v("Success Created but Fail Fetching")
                                        }
                                                .addOnSuccessListener { document ->
                                                    userFromFirestore = FirebaseUser(document.data?.get("displayName")?.toString(),
                                                            document.data?.get("uuid").toString(),
                                                            document.data?.get("inputProgramUser").toString(),
                                                            document.data?.get("bday").toString().toInt(),
                                                            document.data?.get("bMonth").toString().toInt(),
                                                            document.data?.get("bYear").toString().toInt(),
                                                            document.data?.get("weight").toString().toFloat(),
                                                            document.data?.get("height").toString().toFloat(),
                                                            document.data?.get("adminStatus").toString().toBoolean(),
                                                            document.data?.get("user_sex").toString(),
                                                            document.data?.get("coronary").toString().toBoolean(),
                                                            document.data?.get("kidney").toString().toBoolean(),
                                                            document.data?.get("diabetes").toString().toBoolean(),
                                                             diseaseRiskTranslate.getDiseaseRiskFromString(document.data?.get("isCoronary").toString()),
                                                             diseaseRiskTranslate.getDiseaseRiskFromString(document.data?.get("isHypertension").toString()),
                                                             diseaseRiskTranslate.getDiseaseRiskFromString(document.data?.get("isHypoxia").toString()),
                                                             diseaseRiskTranslate.getDiseaseRiskFromString(document.data?.get("isDiabetes").toString())
                                                    )
                                                    currentUser.value = userFromFirestore
                                                }
                                    }
                        } else {
                            for (document in snapshot) {
                                if (document == null) {
                                    Timber.v(" Document is null on this Collection")
                                } else {
                                    Timber.v("Hi We Found User ${document.data["displayName"]} with ${document.data["uuid"]}")
                                    userFromFirestore = FirebaseUser(document.data["displayName"].toString(),
                                            document.data["uuid"].toString(),
                                            document.data["inputProgramUser"].toString(),
                                            document.data["bday"].toString().toInt(),
                                            document.data["bmonth"].toString().toInt(),
                                            document.data["byear"].toString().toInt(),
                                            document.data["weight"].toString().toFloat(),
                                            document.data["height"].toString().toFloat(),
                                            document.data["adminStatus"].toString().toBoolean(),
                                            document.data["user_sex"].toString(),
                                            document.data["coronary"].toString().toBoolean(),
                                            document.data["kidney"].toString().toBoolean(),
                                            document.data["diabetes"].toString().toBoolean(),
                                            diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isCoronary"].toString()),
                                            diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isHypertension"].toString()),
                                            diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isHypoxia"].toString()),
                                            diseaseRiskTranslate.getDiseaseRiskFromString(document.data["isDiabetes"].toString())
                                    )
                                    //Adding Value
                                    currentUser.value = userFromFirestore
                                    Timber.v(userFromFirestore.toString())
                                    break
                                }
                            }
                        }

                    }
        }
    }

}