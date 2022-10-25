package com.example.trelloclone_1.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloclone_1.R
import com.example.trelloclone_1.adapters.CardMembersListItemsAdapter
import com.example.trelloclone_1.databinding.ActivityCardDetailsAcivityBinding
import com.example.trelloclone_1.dialogs.LabelColorListDialog
import com.example.trelloclone_1.dialogs.MembersListDialog
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.*
import com.example.trelloclone_1.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsAcivityBinding
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition: Int = -1
    private var mCardListPosition: Int = -1
    private lateinit var cardName: String
    private var mSelectedColor = ""
    private var mMembersDetailsList: ArrayList<User>? = null
    private var mSelectedDueDateMS: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCardDetailsAcivityBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getIntentData()
        cardName = mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].name
        mSelectedDueDateMS = mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].dueDate
        if(mSelectedDueDateMS > 0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMS))
            binding.tvSelectDueDate.text = selectedDate
        }
        setSupportActionBar(binding.toolbarCardDetailsActivity)
        var actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            actionBar.title = cardName
        }
        binding.toolbarCardDetailsActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.etNameCardDetails.setText(cardName)
        binding.btnUpdateCardDetails.setOnClickListener {
            if(binding.etNameCardDetails.text.toString().isNotEmpty()){
                updateCardDetails()
            }
            else
                Toast.makeText(this, "please enter a name", Toast.LENGTH_SHORT).show()
        }
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }
        mSelectedColor = mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }
        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        binding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }
        setUpSelectedMemberList()
    }
    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, - 1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailsList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard(cardName)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun updateCardDetails(){
        val card = Card(
            cardName,
            mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMS
        )
        Log.e("responseCDA", card.toString())
        val taskList: ArrayList<Task> = mBoardDetails.taskLists
        taskList.removeAt(taskList.size - 1)
        mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition] = card
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    private fun deleteCard(){
        val cardList: ArrayList<Card> = mBoardDetails.taskLists[mTaskListPosition].cards
        cardList.removeAt(mCardListPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskLists
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardList
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    private fun colorsList(): ArrayList<String>{
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#80ed99")
        colorsList.add("#4895ef")
        colorsList.add("#ff9f1c")
        colorsList.add("#a3c4f3")
        colorsList.add("#a594f9")
        colorsList.add("#9bf6ff")
        colorsList.add("#f4e285")
        colorsList.add("#96bbbb")
        return colorsList
    }
    private fun setColor(){
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
    private fun labelColorsListDialog(){
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            "Select a color",
            mSelectedColor
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }
    private fun membersListDialog(){
        val cardAssignedMembersList = mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].assignedTo
        if(cardAssignedMembersList.size > 0){
            for(i in mMembersDetailsList!!.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailsList!![i].id == j){
                        mMembersDetailsList!![i].selected = true
                    }
                }
            }
        }else{
            for(i in mMembersDetailsList!!.indices){
                mMembersDetailsList!![i].selected = false
            }
        }
        val listDialog = object : MembersListDialog(
            this,
            mMembersDetailsList!!,
            "select Members"
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(user.id)){
                        mBoardDetails
                            .taskLists[mTaskListPosition]
                            .cards[mCardListPosition]
                            .assignedTo.add(user.id)
                    }
                }else {
                    mBoardDetails
                        .taskLists[mTaskListPosition]
                        .cards[mCardListPosition]
                        .assignedTo.remove(user.id)
                    for(i in mMembersDetailsList!!.indices){
                        if(mMembersDetailsList!![i].id == user.id){
                            mMembersDetailsList!![i].selected = false
                        }
                    }
                }
                setUpSelectedMemberList()
            }

        }
        listDialog.show()
    }
    @SuppressLint("StringFormatInvalid")
    private fun alertDialogForDeleteCard(cardName: String){
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.yes)){
                dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)){
                dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun setUpSelectedMemberList(){
        val cardAssignedMembersList = mBoardDetails.taskLists[mTaskListPosition].cards[mCardListPosition].assignedTo

        val selectedMemberList: ArrayList<SelectedMembers> = ArrayList()
        for(i in mMembersDetailsList!!.indices){
            for(j in cardAssignedMembersList){
                if(mMembersDetailsList!![i].id == j){
                    val selectedMember = SelectedMembers(
                        mMembersDetailsList!![i].id,
                        mMembersDetailsList!![i].image
                    )
                    selectedMemberList.add(selectedMember)

                }
            }
        }
        if(selectedMemberList.size > 0){
            selectedMemberList.add(SelectedMembers("", ""));
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(
                this, 6
            )
            val adapter = CardMembersListItemsAdapter(
                this,
                selectedMemberList,
                true
            )
            binding.rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(
                object : CardMembersListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }

                }
            )
        }else{
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }
    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this,
            R.style.datePickerTheme,
            DatePickerDialog.OnDateSetListener{_, year, monthOfTheYear, dayOfTheMonth ->
                val sDayOfMonth = if(dayOfTheMonth < 10) "0$dayOfTheMonth" else "$dayOfTheMonth"
                val sMonthOfYear = if((monthOfTheYear + 1) < 10) "0$monthOfTheYear" else "$monthOfTheYear"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding.tvSelectDueDate.text = selectedDate
                Log.e("responseCDA", selectedDate)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMS = theDate!!.time
            },
            year,
            month,
            day
        )
        DatePickerDialog.THEME_DEVICE_DEFAULT_LIGHT
        dpd.show()
    }
}