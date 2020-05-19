package com.cnr.phr_android.dashboard.monitor.login

import android.app.Activity
import android.app.Application
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import com.cnr.phr_android.R
import com.cnr.phr_android.dashboard.monitor.utility.entity.Sex
import com.cnr.phr_android.databinding.FragmentEditPersonalDataBinding
import kotlinx.android.synthetic.main.fragment_edit_personal_data.*
import timber.log.Timber
import java.util.*


class EditPersonalDataFragment :Fragment(){
    private lateinit var  binding:FragmentEditPersonalDataBinding
    var gYear = 2001
    var gMonth = 1
    var gDate = 1  // g = Global
    var gHeight:Float? = null
    var gWeight:Float? = null
    var gSex:Sex = Sex.TBA
    var diabetes = false
    var coronary = false
    var kidney = false
    private lateinit var personalDataViewModel:EditPersonalDataViewModel
    private lateinit var datePicker:DatePicker
    private var uuid:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uuid = arguments!!.getString("userUUID")
    }

    inner class SpinnerActivity: Activity(), AdapterView.OnItemSelectedListener{
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
            gSex = when (position) {
                0 -> Sex.MALE
                1 -> Sex.FEMALE
                else -> Sex.TBA
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {

        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val application= Application()
        personalDataViewModel = EditPersonalDataViewModel(application)
        personalDataViewModel.getPersonalHealthData(uuid!!)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_personal_data,container,false)
        binding.epdToolbar.setTitleTextColor(Color.WHITE)
        binding.epdToolbar.title = "Edit Personal Data"
        getPatient()

        binding.epdToolbar.navigationIcon = resources.getDrawable(R.drawable.baseline_arrow_back_white_18dp)
        binding.epdToolbar.setNavigationOnClickListener { View.OnClickListener {
                it.findNavController().navigateUp()
        }}

        datePicker = binding.datePicker1
        val today = Calendar.getInstance()
        datePicker.init(today.get(Calendar.YEAR),today.get(Calendar.MONTH),today.get(Calendar.DAY_OF_MONTH))
        {
            _,year,month,day->
                val newMonth = month + 1
                Timber.v("You had Select $day / $newMonth / $year ")
                gDate = day
                gMonth = newMonth
                gYear = year
        }

        //spinner
        val sexSpinner: Spinner = binding.sexSpinner
        ArrayAdapter.createFromResource(this.context!!,R.array.sex_select,android.R.layout.simple_spinner_item)
                .also{adapter->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    sexSpinner.adapter = adapter
                }

        sexSpinner.onItemSelectedListener = SpinnerActivity()
        binding.epdSubmit.setOnClickListener {
            if(uuid != null){
                if(gHeight == null || gWeight == null){
                    Toast.makeText(this.context,"Please Fill in All Blank",Toast.LENGTH_LONG).show()
                }
                else {
                    val bundle = Bundle()
                    bundle.putString("userUUID",uuid)
                    coronary = binding.checkCoronary.isChecked
                    kidney = binding.checkKidney.isChecked
                    diabetes = binding.checkDiabetes.isChecked
                    personalDataViewModel.searchAndEditData(uuid!!,gDate,gMonth,gYear,gWeight!!,gHeight!!,gSex,coronary,kidney,diabetes)
                    binding.epdSubmit.findNavController().navigate(R.id.action_editPersonalDataFragment_to_infoPreview,bundle)
                }
            }
            else{
                Toast.makeText(this.context,"No User UUID Found",Toast.LENGTH_SHORT).show()
            }
            gHeight = binding.epdHeight.text.toString().toFloatOrNull()
            gWeight = binding.epdWeight.text.toString().toFloatOrNull()
            Timber.v("Birthday $gDate / $gMonth / $gYear")
            Timber.v("Weight $gWeight , Height $gHeight")
        }

        return binding.root
    }

    private fun getPatient(){
        personalDataViewModel.userData.observe(viewLifecycleOwner, android.arch.lifecycle.Observer { personalData->
            personalData?.let {
                binding.epdName.text = personalData.displayName
                binding.epdWeight.setText(personalData.weight.toString())
                binding.epdHeight.setText(personalData.height.toString())
                datePicker.updateDate(personalData.bYear,personalData.bMonth - 1,personalData.bDay)
            }
        })
    }
}