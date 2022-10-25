package com.example.trelloclone_1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone_1.R
import com.example.trelloclone_1.models.Board
import de.hdodenhof.circleimageview.CircleImageView

class BoardItemsAdapter(private val context: Context, private val list: ArrayList<Board>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var onClickListener: OnClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_board, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .into(holder.itemBoardProfileImageCIV)
            holder.itemBoardBoardNameTV.text = model.name
            holder.itemBoardCreatedByTV.text = "Created By: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if(onClickListener != null ){
                    onClickListener.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int{
        return list.size
    }
    interface OnClickListener{
        fun onClick(position: Int, model: Board )
    }
    fun setOnClickListener(onClickListener: OnClickListener){
    this.onClickListener = onClickListener
    }
    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemBoardProfileImageCIV: CircleImageView = view.findViewById(R.id.itemBoardProfileImageCIV)
        val itemBoardBoardNameTV: TextView = view.findViewById(R.id.itemBoardBoardNameTV)
        val itemBoardCreatedByTV: TextView = view.findViewById(R.id.itemBoardCreatedByTV)
    }
}