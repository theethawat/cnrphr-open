package com.cnr.phr_android.dashboard.monitor.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.widget.Toast
import com.cnr.phr_android.dashboard.monitor.utility.entity.Sex
import com.cnr.phr_android.data.user.FirebaseUser
import com.cnr.phr_android.data.user.PersonalData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.*
import timber.log.Timber
/**
 * Edit by theethawat@cnr - 2020-03-13
 * */
class EditPersonalDataViewModel(application: Application) : AndroidViewModel(application) {
    val db = FirebaseFirestore.getInstance()
    private val firebaseReference = db.collection("user")
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val userData = MutableLiveData<FirebaseUser?>()

    fun getPersonalHealthData(userUUID: String){
        uiScope.launch {
           checkAndGetData(userUUID)
        }
    }

    private suspend fun checkAndGetData(userUUID:String){
       withContext(Dispatchers.IO){
            var healthDataOnCoroutines:FirebaseUser
            firebaseReference.whereEqualTo("uuid",userUUID).limit(1).get()
                    .addOnFailureListener {
                        Timber.v("Cannot check if personal health data is available or not ")
                        Timber.v(it.toString())
                    }
                    .addOnSuccessListener {snapshot->
                        Timber.v("Success Listenter")
                            for(document in snapshot){
                                healthDataOnCoroutines = FirebaseUser(
                                        uuid = document.data["uuid"].toString(),
                                        displayName = document.data["displayName"].toString(),
                                        bDay =  toIntOrZero(document.data["bday"].toString()),
                                        bMonth = toIntOrZero( document.data["bmonth"].toString()),
                                        bYear =  toIntOrZero(document.data["byear"].toString()),
                                        height = toFloatOrZero(document.data["height"].toString()),
                                        weight = toFloatOrZero(document.data["weight"].toString())
                                )
                                userData.value = healthDataOnCoroutines
                            }
                        }
                    }
       }

    private fun toIntOrZero(number:String):Int{
        return if (number.toIntOrNull() == null){
            0
        }
        else{
            number.toInt()
        }
    }

    private fun toFloatOrZero(number:String):Float{
        return if (number.toFloatOrNull() == null){
            0F
        }
        else{
            number.toFloat()
        }
    }
    fun searchAndEditData(userUUID: String, date: Int, month: Int, year: Int, weight: Float, height: Float,sex:Sex = Sex.TBA,coronary:Boolean,kidney:Boolean,diabetes:Boolean) {
       uiScope.launch {
            getPersonalDataSnapshot(userUUID, date, month, year, weight, height,sex,coronary,kidney,diabetes)
        }
    }

    private suspend fun getPersonalDataSnapshot(inputUserUUID: String, date: Int, month: Int, year: Int, weight: Float, height: Float,sex:Sex = Sex.TBA,coronary:Boolean,kidney:Boolean,diabetes:Boolean) {
        withContext(Dispatchers.IO) {
            val personalDataCollection = getPersonalDataCollection(inputUserUUID, date, month, year, weight, height,sex,coronary,kidney,diabetes)
            Timber.v("Input User UUID : $inputUserUUID")
            firebaseReference.whereEqualTo("uuid", inputUserUUID).limit(1).get()
                    .addOnSuccessListener { document ->
                        Timber.v("Get Document Snapshot !!@!!@!!@@@!!!! ")
                        Timber.v(document.toString())
                        if (document.isEmpty) {
                            firebaseReference.add(personalDataCollection)
                                    .addOnFailureListener {
                                        Timber.v("Fail Error : $it ")
                                    }
                                    .addOnSuccessListener {
                                        Timber.v("Document has written to id ${it.id}")
                                    }
                        } else {
                            for (doc in document) {
                                Timber.v(doc.toString())
                                if(doc == null){
                                    Timber.v("No Doc in Documents")
                                }
                                Timber.v("Go to get PersonalDataSnapshot context")
                                val docReference = firebaseReference.document(doc.id)
                                docReference.update(personalDataCollection)
                                        .addOnSuccessListener {
                                            Timber.v("Success Update the Database!!!!")
                                        }
                                        .addOnFailureListener {
                                            Timber.v("Fail update Database $it")
                                        }
                                Timber.v("ID: ${doc.id} , Data: ${doc.data}")
                            }
                        }
                    }
                    .addOnFailureListener {
                        Timber.v("Error $it")
                    }
        }
    }

    private fun getPersonalDataCollection(uuid: String, date: Int, month: Int, year: Int, weight: Float, height: Float,sex:Sex = Sex.TBA,coronary:Boolean,kidney:Boolean,diabetes:Boolean): HashMap<String, Any> {
        return hashMapOf(
                "bday" to date,
                "bmonth" to month,
                "byear" to year,
                "height" to height,
                "weight" to weight,
                "patientUUID" to uuid,
                "user_sex" to sex.label,
                "coronary" to coronary,
                "kidney" to kidney,
                "diabetes" to diabetes
        )
    }



}