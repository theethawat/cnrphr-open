package com.cnr.phr_android.dashboard.monitor.login

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.cnr.phr_android.ActivityMain
import com.cnr.phr_android.R
import com.cnr.phr_android.databinding.FragmentUserSelectBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.signin.SignIn
import com.google.android.gms.signin.SignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import timber.log.Timber
import java.lang.RuntimeException
// Not Work and Not Use!!!
/**
 * Created by theethawat@cnr - 2020-01-26
 * Adapted from https://github.com/firebase/quickstart-android/blob/
 * bf928f5b7385637bf14fd91505429322951d3914/auth/app/src/main/java/com/google
 * /firebase/quickstart/auth/kotlin/GoogleSignInActivity.kt#L73-L91
 * */

//class GoogleLoginFragment :Fragment(){
//
//    private lateinit var binding:FragmentUserSelectBinding
//    private lateinit var googleSignInClient: GoogleSignInClient
//    private lateinit var auth:FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//        val context = context
//        try {
//            googleSignInClient = GoogleSignIn.getClient(this.requireActivity(),gso)
//        }
//        catch (e:RuntimeException) {
//            Timber.v("Error !! Exception : $e")
//            findNavController().navigate(R.id.action_traditionalLoginFragment_to_infoPreview)
//        }
//
//        //googleSignInClient = GoogleSignIn.getClient(parentFragment!!.activity!!,gso)
//        auth = FirebaseAuth.getInstance()
//    }
//
//    private fun signIn(){
//     val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent,RC_SIGN_IN)
//    }
//
//    //Launching intent from sign in
//     public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == RC_SIGN_IN){
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try{
//                //Google Sign in successful connect to firebase auth
//                val account = task.getResult(ApiException::class.java)
//                firebaseAuthWithGoogle(account!!)
//            }catch (e:ApiException){
//                Timber.v("Google Sign in Fail $e")
//                updateUI(null)
//            }
//         }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//    }
//
//    private fun firebaseAuthWithGoogle(acct:GoogleSignInAccount){
//        Timber.v("Google Sign In Account $acct")
//        //Get Credential
//        val credential = GoogleAuthProvider.getCredential(acct.idToken,null)
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener{task: Task<AuthResult> ->
//                    if(task.isSuccessful){
//                        Timber.v("Sign In With Credential Successful")
//                        val user = auth.currentUser
//                        updateUI(user)
//                    }
//                    else{
//                        Timber.v("Sign in With Credential Fail ${task.exception}")
//                        updateUI(null)
//                    }
//                }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//     //   binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_select,container,false)
//        binding.signInButton.setOnClickListener {
//           signIn()
//        }
//        binding.upStatusText.text="No User Sign-In"
//        return binding.root
//    }
//
//    companion object{
//        private const val TAG = "GoogleActivity"
//        private const val RC_SIGN_IN = 9001
//    }
//
//    private fun updateUI(user:FirebaseUser?){
//        if(user != null){
//            binding.upStatusText.text = user.email
//            binding.upNext.visibility = View.VISIBLE
//            binding.upNext.setOnClickListener {
//                binding.upNext.findNavController().navigate(R.id.action_loginFragment_to_infoPreview)
//            }
//        }
//        else{
//            binding.upStatusText.text ="Signing In Error"
//        }
//    }
//}