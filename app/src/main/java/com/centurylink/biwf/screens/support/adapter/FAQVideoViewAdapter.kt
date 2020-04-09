package com.centurylink.biwf.screens.support.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.support.Videofaq
class FAQVideoViewAdapter (
    private var videoListItems: List<Videofaq>,
    private val videoItemClickListener: VideoItemClickListener
) : RecyclerView.Adapter<FAQVideoViewAdapter.CustomVideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): CustomVideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_unread, parent, false)
        return CustomVideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return videoListItems.size
    }

    override fun onBindViewHolder(vh: CustomVideoViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        val videoItem: Videofaq = videoListItems[position]
        return videoListItems.size
    }

    class CustomVideoViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }
}

interface VideoItemClickListener{
    fun onVideoItemClicked(videoFAQ:Videofaq)
}