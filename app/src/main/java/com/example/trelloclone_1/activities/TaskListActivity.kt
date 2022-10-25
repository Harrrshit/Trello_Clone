package com.example.trelloclone_1.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone_1.R
import com.example.trelloclone_1.adapters.TaskListItemsAdapter
import com.example.trelloclone_1.databinding.ActivityTaskListBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.example.trelloclone_1.models.Board
import com.example.trelloclone_1.models.Card
import com.example.trelloclone_1.models.Task
import com.example.trelloclone_1.models.User
import com.example.trelloclone_1.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDocumentId: String
    lateinit var mAssignedMembersDetailsList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().getBoardDetails(this, mBoardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQ_CODE || requestCode == CARD_DETAILS_REQ_CODE){
            showProgressDialog(Constants.PLEASE_WAIT)
            FirestoreClass().getBoardDetails(this, mBoardDocumentId)
        }else{
            Log.e("responseTLA", "hello")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBER_REQ_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setActionBar(){
        setSupportActionBar(binding.toolbarTaskListActivity)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            supportActionBar?.title = mBoardDetails.name
        }
        binding.toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun boardDetails(board: Board){
        mBoardDetails = board
        hideProgressDialog()
        setActionBar()

        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().getAssignedMemberListDetails(this, mBoardDetails.assignedTo)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }
    fun createTaskList(taskListName: String){
        val task = Task(taskListName,  FirestoreClass().getCurrentUserId())
        mBoardDetails.taskLists.add(0, task)
        mBoardDetails.taskLists.removeAt(mBoardDetails.taskLists.size - 1)
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun  updateTaskList(position: Int, listName: String, model: Task){
        val task = Task(listName, model.createdBy, model.cards)
        mBoardDetails.taskLists[position] = task
        mBoardDetails.taskLists.removeAt(mBoardDetails.taskLists.size - 1)
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
        Log.e("responseUpdateTLAfter", model.toString())

    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskLists.removeAt(position)
        mBoardDetails.taskLists.removeAt(mBoardDetails.taskLists.size - 1)
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun addCardToTaskList(position: Int, cardName: String){
        mBoardDetails.taskLists.removeAt(mBoardDetails.taskLists.size - 1)
        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())

        val card = Card(
            cardName,
            FirestoreClass().getCurrentUserId(),
            cardAssignedUsersList
        )

        val cardsList = mBoardDetails.taskLists[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskLists[position].title,
            mBoardDetails.taskLists[position].createdBy,
            cardsList
        )
        mBoardDetails.taskLists[position] = task
        showProgressDialog(Constants.PLEASE_WAIT)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
        Log.e("mBoardDetails", task.toString())
    }
    fun cardDetails(taskListPosition: Int, cardListPosition: Int){
        startActivityForResult(
            Intent(this, CardDetailsActivity::class.java)
                .putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                .putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
                .putExtra(Constants.CARD_LIST_ITEM_POSITION, cardListPosition)
                .putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailsList),
            CARD_DETAILS_REQ_CODE
        )
    }
    fun boardMembersDetailsList(list: ArrayList<User>){
        mAssignedMembersDetailsList = list
        hideProgressDialog()
        val addTaskList = Task("Add List")
        mBoardDetails.taskLists.add(addTaskList)
        binding.rvTaskList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, mBoardDetails.taskLists)
        binding.rvTaskList.adapter = adapter
    }
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        mBoardDetails.taskLists.removeAt(mBoardDetails.taskLists.size - 1)
        mBoardDetails.taskLists[taskListPosition].cards = cards
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    companion object{
        const val MEMBER_REQ_CODE: Int = 13
        const val CARD_DETAILS_REQ_CODE: Int = 14
    }
}