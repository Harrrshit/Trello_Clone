package com.example.trelloclone_1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone_1.R
import com.example.trelloclone_1.models.SelectedMembers
import de.hdodenhof.circleimageview.CircleImageView

open class CardMembersListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    private val assignedMembers: Boolean
): RecyclerView.Adapter<CardMembersListItemsAdapter.NewRecyclerView>() {
    private var onClickListener: OnClickListener? = null
    class NewRecyclerView(view: View): RecyclerView.ViewHolder(view){
        val ivSelectedMemberImage: CircleImageView = view.findViewById(R.id.iv_selected_member_image)
        val ivAddMember: CircleImageView = view.findViewById(R.id.iv_add_member)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewRecyclerView {
        return NewRecyclerView(LayoutInflater.from(context).inflate(R.layout.item_card_selected_member, parent, false))
    }

    override fun onBindViewHolder(holder: NewRecyclerView, position: Int) {
        val model = list[position]
        if(position == list.size - 1 && assignedMembers){
            holder.ivAddMember.visibility = View.VISIBLE
            holder.ivSelectedMemberImage.visibility = View.GONE
        }else{
            holder.ivAddMember.visibility = View.GONE
            holder.ivSelectedMemberImage.visibility = View.VISIBLE
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.ivSelectedMemberImage)
        }
        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.onClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnClickListener{
        fun onClick()
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
}