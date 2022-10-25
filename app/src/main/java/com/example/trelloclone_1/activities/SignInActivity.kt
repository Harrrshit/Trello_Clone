package com.example.trelloclone_1.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.example.trelloclone_1.R
import com.example.trelloclone_1.databinding.ActivitySignInBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbSignIn)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            actionBar.title = "sign in".uppercase()
        }
        binding.tbSignIn.setNavigationOnClickListener {
            doubleBackToExit()
        }
        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }
    }
    private fun signInRegisteredUser(){
        val email: String = binding.etEmail.text.toString().trim{it <= ' '}
        val password: String = binding.etPassword.text.toString().trim{it <= ' '}
        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.progressBar_text))
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    FirestoreClass().loadUserData(this)
                } else {
                    Log.e(TAG, "signInWithEmail:failure", task.exception)
                    errorSnackBar("${task.exception?.message}")
                }
            }
        }
    }
    private fun validateForm(email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(email) ->{
                errorSnackBar("Please enter a Name")
                return false
            }
            TextUtils.isEmpty(password) ->{
                errorSnackBar("Please enter a password")
                return false
            }else ->{
                return true
            }
        }
    }
    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        startActivity(intent)
        finishAffinity()
    }
}