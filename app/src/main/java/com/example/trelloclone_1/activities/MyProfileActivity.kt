package com.example.trelloclone_1.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone_1.R
import com.example.trelloclone_1.databinding.ActivityMyProfileBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.User
import com.example.trelloclone_1.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.lang.Exception

class MyProfileActivity : BaseActivity() {
    private var mSelectedImageUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User
    private lateinit var binding: ActivityMyProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbMyProfileActivity)
        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            supportActionBar!!.title = "profile".uppercase()
        }
        binding.tbMyProfileActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        FirestoreClass().loadUserData(this)
        binding.civMyProfileImage.setOnClickListener{

            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding.btnMyProfileUpdate.setOnClickListener {
            if(mSelectedImageUri != null){
                uploadUserImage()
            }else{
                showProgressDialog("Please Wait")
                updateUserProfileData()
            }
        }
    }
    fun setUserDataInUI(user: User){
        mUserDetails = user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.profile_placeholder)
            .into(binding.civMyProfileImage)
        binding.etMyProfileName.setText(user.name)
        binding.etMyProfileEmail.setText(user.email)
        if(user.mobile != 0L){
            binding.etMyProfileNumber.setText(user.mobile.toString())
        }
    }
    private fun uploadUserImage(){
        showProgressDialog("please wait")
        if(mSelectedImageUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE${System.currentTimeMillis()}.${Constants.getFileExtension(this, mSelectedImageUri)}")
            sRef.putFile(mSelectedImageUri!!).addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            mProfileImageURL = uri.toString()
                            updateUserProfileData()
                        }
            }.addOnFailureListener{ exception ->
                    errorSnackBar("${exception.message}")
                    hideProgressDialog()
            }
        }
    }
    fun profileUpdateSuccessful(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if(binding.etMyProfileName.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding.etMyProfileName.text.toString()
        }
        if(binding.etMyProfileNumber.text!!.isNotEmpty() && binding.etMyProfileNumber.text.toString().toLong() != mUserDetails.mobile){
            userHashMap[Constants.MOBILE] = binding.etMyProfileNumber.text.toString().toLong()
        }
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
    /****** Request For Permission ******/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                errorSnackBar("Permission are denied from the user")
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageUri = data.data!!
            try{
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.profile_placeholder)
                    .into(binding.civMyProfileImage)
            }catch (e: Exception){
                e.printStackTrace()
                errorSnackBar("Failed!!")
            }
        }
    }
}