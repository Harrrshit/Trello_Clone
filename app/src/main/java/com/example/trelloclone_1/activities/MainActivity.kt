package com.example.trelloclone_1.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone_1.R
import com.example.trelloclone_1.adapters.BoardItemsAdapter
import com.example.trelloclone_1.databinding.ActivityMainBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.Board
import com.example.trelloclone_1.models.User
import com.example.trelloclone_1.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{
    companion object{
        private const val MY_PROFILE_REQ_CODE: Int = 11
        private const val CREATE_BOARD_REQUEST_CODE: Int  = 12
    }
    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolBarMain: Toolbar = findViewById(R.id.tb_appBarMain)
        setSupportActionBar(toolBarMain)
        toolBarMain.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        toolBarMain.setNavigationOnClickListener {
               toggleDrawer()
        }
        FirestoreClass().loadUserData(this, true)

        binding.nvNavigation.setNavigationItemSelectedListener(this)

        val fabMainButton: FloatingActionButton = findViewById(R.id.fab_mainButton)
        fabMainButton.setOnClickListener{
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
        mSharedPreferences = this.getSharedPreferences(Constants.TRELLO_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if(tokenUpdated){
            showProgressDialog(Constants.PLEASE_WAIT)
            FirestoreClass().loadUserData(this, true)
        }else{
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) { token ->
                updateFCMToken(token)
            }
        }
    }
    private fun toggleDrawer(){
         if(binding.dlDrawer.isDrawerOpen(GravityCompat.START)){
             binding.dlDrawer.closeDrawer(GravityCompat.START)
         }else{
             binding.dlDrawer.openDrawer(GravityCompat.START)
         }
    }
    override fun onBackPressed() {
         if(binding.dlDrawer.isDrawerOpen(GravityCompat.START)){
             binding.dlDrawer.closeDrawer(GravityCompat.START)
         }else{
             doubleBackToExit()
         }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
       Log.e("Item", "" + item.itemId)
         when(item.itemId){
             R.id.menu_profile ->{
                 startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQ_CODE)
             }
             R.id.menu_logout ->{
                 FirebaseAuth.getInstance().signOut()
                 mSharedPreferences.edit().clear().apply()
                 val intent = Intent(this@MainActivity, IntroActivity::class.java)
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                 startActivity(intent)
                 finish()
             }
         }
         binding.dlDrawer.closeDrawer(GravityCompat.START)
         return true
     }
    fun populateBoardsListToUi(boardList: ArrayList<Board>){
        hideProgressDialog()
        val mainContentNoBoardsTV: TextView = findViewById(R.id.mainContentNoBoardsTV)
        val mainContentRV: RecyclerView = findViewById(R.id.mainContentRV)
        if(boardList.size > 0){
            mainContentRV.visibility = View.VISIBLE
            mainContentNoBoardsTV.visibility = View.GONE

            mainContentRV.layoutManager = LinearLayoutManager(this)
            mainContentRV.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            mainContentRV.adapter = adapter
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(applicationContext, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })
        }else{
            mainContentRV.visibility = View.GONE
            mainContentNoBoardsTV.visibility = View.VISIBLE
        }
    }

    /***updating navigation drawer user details****/
    fun updateNavigationUserDetails(user: User, readBoardList: Boolean){
        hideProgressDialog()
        val civNavImage: CircleImageView = findViewById(R.id.civ_navImage)
        val tvNavUserName: TextView = findViewById(R.id.tv_navUserName)
       mUserName = user.name
       Glide
           .with(this@MainActivity)
           .load(user.image)
           .centerCrop()
           .placeholder(R.drawable.profile_placeholder)
           .into(civNavImage)
       tvNavUserName.text = user.name
        if(readBoardList){
            showProgressDialog("Please Wait")
            FirestoreClass().getBoardListActivity(this)
        }
    }
    fun tokenUpdateSuccessful(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().loadUserData(this, true)
    }
    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQ_CODE){
            Log.e("Hello", "Result Ok")
            FirestoreClass().loadUserData(this, true)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().loadUserData(this, true)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }
}