package com.cnr.phr_android.dashboard.monitor.login

import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.transition.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cnr.phr_android.R
import com.cnr.phr_android.databinding.FragmentUserSelectBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber
import java.time.Duration

/**
 * Created by theethawat@cnr - 2020-01-27
 * */
class TraditionalLoginFragment : Fragment(){

    private lateinit var binding:FragmentUserSelectBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var email:String
    private lateinit var password:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_select,container,false)
        auth = FirebaseAuth.getInstance()
        binding.upStatusText.text = "Waiting for Login"
        binding.upNext.setOnClickListener {
            email = binding.upEmail.text.toString()
            password = binding.upPassword.text.toString()
            Timber.v("Push to Sign In $email")
            signIn(email,password)
        }
        binding.upRegister.setOnClickListener{
            email = binding.upEmail.text.toString()
            password = binding.upPassword.text.toString()
            Timber.v("Push to Register In $email")
            register(email,password)
        }
        return binding.root
    }

    private fun register(email: String,password: String){
        Timber.v("Email Submit : $email")
        binding.upProgressBar.visibility = View.VISIBLE

        if(!validateForm()){
            return
        }
        if(email.isEmpty()|| password.isEmpty()){
            Snackbar.make(view!!,"Register in Unsuccessful",Snackbar.LENGTH_INDEFINITE).show()
            return
        }

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{task: Task<AuthResult> ->
                    if(task.isSuccessful){
                        // Sign In Success
                        Timber.v("Create User Success")
                        val user = auth.currentUser
                        updateUI(user)
                    }
                    else{
                        // If register or sign in fails
                        Timber.v("Create Use Fail ${task.exception} ")
                        Snackbar.make(view!!,"Register Unsuccessful",Snackbar.LENGTH_INDEFINITE).show()
                        updateUI(null)
                    }
                }
    }

    private fun signIn(email:String,password:String){
        Timber.v("Email Submit : $email")
       binding.upProgressBar.visibility = View.VISIBLE

        if(!validateForm()){
            return
        }
        if(email.isEmpty()|| password.isEmpty()){
            Snackbar.make(view!!,"Sign in Unsuccessful",Snackbar.LENGTH_INDEFINITE).show()
            return
        }
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{task: Task<AuthResult> ->
                    if(task.isSuccessful){
                        //Update User Information
                        val user = auth.currentUser
                        updateUI(user)
                    }
                    else{
                        Timber.v("Sign in With Email Error ${task.exception}")
                        Snackbar.make(view!!,"Sign in Unsuccessful",Snackbar.LENGTH_INDEFINITE).show()
                        updateUI(null)
                    }
                }
    }

    private fun signOut(){
        auth.signOut()
        updateUI(null)
    }

     override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private fun validateForm():Boolean{
        var valid = true
        val email = binding.upEmail.text.toString()
        if(TextUtils.isEmpty(email)){
            binding.upEmail.error = "Required!!"
            valid = false
        }
        else{
            binding.upEmail.error = null
        }
        val password = binding.upPassword.text.toString()
        if(TextUtils.isEmpty(password)){
            binding.upPassword.error = "Required!!"
            valid = false
        }
        else{
            binding.upPassword.error = null
        }
        return valid
    }


    private fun updateUI(user:FirebaseUser?){
        if(user!= null){
            binding.upStatusText.text = "Success Login"
            val bundle = Bundle()
            bundle.putString("userUUID",user.uid)
            bundle.putString("userName",user.email)
            findNavController().navigate(R.id.action_traditionalLoginFragment_to_infoPreview,bundle)
        }
        else{
            binding.upStatusText.text = "Not Login Now"
        }
        binding.upProgressBar.visibility = View.INVISIBLE
    }

}