package com.example.trelloclone_1.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone_1.R
import com.example.trelloclone_1.adapters.LabelColorListItemAdapter

abstract class LabelColorListDialog(
    context: Context,
    private val list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
    ): Dialog(context) {
    private var adapter: LabelColorListItemAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }
    private fun setUpRecyclerView(view: View){
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val rvDialog: RecyclerView = findViewById(R.id.rv_dialogList)

       if(list.size > 0){
           tvTitle.text = title
           rvDialog.layoutManager = LinearLayoutManager(context)
           adapter = LabelColorListItemAdapter(context, list, mSelectedColor)
           rvDialog.adapter = adapter

           adapter?.onItemClickListener = object : LabelColorListItemAdapter.OnItemClickListener{
               override fun onClick(position: Int, color: String) {
                   dismiss()
                   onItemSelected(color)
               }
           }
       }
    }
    protected abstract fun onItemSelected(color: String)
}