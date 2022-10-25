package com.example.trelloclone_1.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.trelloclone_1.R
import com.example.trelloclone_1.databinding.ActivitySignUpBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbSignUp)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            actionBar.title = "sign up".uppercase()
        }
        binding.tbSignUp.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }
    fun userRegisteredSuccess(){
        Toast.makeText(
            this,"You have successfully registered",
            Toast.LENGTH_SHORT
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
    private fun registerUser(){
        val name: String = binding.etName.text.toString().trim{it <= ' '}
        val email: String = binding.etEmail.text.toString().trim{it <= ' '}
        val password: String = binding.etPassword.text.toString().trim{it <= ' '}
        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.progressBar_text))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fireBaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = fireBaseUser.email!!
                        val user = User(fireBaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    } else {
                        hideProgressDialog()
                        errorSnackBar("${task.exception!!.message}")
                    }
                }
        }
    }
    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                errorSnackBar("Please enter a Name")
                return false
            }
            TextUtils.isEmpty(email) ->{
                errorSnackBar("Please enter a Email")
                return false
            }
            TextUtils.isEmpty(password) ->{
                errorSnackBar("Please enter a Password")
                return false
            }else ->{
                return true
            }
        }
    }
}