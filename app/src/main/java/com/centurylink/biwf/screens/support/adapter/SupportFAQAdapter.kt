package com.centurylink.biwf.screens.support.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.support.FaqTopicsItem
import kotlinx.android.synthetic.main.widget_faq_section.view.*

/**
 * SupportFAQ Adapter used for the purpose of displaying the FAQ topics list items in the UI
 */
class SupportFAQAdapter(
    private val mContext: Context,
    private val faqItemClickListener: SupportItemClickListener,
    private var faqListItems: MutableList<FaqTopicsItem>
) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(mContext).inflate(
                R.layout.widget_faq_section,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return faqListItems.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item: FaqTopicsItem = faqListItems[position]
        holder.header.text = item.type
        holder.content.setOnClickListener { faqItemClickListener.onFaqItemClick(item) }
    }
}

class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val header: TextView = view.faq_sub_heading
    val content: ConstraintLayout = view.faq_list_content
}

interface SupportItemClickListener {

    /**
     * Handle click event on Faq questions click
     *
     */
    fun onFaqItemClick(item: FaqTopicsItem)

}