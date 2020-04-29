package com.cnr.phr_android.dashboard.monitor.monitor_listing.analysis_tab

import android.app.Application
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.disease.VitalSignRisk
import com.cnr.phr_android.dashboard.monitor.utility.RiskColor
import com.cnr.phr_android.dashboard.monitor.utility.entity.BloodPressureDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskIndication
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevel
import com.cnr.phr_android.databinding.FragmentMonitorAnalysisBinding
import timber.log.Timber

/**
 * Create by theethawat@cnr - 2020-03-03
 * */
class AnalysisTabFragment : Fragment() {

    private lateinit var binding: FragmentMonitorAnalysisBinding
    private lateinit var requestDataType: VitalsignDataType
    private lateinit var userUUID: String
    private lateinit var viewModel: AnalysisTabViewModel
    private lateinit var riskAnalysis1: VitalSignRisk
    private var riskAnalysis2: VitalSignRisk? = null
    private var firstValue = 0
    private var firstValueRisk = RiskLevel.SAFE
    private val colorTool = RiskColor()
    // To keep Diastolic Value if needed
    private var secondValue = 0
    private var secondValueRisk = RiskLevel.SAFE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestDataTypeString = arguments?.getString("dataTypeString")!!
        requestDataType = getVitalSignDataType(requestDataTypeString)
        userUUID = arguments?.getString("inputUUID")!!
        Timber.v("Analysis Fragment Enter $requestDataType & UserUUID $userUUID")
        if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            riskAnalysis1 = VitalSignRisk(requestDataType, BloodPressureDataType.SYSTOLIC)
            riskAnalysis2 = VitalSignRisk(requestDataType, BloodPressureDataType.DIASTOLIC)
        } else {
            riskAnalysis1 = VitalSignRisk(requestDataType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_monitor_analysis, container, false)
        viewModel = AnalysisTabViewModel(requestDataType, userUUID, Application())
        disableNHESCard()
        runLatestFetchObserver()
        getDataUnit()
        observePopulationRanger()
        observeSpecificAnalysis()
        return binding.root
    }

    /**************** Data Referencing  **************************/
    private fun getVitalSignDataType(dataText: String): VitalsignDataType {
        Timber.v("Get VitalSign Data Type Has called ")
        return when (dataText) {
            "BloodGlucose" -> VitalsignDataType.BLOOD_GLUCOSE
            "BloodPressure" -> VitalsignDataType.BLOOD_PRESSURE
            "Spo2" -> VitalsignDataType.SPO2
            "HeartRate" -> VitalsignDataType.HEART_RATE
            else -> VitalsignDataType.BLOOD_GLUCOSE
        }
    }

    /************* ViewModel Observer ************************/
    private fun runLatestFetchObserver() {
        viewModel.firstValue.observe(viewLifecycleOwner, Observer { vitalSignValue ->
            vitalSignValue?.let {
                firstValue = it
                binding.valueFirst.text = firstValue.toString()
                firstValueRisk = riskAnalysis1.getYourDataRisk(firstValue)
                viewModel.getDataAdvice(firstValueRisk)
                observeDataAdvice()
            }
        })

        if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            viewModel.secondValue.observe(viewLifecycleOwner, Observer { vitalSignValue ->
                vitalSignValue?.let {
                    secondValue = it
                    binding.valueSecond.text = secondValue.toString()
                    secondValueRisk = riskAnalysis2!!.getYourDataRisk(secondValue)
                }
            })
        } else {
            binding.valueSecond.visibility = View.INVISIBLE
            binding.valueSlash.visibility = View.INVISIBLE
            binding.valueRiskLevel2.visibility = View.INVISIBLE
            binding.valueSlash2.visibility = View.INVISIBLE
        }
    }

    private fun observeDataAdvice() {
        viewModel.dataAdvice.observe(viewLifecycleOwner, Observer { advice ->
            advice?.let {
                binding.valueRiskDescribe.text = it
                binding.valueRiskLevel.text = firstValueRisk.thaiLabel
                binding.valueRiskLevel.setTextColor(colorTool.getRiskColor(firstValueRisk))
                if(requestDataType == VitalsignDataType.BLOOD_PRESSURE){
                    binding.valueRiskLevel2.text = secondValueRisk.thaiLabel
                    binding.valueRiskLevel2.setTextColor(colorTool.getRiskColor(secondValueRisk))
                }
            }
        })
    }

    private fun observePopulationRanger() {
        // Observe Patient Density
        viewModel.patientDensity.observe(viewLifecycleOwner, Observer { range ->
            range?.let { populationRange ->
                binding.valuePercent.text = populationRange.toString()
                if (populationRange.toString().toFloat() > 40) {
                    binding.valuePercent.setTextColor(Color.RED)
                }
                binding.percentRangeBar.progress = populationRange.toInt()
            }
        })

        // Observe Average Value
        viewModel.averageValue.observe(viewLifecycleOwner, Observer { mean ->
            mean?.let {
                binding.averageValue1.text = it.toString()
                compareAndAddRiskArrow(it)
                if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
                    //TBA
                } else {
                    binding.averageValue2.visibility = View.INVISIBLE
                    binding.averageValueSlash.visibility = View.INVISIBLE
                }
            }
        })

        // Observe Diastolic Blood pressure Value
        viewModel.averageValue2.observe(viewLifecycleOwner, Observer { mean ->
            mean?.let {
                binding.averageValue2.text = it.toString()
            }
        })

    }

    private fun observeSpecificAnalysis() {
        viewModel.adviceOnDisease.observeForever { diseaseAdvice ->
            diseaseAdvice?.let { advice ->
                binding.valueProbabilityResult.text = advice
                setSpecificAnalyseColor(viewModel.diseaseIndicator)
                // Disease Probability 0  is main Probability while 1,2 is for support
                Timber.v("Disease Main Indicator in Fragment is => ${viewModel.diseaseIndicator[0]}")
                binding.diseaseMainProb.text = viewModel.diseaseIndicator[0].indication
                binding.diseaseProbability1.text = viewModel.diseaseIndicator[1].indication
                binding.diseaseProbability2.text = viewModel.diseaseIndicator[2].indication
            }
        }
    }

    /********** UI Setting **************/

    private fun disableNHESCard() {
        // These 2 VitalSign have not on National Health Exam Survey
        if (requestDataType == VitalsignDataType.SPO2 || requestDataType == VitalsignDataType.HEART_RATE) {
            binding.cardPercent.visibility = View.GONE
            binding.cardSpecificData.visibility = View.GONE
        }
    }

    private fun getDataUnit() {
        when (requestDataType) {
            VitalsignDataType.BLOOD_PRESSURE -> {
                binding.valueUnit.text = getText(R.string.unit_mmhg)
                binding.valueUnit2.text = getText(R.string.unit_mmhg)
            }
            VitalsignDataType.BLOOD_GLUCOSE -> {
                binding.valueUnit.text = getText(R.string.unit_mg_per_decilit)
                binding.valueUnit2.text = getText(R.string.unit_mg_per_decilit)
            }
            VitalsignDataType.HEART_RATE -> {
                binding.valueUnit.text = getText(R.string.unit_bpm)
                binding.valueUnit2.text = getText(R.string.unit_bpm)
            }
            VitalsignDataType.SPO2 -> {
                binding.valueUnit.text = getText(R.string.unit_percent)
                binding.valueUnit2.text = getText(R.string.unit_percent)
            }
            else -> {
                binding.valueUnit.text = ""
                binding.valueUnit2.text = ""
            }
        }
    }

    /********** UI Decoration and Arrow Indicator **************/

    private fun compareAndAddRiskArrow(averageValue:Float){
        val arrowUp:Drawable = ResourcesCompat.getDrawable(context!!.resources,R.drawable.expand_less,null)!!
        val arrowDown:Drawable = ResourcesCompat.getDrawable(context!!.resources,R.drawable.expand_more,null)!!
        if(averageValue >= firstValue){
            binding.titleArrowImage.setImageDrawable(arrowDown)
        }
        else if(averageValue < firstValue){
            binding.titleArrowImage.setImageDrawable(arrowUp)
        }
    }


    private fun setSpecificAnalyseColor(indicator: List<RiskIndication>) {
        val positiveImg: Drawable = ResourcesCompat.getDrawable(this.context!!.resources, R.drawable.thumb_up_black_18dp, null)!!
        val negativeImg: Drawable = ResourcesCompat.getDrawable(this.context!!.resources, R.drawable.thumb_down_black_18dp, null)!!
        val diseaseProbTextView = listOf(binding.diseaseProbability1, binding.diseaseProbability2)
        val checkImgView = listOf(binding.checkDiseaseProb1, binding.checkDiseaseProb2)
        for (index in 0..1) {
            if (indicator[index+1].dangerLevel == RiskLevel.SAFE || indicator[index+1].dangerLevel == RiskLevel.SUBSTANDARD) {
                checkImgView[index].setImageDrawable(positiveImg)
            } else if (indicator[index+1].dangerLevel == RiskLevel.DANGER || indicator[index+1].dangerLevel == RiskLevel.WARNING ||
                    indicator[index+1].dangerLevel == RiskLevel.MODERATE || indicator[index+1].dangerLevel == RiskLevel.MASSIVE) {
                checkImgView[index].setImageDrawable(negativeImg)
            } else {
                diseaseProbTextView[index].visibility = View.GONE
                checkImgView[index].visibility = View.GONE
            }
        }
    }


}