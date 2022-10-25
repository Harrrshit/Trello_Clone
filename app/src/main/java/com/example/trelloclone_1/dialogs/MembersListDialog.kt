package com.example.trelloclone_1.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone_1.R
import com.example.trelloclone_1.adapters.LabelColorListItemAdapter
import com.example.trelloclone_1.adapters.MemberListAdapter
import com.example.trelloclone_1.models.User
import java.util.*
import kotlin.collections.ArrayList

abstract class MembersListDialog(
    context: Context,
    private val list: ArrayList<User>,
    private val title: String
    ): Dialog(context) {
    private var adapter: MemberListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
        Log.e("responseMLD", list.toString())
    }
    private fun setUpRecyclerView(view: View) {
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val rvDialogList: RecyclerView = findViewById(R.id.rv_dialogList)
        if(list.size > 0){
            tvTitle.text = title
            rvDialogList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListAdapter(context, list)
            rvDialogList.adapter = adapter

            adapter!!.setOnClickListener(object : MemberListAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }

            })
        }
    }
    protected abstract fun onItemSelected(user: User, action: String)
}