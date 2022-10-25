package com.example.trelloclone_1.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloclone_1.activities.*
import com.example.trelloclone_1.models.Board
import com.example.trelloclone_1.models.User
import com.example.trelloclone_1.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.collections.HashMap

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USER)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener{
                Toast.makeText(activity, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun getCurrentUserId(): String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USER)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                when(activity){
                    is MainActivity -> {
                        activity.tokenUpdateSuccessful()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccessful()
                    }
                }

            }

            .addOnFailureListener {
                exception ->
                when(activity){
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity, "${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun loadUserData(activity: Activity, readBoardList: Boolean = false){
        mFireStore.collection(Constants.USER)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                when(activity){
                    is SignInActivity ->{
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity ->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                    }
                    is MyProfileActivity ->{
                        if(loggedInUser != null){
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener{
                when(activity){
                    is SignInActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                }
            }
        }
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                Log.i(Constants.TAG, exception.message.toString())
            }
    }
    fun getBoardListActivity(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                activity.hideProgressDialog()
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)
                    board!!.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUi(boardList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }
    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                activity.hideProgressDialog()
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }
    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap =  HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskLists
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if(activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }.addOnFailureListener { exception ->
                if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e("fail", exception.message.toString())
            }
    }
    fun getAssignedMemberListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USER)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                val userList: ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setUpMemberList(userList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(userList)
            }.addOnFailureListener { exception ->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                Toast.makeText(activity, exception.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USER)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {document ->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.errorSnackBar("No Such member Found")
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
                activity.errorSnackBar("Something went wrong")
            }
    }
    fun assignMembersToBoard(activity: Activity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                if(activity is MembersActivity)
                    activity.memberAssignSuccess(user)
            }.addOnFailureListener {
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show()
            }
    }
}