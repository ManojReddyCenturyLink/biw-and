package com.centurylink.biwf.screens.support.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.support.Videofaq
class FAQVideoViewAdapter (
    private var videoListItems: List<Videofaq>,
    private val videoItemClickListener: VideoItemClickListener
) : RecyclerView.Adapter<FAQVideoViewAdapter.CustomVideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): CustomVideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.faq_vido_list_item, parent, false)
        return CustomVideoViewHolder(view,videoItemClickListener)
    }

    override fun getItemCount(): Int {
        return videoListItems.size
    }

    override fun onBindViewHolder(videoViewHolder: CustomVideoViewHolder, position: Int) {
        videoViewHolder.bindItems(videoListItems[position])
    }

    override fun getItemViewType(position: Int): Int {
       // val videoItem: Videofaq = videoListItems[position]
        return videoListItems.size
    }

    class CustomVideoViewHolder(view: View,videoItemClickListener: VideoItemClickListener) : RecyclerView.ViewHolder(view){
        fun bindItems(videoFaq: Videofaq) {
            val videoThumbnail = itemView.findViewById(R.id.faq_video_thumbnail) as ImageView
            val videoDescription = itemView.findViewById(R.id.faq_video_desc) as TextView
            val videoDuration  = itemView.findViewById(R.id.faq_video_duration) as TextView
            videoDescription.text = videoFaq.description
            videoDuration.text = videoFaq.duration
            videoThumbnail.setOnClickListener { }
        }
    }
}

interface VideoItemClickListener{
    fun onVideoItemClicked(videoFAQ:Videofaq)
}