package com.cnr.phr_android.dashboard.monitor.monitor_listing

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.Color.WHITE
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cnr.phr_android.R
import com.cnr.phr_android.base.user.VitalsignDataType
import com.cnr.phr_android.dashboard.monitor.utility.entity.RiskLevelTemplate
import com.cnr.phr_android.data.entity.*
import com.cnr.phr_android.databinding.FragmentMonitorMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber
import kotlin.collections.ArrayList

/**
 * Created by theethawat@cnr - 2020-01-06
 * Reference about bundle passing
 *   //https://medium.com/incwell-innovations/passing-data-in-android-navigation-architecture-component-part-2-5f1ebc466935
 * Create ViewPager Reference
 *  https://www.javatpoint.com/kotlin-android-tablayout-with-viewpagers
 *
 * */
class MonitorMainFragment : Fragment() {

    lateinit var application: Application

    private lateinit var requestDataType: VitalsignDataType
    private lateinit var binding: FragmentMonitorMainBinding
    private lateinit var viewModelFactory: MonitorViewModelFactory
    private lateinit var viewModel: AndroidViewModel
    private lateinit var userUUID: String
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var riskBoundaryData: RiskLevelTemplate
    private lateinit var bpBoundaryDiastolic: RiskLevelTemplate

    private lateinit var requestDataTypeString: String
    private var tempDataList = ArrayList<DeviceRoomData>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createTabLayout()
        createViewPager()
    }

    private fun createTabLayout() {
        tabLayout = view!!.findViewById(R.id.monitor_section_tab)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Add Argument From Fragment Bundle
        requestDataTypeString = arguments?.getString("requestDataType")!!
        requestDataType = VitalsignDataType.BLOOD_GLUCOSE
        requestDataType = getVitalSignDataType(requestDataTypeString)
        userUUID = arguments?.getString("userUUID")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        application = requireNotNull(this.activity).application
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_monitor_main, container, false)
        viewModelFactory = MonitorViewModelFactory(requestDataType, userUUID, application)
        viewModel = getViewModelFromFactory(viewModelFactory)
        binding.monitorToolbar.title = requestDataType.thaiName
        binding.monitorToolbar.setTitleTextColor(WHITE)

        observeGraphData()
        // observeRiskLevelData()
        return binding.root
    }

    private fun observeGraphData(){
        (viewModel as MonitorMainViewModel).readyStatus.observe(viewLifecycleOwner, Observer { ready ->
            ready?.let {
                Timber.v("Ready is not null")
                if(ready == true){
                    tempDataList = (viewModel as MonitorMainViewModel).allData.value as ArrayList<DeviceRoomData>
                    Timber.v("On Observer Data Observed!!!")
                    Timber.v(tempDataList.toString())
                    if(requestDataType == VitalsignDataType.BLOOD_PRESSURE){
                        riskBoundaryData = (viewModel as MonitorMainViewModel).firstSignRiskLevel.value!!
                        bpBoundaryDiastolic =(viewModel as MonitorMainViewModel).secondSignRiskLevel.value!!
                    }
                    else{
                        riskBoundaryData = (viewModel as MonitorMainViewModel).firstSignRiskLevel.value!!
                    }
                    settingChart()
                }
            }
//            Handler().postDelayed({
//                if (tempDataList.isNotEmpty()) {
//                    settingChart()
//                }
//            }, 2000)
        })
    }

    // Spare function
    private fun observeRiskLevelData(){
        /***** GETTING RISK LEVEL DATA*******************/
        if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            (viewModel as MonitorMainViewModel).secondSignRiskLevel.observe(viewLifecycleOwner, Observer { riskLevel ->
                riskLevel?.let {
                    riskBoundaryData = (viewModel as MonitorMainViewModel).firstSignRiskLevel.value!!
                    bpBoundaryDiastolic = it
                    settingChart()
                }
            })
        }
        else{
            (viewModel as MonitorMainViewModel).firstSignRiskLevel.observe(viewLifecycleOwner, Observer { riskLevel ->
                riskLevel?.let {
                    Timber.v("RiskLevelBoundary is Set !!!!")
                    riskBoundaryData = it
                    settingChart()
                }
            })
        }
    }

    private fun settingChart() {
        Timber.v("--------------Chart Setting-----------")
        val chart = binding.monitorChart
        val lineData = settingLineChart(tempDataList)
        //Axis
        setXAxis(chart)
        setYAxis(chart)
        //legend
        val legend = chart.legend
        legend.isEnabled = false
        //Chart
        chart.data = lineData
        chart.setBorderColor(Color.LTGRAY)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.description.isEnabled = false
        chart.invalidate()
    }

    private fun setXAxis(chart: LineChart) {
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.axisLineColor = Color.LTGRAY
        xAxis.axisLineWidth = 2F
        xAxis.setDrawLabels(false)
        xAxis.spaceMin = 0.2F
        xAxis.spaceMax = 0.5F
      //  xAxis.axisMinimum = 0F
    }

    private fun setYAxis(chart: LineChart) {
        val yAxis = chart.axisLeft
        val yAxisRight = chart.axisRight
        var firstUpLimitLine = LimitLine(riskBoundaryData.dangerMin.toFloat(), "ระดับอันตราย")
        var firstLowLimitLine = LimitLine(riskBoundaryData.riskMin.toFloat(), "ระดับเสี่ยง")

        // SPO 2 is much is great !!!
        if (requestDataType == VitalsignDataType.SPO2) {
            firstUpLimitLine = LimitLine(riskBoundaryData.dangerMin.toFloat(), "ระดับอันตราย")
        }
        if (requestDataType == VitalsignDataType.SPO2) {
            firstLowLimitLine = LimitLine(riskBoundaryData.riskMin.toFloat(), "ระดับเสี่ยง")
        }

        // For Blood Pressure,there are systolic and diastolic will
        if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            firstLowLimitLine.label = "ระดับเสี่ยงของ Systolic"
            firstUpLimitLine.label = "ระดับอันตรายของ Systolic"

            // It must use the second line
            val secondUpLimitLine = LimitLine(bpBoundaryDiastolic.dangerMin.toFloat(), "ระดับอันตรายของ Diastolic")
            val secondLowLimitLine = LimitLine(bpBoundaryDiastolic.riskMin.toFloat(), "ระดับเสี่ยงของ Diastolic")

            secondUpLimitLine.lineColor = Color.RED
            secondUpLimitLine.textSize = 12f
            secondUpLimitLine.enableDashedLine(4f, 2f, 2f)
            yAxis.addLimitLine(secondUpLimitLine)

            secondLowLimitLine.lineColor = Color.parseColor("#FFD700")
            secondLowLimitLine.textSize = 12f
            secondLowLimitLine.enableDashedLine(4f, 2f, 2f)
            yAxis.addLimitLine(secondLowLimitLine)
        }

        // Above one Line Setting
        firstUpLimitLine.lineColor = Color.RED
        firstUpLimitLine.textSize = 12f
        firstUpLimitLine.enableDashedLine(4f, 2f, 2f)

        // Below one Line Setting
        firstLowLimitLine.lineColor = Color.parseColor("#FFD700")
        firstLowLimitLine.textSize = 12f
        firstLowLimitLine.enableDashedLine(4f, 2f, 2f)

        yAxis.addLimitLine(firstUpLimitLine)
        yAxis.addLimitLine(firstLowLimitLine)
        yAxis.setDrawLabels(false)
        if (requestDataType == VitalsignDataType.SPO2) {
            // Add More Space below the line
            yAxis.spaceBottom = 90F
            yAxisRight.spaceBottom = 90F
        } else {
            yAxis.spaceTop = 30F
            yAxisRight.spaceTop = 30F
            yAxis.spaceBottom = 30F
            yAxisRight.spaceBottom = 30F
        }

    }


    // Setting Value of Data
    private fun settingLineChart(dataList: List<DeviceRoomData>): LineData {
        val entries = ArrayList<Entry>() // If it be Blood Pressure it will be systolic value
        val bpSupportEntries = ArrayList<Entry>()
        var index = 1
        val firstDataSet: LineDataSet
        var secondDataSet: LineDataSet? = null
        val reverseDataList = dataList.asReversed()
        if (requestDataType == VitalsignDataType.BLOOD_PRESSURE) {
            for (dataToGraph in reverseDataList) {
                Timber.v("Entry add to graph")
                entries.add(Entry(index.toFloat(), (dataToGraph as BloodPressure).systolic!!.toFloat()))
                bpSupportEntries.add(Entry(index.toFloat(), (dataToGraph as BloodPressure).diastolic!!.toFloat()))
                index++
            }
            firstDataSet = LineDataSet(entries, "Systolic")
            secondDataSet = LineDataSet(bpSupportEntries, "Diastolic")
        } else {
            for (dataToGraph in reverseDataList) {
                Timber.v("Entry add to graph")
                entries.add(Entry(index.toFloat(), getGraphValue(dataToGraph)))
                index++
            }
            firstDataSet = LineDataSet(entries, "")
        }

        //Setting Graph Configuration
        firstDataSet.mode = LineDataSet.Mode.LINEAR
        firstDataSet.setCircleColor(Color.MAGENTA)
        firstDataSet.lineWidth = 2F
        firstDataSet.fillColor = Color.rgb(255, 20, 147)
        firstDataSet.fillAlpha = 128
        firstDataSet.setDrawFilled(true)
        firstDataSet.setColor(Color.MAGENTA, 2)
        return if (secondDataSet != null) {
            secondDataSet.mode = LineDataSet.Mode.LINEAR
            secondDataSet.setCircleColor(Color.CYAN)
            secondDataSet.lineWidth = 2F
            secondDataSet.fillColor = Color.rgb(64, 224, 208)
            secondDataSet.fillAlpha = 255
            secondDataSet.setDrawFilled(true)
            secondDataSet.setColor(Color.CYAN, 2)
            val dataSetCombination: List<LineDataSet> = listOf(firstDataSet, secondDataSet)
            LineData(dataSetCombination)
        } else {
            LineData(firstDataSet)
        }
    }

    private fun getGraphValue(data: DeviceRoomData): Float {
        if (data is Spo2) {
            return data.pulseOximeter!!.toFloat()
        }
        if (data is HeartRate) {
            return data.value!!.toFloat()
        }
        if (data is BloodGlucose) {
            return data.value!!.toFloat()
        } else {
            throw IllegalArgumentException("Data Type is not Support!!")
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

    private fun getViewModelFromFactory(factory: MonitorViewModelFactory): AndroidViewModel {
        return ViewModelProviders.of(this, factory).get(MonitorMainViewModel::class.java)
    }

    private fun createViewPager() {
        viewPager = view!!.findViewById(R.id.list_monitor_page_view)
        val monitorPageAdapter = MonitorPageAdapter(activity!!.supportFragmentManager, tabLayout.tabCount, userUUID, requestDataTypeString)
        viewPager.adapter = monitorPageAdapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

}

operator fun FirebaseFirestoreSettings.invoke(settings: FirebaseFirestoreSettings) {

}
