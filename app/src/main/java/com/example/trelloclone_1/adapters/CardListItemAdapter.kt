package com.example.trelloclone_1.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone_1.R
import com.example.trelloclone_1.activities.TaskListActivity
import com.example.trelloclone_1.models.Board
import com.example.trelloclone_1.models.Card
import com.example.trelloclone_1.models.SelectedMembers

class CardListItemAdapter(
    private val context: Context,
    private var list: ArrayList<Card>): RecyclerView.Adapter<CardListItemAdapter.MyViewHolder>() {
    private lateinit var onClickListener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int){
        val model = list[position]
        holder.tvCardName.text = model.name
        if(model.labelColor.isNotEmpty()){
            holder.viewLabelColor.visibility = View.VISIBLE
            holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
        }else{
            holder.viewLabelColor.visibility = View.GONE
        }
        if((context as TaskListActivity).mAssignedMembersDetailsList.size > 0){
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

            for(i in context.mAssignedMembersDetailsList.indices){
                for(j in model.assignedTo){
                     if(context.mAssignedMembersDetailsList[i].id == j){
                         val selectedMember = SelectedMembers(
                             context.mAssignedMembersDetailsList[i].id,
                             context.mAssignedMembersDetailsList[i].image
                         )
                         selectedMembersList.add(selectedMember)
                     }
                }
            }
            if(selectedMembersList.size > 0){
                if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                    holder.rvCardSelectedMembersList.visibility = View.GONE
                }else{
                    holder.rvCardSelectedMembersList.visibility = View.VISIBLE
                    holder.rvCardSelectedMembersList.layoutManager = GridLayoutManager(context, 4)
                    val adapter = CardMembersListItemsAdapter(
                        context,
                        selectedMembersList,
                        false
                    )

                    holder.apply {
                        rvCardSelectedMembersList.adapter = adapter
                    }
                    adapter.setOnClickListener(object : CardMembersListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            if(onClickListener != null){
                                onClickListener!!.onClick(position)
                            }
                        }

                    })
                }
            }else{
                holder.rvCardSelectedMembersList.visibility = View.GONE
            }
        }
        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                onClickListener.onClick(position,)
            }
        }
    }
    interface OnClickListener{
        fun onClick(position: Int)
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    override fun getItemCount(): Int {
        return list.size
    }
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvCardName: TextView = view.findViewById(R.id.tv_card_name)
        val viewLabelColor: View = view.findViewById(R.id.view_label_color)
        val rvCardSelectedMembersList : RecyclerView = view.findViewById(R.id.rv_card_selected_members_list)
    }
    
}