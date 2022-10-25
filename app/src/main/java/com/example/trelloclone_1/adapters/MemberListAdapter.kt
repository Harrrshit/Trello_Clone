package com.example.trelloclone_1.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone_1.R
import com.example.trelloclone_1.models.User
import com.example.trelloclone_1.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MemberListAdapter (
    private var context: Context,
    private var list: ArrayList<User>
    ): RecyclerView.Adapter<MemberListAdapter.MyRecyclerView>() {
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecyclerView {
        Log.e("responseMLA", list.toString())
        return MyRecyclerView(LayoutInflater.from(context).inflate(R.layout.item_members, parent, false))
    }

    override fun onBindViewHolder(holder: MyRecyclerView, position: Int) {
        val model = list[position]
        Log.e("responseMLA", model.image)
        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.profile_placeholder)
            .into(holder.civMemberImage)
        holder.tvMemberName.text = model.name
        holder.tvMemberEmail.text = model.email

        if(model.selected){
            holder.ivSelectedMember.visibility = View.VISIBLE
        }
        else{
            holder.ivSelectedMember.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                if(model.selected){
                    onClickListener!!.onClick(position, model, Constants.UNSELECT)
                }else{
                    onClickListener!!.onClick(position, model, Constants.SELECT)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    class MyRecyclerView(view: View): RecyclerView.ViewHolder(view){
        val civMemberImage: CircleImageView = view.findViewById(R.id.iv_member_image)
        val tvMemberName: TextView = view.findViewById(R.id.tv_member_name)
        val tvMemberEmail: TextView = view.findViewById(R.id.tv_member_email)
        val ivSelectedMember : ImageView = view.findViewById(R.id.iv_selected_member)
    }
    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
}