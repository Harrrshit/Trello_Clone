package com.example.trelloclone_1.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone_1.R
import com.example.trelloclone_1.activities.TaskListActivity
import com.example.trelloclone_1.models.Card
import com.example.trelloclone_1.models.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemsAdapter(private val context: Context, private var list: ArrayList<Task>):
    RecyclerView.Adapter<TaskListItemsAdapter.MyViewHolder>(){
    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.8).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            (15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0
        )
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if(position == list.size - 1){
            holder.tvAddTaskList.visibility = View.VISIBLE
            holder.llTaskItem.visibility = View.GONE
        }else{
            holder.tvAddTaskList.visibility = View.GONE
            holder.llTaskItem.visibility = View.VISIBLE
        }
        holder.tvTaskListTitle.text = model.title

        holder.tvAddTaskList.setOnClickListener {
            Log.i("response", "clicked")
            holder.tvAddTaskList.visibility = View.GONE
            holder.cvAddTaskListName.visibility = View.VISIBLE
        }
        holder.ibCloseListName.setOnClickListener {
            holder.tvAddTaskList.visibility = View.VISIBLE
            holder.cvAddTaskListName.visibility = View.GONE
        }
        holder.ibDoneListName.setOnClickListener {
            val listName = holder.etTaskListName.text.toString()
            Log.e("responseTLIA", listName)
            if(listName.isNotEmpty()){
                if(context is TaskListActivity){
                    context.createTaskList(listName)
                    Log.e("responseTLIA", model.toString())
                }
            }else{
                Toast.makeText(context, "cannot create empty boards", Toast.LENGTH_SHORT).show()
            }
        }
        holder.ibEditListName.setOnClickListener {
            holder.etEditTaskListName.setText(model.title)
            holder.llTitleView.visibility =  View.GONE
            holder.cvEditTaskListName.visibility = View.VISIBLE
        }
        holder.ibDeleteList.setOnClickListener {
            alertDialogForDeleteList(position, model.title)
        }
        holder.ibCloseEditTable.setOnClickListener {
            holder.llTitleView.visibility = View.VISIBLE
            holder.cvEditTaskListName.visibility = View.GONE
        }
        holder.ibDoneEditListName.setOnClickListener {
            val listName = holder.etEditTaskListName.text.toString()
            Log.e("responseTLIABefore", model.toString())
            if(listName.isNotEmpty()){
                if(context is TaskListActivity){
                    context.updateTaskList(position, listName, model)
                    Log.e("responseTLIAAfter", model.toString())
                }
            }else{
                Toast.makeText(context, "please enter a list name", Toast.LENGTH_SHORT).show()
            }
        }
        holder.tvAddCard.setOnClickListener {
            holder.tvAddCard.visibility = View.GONE
            holder.cvAddCard.visibility = View.VISIBLE
        }
        holder.ibCloseCardName.setOnClickListener {
            holder.tvAddCard.visibility = View.VISIBLE
            holder.cvAddCard.visibility = View.GONE
        }
        holder.ibDoneCardName.setOnClickListener {
            val cardName = holder.etCardName.text.toString()
            if(cardName.isNotEmpty()){
                if(context is TaskListActivity){
                    context.addCardToTaskList(position, cardName)
                }
            }else{
                Toast.makeText(context, "please enter card name", Toast.LENGTH_SHORT).show()
            }
        }
        holder.rvCardList.layoutManager = LinearLayoutManager(context)
        holder.rvCardList.setHasFixedSize(true)
        val adapter = CardListItemAdapter(context, model.cards)
        holder.rvCardList.adapter = adapter

        adapter.setOnClickListener(
            object : CardListItemAdapter.OnClickListener{
                override fun onClick(cardPosition: Int) {
                    if(context is TaskListActivity){
                        context.cardDetails(position, cardPosition)
                    }
                }
            }
        )
        val dividerItemDecoration =  DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        holder.rvCardList.addItemDecoration(dividerItemDecoration)
        val helper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = viewHolder.adapterPosition
                    val targetPosition = target.adapterPosition
                    if(mPositionDraggedFrom == -1){
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition
                    Collections.swap(list[position].cards, draggedPosition, targetPosition)
                    adapter.notifyItemMoved(draggedPosition, targetPosition)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    if(mPositionDraggedFrom != 1 && mPositionDraggedTo != -1 &&
                            mPositionDraggedFrom != mPositionDraggedTo){
                        (context as TaskListActivity).updateCardsInTaskList(
                            position,
                            list[position].cards
                        )
                    }
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }

            }
        )
        helper.attachToRecyclerView(holder.rvCardList)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val llTaskItem: LinearLayout = view.findViewById(R.id.ll_task_item)
        val tvAddTaskList: TextView = view.findViewById(R.id.tv_add_task_list)
        val tvTaskListTitle: TextView = view.findViewById(R.id.tv_task_list_title)
        val cvAddTaskListName: CardView = view.findViewById(R.id.cv_add_task_list_name)
        val ibCloseListName: ImageButton = view.findViewById(R.id.ib_close_list_name)
        val ibDoneListName: ImageButton = view.findViewById(R.id.ib_done_list_name)
        val etTaskListName: EditText = view.findViewById(R.id.et_task_list_name)
        val ibEditListName: ImageButton = view.findViewById(R.id.ib_edit_list_name)
        val ibDeleteList: ImageButton = view.findViewById(R.id.ib_delete_list)
        val etEditTaskListName: EditText = view.findViewById(R.id.et_edit_task_list_name)
        val llTitleView: LinearLayout = view.findViewById(R.id.ll_title_view)
        val cvEditTaskListName: CardView = view.findViewById(R.id.cv_edit_task_list_name)
        val ibCloseEditTable: ImageButton = view.findViewById(R.id.ib_close_editable_view)
        val ibDoneEditListName: ImageButton = view.findViewById(R.id.ib_done_edit_list_name)
        val tvAddCard: TextView = view.findViewById(R.id.tv_add_card)
        val cvAddCard: CardView = view.findViewById(R.id.cv_add_card)
        val etCardName: EditText = view.findViewById(R.id.et_card_name)
        val ibCloseCardName: ImageButton = view.findViewById(R.id.ib_close_card_name)
        val ibDoneCardName: ImageButton = view.findViewById(R.id.ib_done_card_name)
        val rvCardList: RecyclerView = view.findViewById(R.id.rv_card_list)

    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteList(position: Int, title: String){
        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface, _ ->
            dialogInterface.dismiss()
            if(context is TaskListActivity){
                context.deleteTaskList(position)
            }

        }
        builder.setNegativeButton("No"){dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }
}