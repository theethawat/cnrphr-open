package com.cnr.phr_android.dashboard.monitor


import android.app.Application
import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.*
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.cnr.phr_android.R
import com.cnr.phr_android.dashboard.DashboardActivity
import com.cnr.phr_android.dashboard.monitor.monitor_listing.invoke
import com.cnr.phr_android.dashboard.monitor.utility.AppCalculation
import com.cnr.phr_android.data.user.FirebaseUser
import com.cnr.phr_android.data.user.UserDiseaseRisk
import com.cnr.phr_android.databinding.FragmentInfoPreviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber

/**
 * Create by theethawat@cnr  2020-01-17
 * */
class InfoPreview : Fragment() {
    lateinit var binding: FragmentInfoPreviewBinding

    // Owner Patient as a main user
    private var ownerUser: FirebaseUser? = null

    // Variable Declare
    private lateinit var application: Application
    private lateinit var infoPreviewViewModel: InfoPreviewViewModel
    private lateinit var userUUID: String
    private lateinit var userName: String
    private val wildCardUUID: String = "dd829d6d-894d-4f02-8f9a-d076dee6bdf8"
    private val appCalculation = AppCalculation()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Timestamp Setting
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings(settings)
        setHasOptionsMenu(true)

        // Receive Argument Bundle
        processArgument(arguments!!)
        Timber.v("---========> User UUID From Google Auth is $userName with $userUUID <========-------")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        Timber.v("Initial Application on View Creates")
        application = requireNotNull(this.activity).application
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info_preview, container, false)
        infoPreviewViewModel = InfoPreviewViewModel(userUUID, userName, application)
        infoPreviewViewModel.findUserFromFirestore()

        // Control Handle
        setUIBasicComponent()
        setUIMajorButton()
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        infoPreviewViewModel.currentUserCoruetines.observe(this, Observer { currentUser ->
            currentUser?.let {
                ownerUser = it
                Timber.v("Owner User is Ready $ownerUser")
                getUserVitalSignDataDisplay()
                checkIfPersonalDataInitiate()
            }
        })
    }


    private fun getUserVitalSignDataDisplay() {
        Timber.v("Get UserVitalSignDataDisplay has Called on Fragment ")
        infoPreviewViewModel.userVitalSignList.observe(this, Observer { personalList ->
            personalList?.let { personalData ->
                binding.valueDiastolic.text = personalData.diastolic.toString()
                binding.valueSystolic.text = personalData.systolic.toString()
                binding.valueGlucose.text = personalData.glucose.toString()
                binding.valueSpo2.text = personalData.spo2.toString()
                binding.valueHeartrate.text = personalData.heartRate.toString()
            }
        })
        infoPreviewViewModel.userObesityDescribe.observe(this, Observer { personalObesity ->
            personalObesity?.let {
                binding.userWeightDescribe.text = personalObesity
                setViewVisibility()
                setDisplayPersonalData()
            }
        })
    }

    private fun processArgument(arguments: Bundle) {
        userUUID = if (arguments["userUUID"] != null) {
            Timber.v("Argument UserUUID is not null")
            (arguments.getString("userUUID")!!)
        } else {
            wildCardUUID
        }
        userName = if (arguments["userName"] != null) {
            (arguments.getString("userName")!!)
        } else {
            "User Not Found"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.new_nav_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.bt_logout -> {
                logoutFirestore()
                true
            }
            R.id.bt_edit_profile -> {
                val bundle = Bundle()
                bundle.putString("userUUID", userUUID)
                findNavController().navigate(R.id.action_infoPreview_to_editPersonalDataFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkIfPersonalDataInitiate() {
        // To Check Weather birth day and birth month is 0 that it can't happen in real life
        // Save this for show that user has not initial his data to move to edit data page
        if (ownerUser!!.bDay == 0 && ownerUser!!.bMonth == 0) {
            val bundle = Bundle()
            Timber.v("User Birthday ${ownerUser!!.bDay}")
            bundle.putString("userUUID", userUUID)
            findNavController().navigate(R.id.action_infoPreview_to_editPersonalDataFragment, bundle)
        }
    }


    private fun setUIMajorButton() {
        var bundle: Bundle
        binding.btMeasure.setOnClickListener {
            val inputUserUUID = if (ownerUser!!.inputProgramUser == null) {
                null
            } else {
                ownerUser!!.inputProgramUser
            }
            val intent = Intent(this.context, DashboardActivity::class.java).apply {
                Timber.v("Before Send Intent userUUID: $userUUID  $inputUserUUID")
                putExtra("userUUID2", inputUserUUID)
                putExtra("firebaseUserUUID", userUUID)
            }
            startActivity(intent)
        }
        binding.appEditProfile.setOnClickListener {
            bundle = Bundle()
            bundle.putString("userUUID", userUUID)
            binding.appEditProfile.findNavController().navigate(R.id.action_infoPreview_to_editPersonalDataFragment, bundle)
        }
    }

    private fun logoutFirestore() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_infoPreview_to_traditionalLoginFragment2)
    }

    private fun setUIBasicComponent() {
        var bundle: Bundle
        binding.frontToolbar.title = "Personal Health Connected"
        binding.frontToolbar.setTitleTextColor(Color.WHITE)
        binding.frontToolbar.inflateMenu(R.menu.new_nav_menu)
        binding.frontToolbar.setOnMenuItemClickListener { menuItem: MenuItem? ->
            when {
                menuItem!!.itemId == R.id.bt_logout -> {
                    logoutFirestore()
                    true
                }
                menuItem.itemId == R.id.bt_edit_profile -> {
                    val menuBundle = Bundle()
                    menuBundle.putString("userUUID", userUUID)
                    findNavController().navigate(R.id.action_infoPreview_to_editPersonalDataFragment, menuBundle)
                    true
                }
                else -> {3
                    true
                }
            }
        }
        binding.userName.text = "Waiting"
        binding.appSpo2.setOnClickListener {
            bundle = Bundle()
            bundle.putString("requestDataType", "Spo2")
            bundle.putString("userUUID", ownerUser!!.inputProgramUser)
            binding.appSpo2.findNavController().navigate(R.id.action_infoPreview_to_monitorMainFragment, bundle)
        }
        binding.appBloodGlucose.setOnClickListener {
            bundle = Bundle()
            bundle.putString("requestDataType", "BloodGlucose")
            bundle.putString("userUUID", ownerUser!!.inputProgramUser)
            binding.appBloodGlucose.findNavController().navigate(R.id.action_infoPreview_to_monitorMainFragment, bundle)
        }
        binding.appBloodPressure.setOnClickListener {
            bundle = Bundle()
            bundle.putString("requestDataType", "BloodPressure")
            bundle.putString("userUUID", ownerUser!!.inputProgramUser)
            binding.appBloodPressure.findNavController().navigate(R.id.action_infoPreview_to_monitorMainFragment, bundle)
        }
        binding.appHeartRate.setOnClickListener {
            bundle = Bundle()
            bundle.putString("requestDataType", "HeartRate")
            bundle.putString("userUUID", ownerUser!!.inputProgramUser)
            binding.appHeartRate.findNavController().navigate(R.id.action_infoPreview_to_monitorMainFragment, bundle)
        }
    }


    private fun setDisplayPersonalData() {
        settingDiseaseStatus()
        binding.userName.text = ownerUser!!.displayName
        binding.userHeight.text = ownerUser!!.height.toString()
        binding.userWeight.text = ownerUser!!.weight.toString()
        binding.userAge.text = appCalculation.calculateAge(ownerUser!!.bDay, ownerUser!!.bMonth, ownerUser!!.bYear).toString()
        binding.userWeightResult.text = appCalculation.calculateBMI(ownerUser!!.weight, ownerUser!!.height).thaiName
    }

    private fun settingDiseaseStatus() {
        val userProperties = ownerUser!!
        selectAndSetDiseaseRiskIcon(binding.iconHypertension, userProperties.isHypertension)
        selectAndSetDiseaseRiskIcon(binding.iconCoronary, userProperties.isCoronary)
        selectAndSetDiseaseRiskIcon(binding.iconHypoxia, userProperties.isHypoxia)
        selectAndSetDiseaseRiskIcon(binding.iconDiabetes, userProperties.isDiabetes)
    }

    private fun selectAndSetDiseaseRiskIcon(resource: ImageView, diseaseDangerLevel: UserDiseaseRisk) {
        val positiveImg: Drawable = ResourcesCompat.getDrawable(this.context!!.resources, R.drawable.thumb_up_black_18dp, null)!!
        val negativeImg: Drawable = ResourcesCompat.getDrawable(this.context!!.resources, R.drawable.thumb_down_black_18dp, null)!!
        val warningImg: Drawable = ResourcesCompat.getDrawable(this.context!!.resources, R.drawable.icon_warning, null)!!
        when (diseaseDangerLevel) {
            UserDiseaseRisk.SAFE -> {
                resource.setImageDrawable(positiveImg)
            }
            UserDiseaseRisk.RISK -> {
                resource.setImageDrawable(warningImg)
            }
            UserDiseaseRisk.DANGER -> {
                resource.setImageDrawable(negativeImg)
            }
            else -> {
                resource.setImageDrawable(positiveImg)
            }
        }
    }

    private fun setViewVisibility() {
        binding.appEditProfile.visibility = View.VISIBLE
        binding.appProgressBar.visibility = View.GONE
        binding.appHeartRate.visibility = View.VISIBLE
        binding.appBloodPressure.visibility = View.VISIBLE
        binding.appBloodGlucose.visibility = View.VISIBLE
        binding.appSpo2.visibility = View.VISIBLE
        binding.cardWeight.visibility = View.VISIBLE
        binding.cardDisease.visibility = View.VISIBLE
        binding.iconHypertension.visibility = View.VISIBLE
        binding.iconHypoxia.visibility = View.VISIBLE
        binding.iconCoronary.visibility = View.VISIBLE
        binding.iconDiabetes.visibility = View.VISIBLE
        binding.titleHypertension.visibility = View.VISIBLE
        binding.titleCoronary.visibility = View.VISIBLE
        binding.titleHypoxia.visibility = View.VISIBLE
        binding.titleDiabetes.visibility = View.VISIBLE
    }

}



