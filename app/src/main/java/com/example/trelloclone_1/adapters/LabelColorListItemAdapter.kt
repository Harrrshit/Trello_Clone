package com.example.trelloclone_1.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone_1.R

class LabelColorListItemAdapter(
    private val context: Context,
    private val list: ArrayList<String>,
    private val mSelectedColor: String
    ): RecyclerView.Adapter<LabelColorListItemAdapter.NewViewHolder>() {
    var onItemClickListener : OnItemClickListener? = null
    class NewViewHolder(view: View): RecyclerView.ViewHolder(view){
        val viewMain: View = view.findViewById(R.id.view_main)
        val ivSelectedColor: ImageView = view.findViewById(R.id.iv_selected_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewViewHolder {
        return NewViewHolder(LayoutInflater.from(context).inflate(R.layout.item_label_color, parent,false))
    }

    override fun onBindViewHolder(holder: NewViewHolder, position: Int) {
        val item = list[position]
        holder.viewMain.setBackgroundColor(Color.parseColor(item))
        if(item == mSelectedColor){
            holder.ivSelectedColor.visibility = View.VISIBLE
        }else{
            holder.ivSelectedColor.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            if(onItemClickListener != null){
                onItemClickListener!!.onClick(position, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnItemClickListener{
        fun onClick(position: Int, color: String)
    }
}