package com.example.trelloclone_1.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone_1.R
import com.example.trelloclone_1.adapters.MemberListAdapter
import com.example.trelloclone_1.databinding.ActivityMembersBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.Board
import com.example.trelloclone_1.models.User
import com.example.trelloclone_1.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMemberList: ArrayList<User>
    private var anyChangesMade: Boolean = false
    private lateinit var binding: ActivityMembersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMembersBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMembersActivity)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            supportActionBar?.title = "members".uppercase()
        }
        binding.toolbarMembersActivity.setNavigationOnClickListener {
            onBackPressed()

        }
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails= intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            Log.e("mboardDetailsMA", mBoardDetails.toString())
        }
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().getAssignedMemberListDetails(this, mBoardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member ->{
                dialogSearchMember()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setUpMemberList(list: ArrayList<User>){
        mAssignedMemberList = list
        hideProgressDialog()
        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        val adapter = MemberListAdapter(this, list)
        binding.rvMembersList.adapter = adapter
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMembersToBoard(this, mBoardDetails, user)
    }
    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMemberList.add(user)
        anyChangesMade = true
        setUpMemberList(mAssignedMemberList)
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        var dialogSearchTextInput: TextInputEditText = dialog.findViewById(R.id.dialogSearchMemberTIET)
        var dialogSearchAddTV: TextView = dialog.findViewById(R.id.dialogSearchMemberAddTV)
        var dialogSearchCancelTV: TextView = dialog.findViewById(R.id.dialogSearchMemberCancelTV)
        val email = dialogSearchTextInput.text
        dialogSearchAddTV.setOnClickListener {
            if(email.toString().isNotEmpty()){
                dialog.dismiss()
                FirestoreClass().getMemberDetails(this, email.toString())
            }else {
                Toast.makeText(this, "Please enter an email id", Toast.LENGTH_SHORT).show()
            }
        }
        dialogSearchCancelTV.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    private inner class SendNotificationToUserAsyncTask(
        val boardName: String,
        val token: String
    ): AsyncTask<Any, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(Constants.PLEASE_WAIT)
        }
        override fun doInBackground(vararg params: Any?): String {
            var result: String = ""
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val writer = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board: $boardName")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the board by ${mAssignedMemberList[0].name}"
                )
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)
                writer.writeBytes(jsonRequest.toString())
                writer.flush()
                writer.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }
            }catch(e: SocketTimeoutException){
                result = "Socket timeout"
            }catch(e: Exception){
                result = "ERROR: ${e.message}"
            }finally {
                connection!!.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }
    }
}