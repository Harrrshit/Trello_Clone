package com.example.trelloclone_1.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone_1.R
import com.example.trelloclone_1.databinding.ActivityCreateBoardBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.Board
import com.example.trelloclone_1.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private var mSelectedImageUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbCreateBoard)
        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Create Board"
        }
        binding.tbCreateBoard.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.tbCreateBoard.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.civMyProfileImage.setOnClickListener {
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
        binding.btnBoardCreate.setOnClickListener {
            if(mSelectedImageUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog("Please Wait")
                createBoard()
            }
        }
    }
    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())
        val board = Board(
            binding.etBoardName.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignedUsersArrayList
        )
        FirestoreClass().createBoard(this, board)
    }
    private fun uploadBoardImage(){
        showProgressDialog("Creating your board!")
        if(mSelectedImageUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("BOARD_IMAGE${System.currentTimeMillis()}.${Constants.getFileExtension(this, mSelectedImageUri)}")
            sRef.putFile(mSelectedImageUri!!).addOnSuccessListener {
                    taskSnapshot ->
                hideProgressDialog()
                Log.e(
                    "Log Response" ,
                    ""+taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e(
                            "" + Constants.TAG,
                            "" + uri.toString()
                        )
                        mBoardImageUrl = uri.toString()
                        createBoard()
                    }
            }.addOnFailureListener{
                    exception ->
                errorSnackBar("${exception.message}")
                hideProgressDialog()
            }
        }
    }
    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

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
                    .with(this@CreateBoardActivity)
                    .load(mSelectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.profile_placeholder)
                    .into(binding.civMyProfileImage)
            }catch (e: IOException){
                e.printStackTrace()
                errorSnackBar("Failed!!")
            }
        }
    }
}