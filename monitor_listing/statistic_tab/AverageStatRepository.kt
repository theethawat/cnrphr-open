package com.cnr.phr_android.dashboard.monitor.monitor_listing.statistic_tab

import android.arch.lifecycle.MutableLiveData
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.MapRangeAmount
import com.cnr.phr_android.dashboard.monitor.utility.entity.MapValueAge
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Created by theethawat@cnr - 2020-03-14
 * */

class AverageStatRepository(private val vitalsignDataType: VitalsignDataType,private val bloodPressureSpecificType:BloodPressureDataType = BloodPressureDataType.NO) {
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private val firestore = FirebaseFirestore.getInstance()
    private var dataMax: Int = 0
    private var dataMin: Int = 0
    private val fetchedDataSet = MutableLiveData<List<MapValueAge>>()
    val exportedDataSet = MutableLiveData<List<MapRangeAmount>>()

    init {
        val label = getDataLabel(vitalsignDataType)
        uiScope.launch {
            fetchedDataSet.value = fetchDataFromFireStore(label)
        }
        android.os.Handler().postDelayed({
            fetchedDataSet.observeForever { dataSet ->
                dataSet?.let { data ->
                    Timber.v("We Found Data Set !!!!")
                    Timber.v(data.toString())
                    weightingData(fetchedDataSet.value!!)
                }
            }
        }, 2000)
         }

    fun filterAge(initial:Int,finish:Int){
        Timber.v("In Repository, we got initial and finish with range $initial - $finish")
        if(fetchedDataSet.value != null){
            Timber.v("Fetched Data Set is Not Null")
            val dataSet = fetchedDataSet.value
            val filteredDataSet = dataSet!!.filter { it.age in initial..finish }
            weightingData(filteredDataSet)
        }
    }

    private suspend fun fetchDataFromFireStore(label: String): List<MapValueAge> {
        val valueAgeDataArray = ArrayList<MapValueAge>()
        var first = true
        return withContext(Dispatchers.IO) {
            firestore.collection(label).get()
                    .addOnFailureListener {
                        Timber.v("Error found at Average Stat Repository $it")
                    }
                    .addOnSuccessListener { snapshot ->
                        for (document in snapshot) {
                            val vitalSignValue: Int = getValueFromVitalSignType(document)
                            if (first) {
                                dataMin = vitalSignValue
                                first = false
                            }
                            // Find Minimum and Maximum for manage graph to 6 bar graph
                            dataMin = minOf(vitalSignValue, dataMin)
                            dataMax = maxOf(vitalSignValue, dataMax)
                            val ageValue: Int = document["age"].toString().toInt()
                            val ageAndValueMap = MapValueAge(ageValue, vitalSignValue)
                            valueAgeDataArray.add(ageAndValueMap)
                        }
                    }
            valueAgeDataArray
        }
    }

    private fun weightingData(dataSet:List<MapValueAge>) {
        Timber.v("Weight Data ---- (Trying Use Kotlin Sort Group)")
        val dataListGroup = dataSet.sortedBy { it.vitalSignValue }.ifEmpty { listOf(MapValueAge(0,0)) }.groupBy { it.vitalSignValue }
        val dataWeightGroup = ArrayList<MapRangeAmount>()
        dataListGroup.forEach{dataItem->
            val dataItemKey = dataItem.key
            val dataItemAmount = dataItem.value.count()
            val tempDataWeight = MapRangeAmount(dataItemKey.toFloat(),dataItemAmount)
            Timber.v("Render data to graph get => $tempDataWeight")
            dataWeightGroup.add(tempDataWeight)
        }
        if(dataWeightGroup.isNotEmpty()){
            exportedDataSet.value = dataWeightGroup
        }

    }


    private fun getValueFromVitalSignType(doc: QueryDocumentSnapshot): Int {
        return when (vitalsignDataType) {
            VitalsignDataType.SPO2 -> {
                Timber.v(doc.data["pulseOximeter"].toString())
                doc.data["pulseOximeter"].toString().toInt()}
            VitalsignDataType.BLOOD_PRESSURE -> {
                when (bloodPressureSpecificType) {
                    BloodPressureDataType.SYSTOLIC -> doc.data["systolic"].toString().toFloat().toInt()
                    BloodPressureDataType.DIASTOLIC -> doc.data["diastolic"].toString().toFloat().toInt()
                    else -> doc.data["systolic"].toString().toFloat().toInt()
                }
            }
            VitalsignDataType.HEART_RATE -> doc.data["age"].toString().toInt()
            VitalsignDataType.BLOOD_GLUCOSE -> (doc.data["value"].toString().toFloat() * 100000).toInt()
            else -> 0
        }
    }

    private fun getDataLabel(dataType: VitalsignDataType): String {
        return when (dataType) {
            VitalsignDataType.SPO2 -> "spo2"
            VitalsignDataType.BLOOD_GLUCOSE -> "glucose"
            VitalsignDataType.HEART_RATE -> "heart_rate"
            VitalsignDataType.BLOOD_PRESSURE -> "blood_pressure"
            else -> ""
        }
    }

}