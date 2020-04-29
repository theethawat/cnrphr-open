package com.cnr.phr_android.dashboard.monitor.monitor_listing.statistic_tab

import android.app.Application
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.VitalSignAndUnit
import com.cnr.phr_android.dashboard.monitor.utility.entity.MapRangeAmount
import com.cnr.phr_android.databinding.FragmentStatisticBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import timber.log.Timber

/**
 * Created by theethawat@cnr - 2020-03-23
 * */
class StatisticFragment : Fragment() {
    private lateinit var binding: FragmentStatisticBinding
    private lateinit var requestDataType: VitalsignDataType
    private lateinit var viewModel: AverageStatViewModel
    private lateinit var dataRangeAndAmount: List<MapRangeAmount>
    private lateinit var diastolicRangeAmount: List<MapRangeAmount>
    private val vitalSignUnitCalculator = VitalSignAndUnit()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestDataTypeString = arguments?.getString("dataTypeString")!!
        requestDataType = getVitalSignDataType(requestDataTypeString)
        viewModel = AverageStatViewModel(requestDataType, application = Application())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistic, container, false)
        viewModel.observeDataSetAndWeight()
        viewModel.averageStatData.observe(viewLifecycleOwner, Observer { rangeAmountList ->
            rangeAmountList?.let {
                dataRangeAndAmount = it
                Timber.v("**** Data of Range and Amount in Fragment is Observed ****")
                if (requestDataType != VitalsignDataType.BLOOD_PRESSURE) {
                    createAverageLineChart()
                }
            }
        })

        // Especially for Blood Pressure Diastolic
        if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            viewModel.diastolicStatData.observe(viewLifecycleOwner, Observer { rangeAmountList ->
                rangeAmountList?.let {
                    diastolicRangeAmount = it
                    Timber.v("**** Data of Range and Amount Diastolic in Fragment is Observed ****")
                    createAverageLineChart()
                }
            })
        }

        binding.button.setOnClickListener {
            val ageFrom = binding.numberFrom.text.toString().toInt()
            val ageTo = binding.numberTo.text.toString().toInt()
            Timber.v("User Request Age From $ageFrom to $ageTo")
            viewModel.filterAge(ageFrom, ageTo)
        }
        return binding.root
    }

    private fun createAverageLineChart() {
        val chart = binding.averageChart
        val avrEntries = ArrayList<Entry>()
        for (graphEntry in dataRangeAndAmount) {
            Timber.v("Add Entry")
            avrEntries.add(Entry(graphEntry.range, graphEntry.amount.toFloat()))
        }
        val avrDataSet: LineDataSet
        avrDataSet = if (requestDataType != VitalsignDataType.BLOOD_PRESSURE) {
            LineDataSet(avrEntries, "จำนวนผู้คนที่มีค่าอยู่ในระดับนี้")
        } else {
            LineDataSet(avrEntries, "ความดันโลหิตเมื่อหัวใจบีบตัว")
        }

        // Dataset Setting
        settingDataSetStyle(avrDataSet, 64, 224, 208)


        // Especially for Blood Pressure Diastolic
        val avrData: LineData
        avrData = if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            val diastolicEntries = ArrayList<Entry>()
            for (graphEntry in diastolicRangeAmount) {
                Timber.v("Add Entry")
                diastolicEntries.add(Entry(graphEntry.range, graphEntry.amount.toFloat()))
            }
            val diastolicDataSet = LineDataSet(diastolicEntries, "ความดันโลหิตเมื่อหัวใจคลายตัว")
            settingDataSetStyle(diastolicDataSet, 255, 192, 203)
            val dataSetCombination: List<LineDataSet> = listOf(avrDataSet, diastolicDataSet)
            LineData(dataSetCombination)
        } else {
            LineData(avrDataSet)
        }


        avrData.notifyDataChanged()
        chart.axisRight.isEnabled = false
        settingLegned(chart.legend)
        settingXAxis(chart.xAxis)
        settingYAxis(chart.axisLeft)
        chart.description.text = requestDataType.thaiName + "ของผู้ใช้งาน หน่วยเป็น " + vitalSignUnitCalculator.getUnitFromVitalSing(requestDataType).label
        chart.setDrawGridBackground(false)
        chart.data = avrData
        chart.invalidate()
    }

    private fun settingLegned(legend: Legend) {
        if (requestDataType != VitalsignDataType.BLOOD_PRESSURE) {
            legend.isEnabled = false
        }
    }

    private fun settingDataSetStyle(dataSet: LineDataSet, red: Int, green: Int, blue: Int) {
        dataSet.fillColor = Color.rgb(red, green, blue)
        dataSet.color = Color.rgb(red, green, blue)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.fillAlpha = 240
        dataSet.setDrawFilled(true)
        dataSet.lineWidth = 0.2f
    }

    private fun settingXAxis(axis: XAxis) {
        axis.setDrawLabels(true)
        axis.position = XAxis.XAxisPosition.BOTTOM
        axis.setDrawGridLines(false)
        axis.granularity = 5F
    }

    private fun settingYAxis(axis: YAxis) {
        axis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        axis.granularity = 1f
        axis.isGranularityEnabled = true
        axis.setLabelCount(10, false)
        axis.setValueFormatter { value, axis ->
            value.toInt().toString()
        }
    }

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
}