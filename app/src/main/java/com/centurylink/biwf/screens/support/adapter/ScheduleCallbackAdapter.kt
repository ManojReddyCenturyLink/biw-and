package com.centurylink.biwf.screens.support.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.support.TopicList
import kotlinx.android.synthetic.main.widget_recyclerview_list.view.*

/**
 * ScheduleCallbackAdapter Adapter used for the purpose of displaying the callback reasons topics list items in the UI
 */
class ScheduleCallbackAdapter(
    private val mContext: Context,
    private val itemClickListener: ScheduleCallbackItemClickListener,
    private var listItems: List<TopicList>
) : RecyclerView.Adapter<ScheduleCallbackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleCallbackViewHolder {
        return ScheduleCallbackViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.widget_recyclerview_list, parent, false))
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun onBindViewHolder(holder: ScheduleCallbackViewHolder, position: Int) {
        val item: TopicList = listItems[position]
        holder.header.text = item.topic
    }
}

class ScheduleCallbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val header: TextView = view.rv_sub_heading
    val content: ConstraintLayout = view.rv_list_content
}

interface ScheduleCallbackItemClickListener {
    /**
     * Handle click event on item click
     */
    fun onItemClick(item: TopicList)
}