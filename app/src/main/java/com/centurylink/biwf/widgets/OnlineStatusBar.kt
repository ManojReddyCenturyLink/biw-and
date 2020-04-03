package com.centurylink.biwf.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.centurylink.biwf.R

class OnlineStatusBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val onlineStatusIcon: ImageView
    private val onlineStatusText: TextView
    private val networkNameTextView: TextView
    private val onlineStatusSpacer: TextView

    fun setOnlineStatus(onlineStatusData: OnlineStatusData) {
        if (onlineStatusData.isOnline) isOnline(onlineStatusData.networkName) else isOffLine()
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_online_bar, this, true)
        onlineStatusSpacer = view.findViewById(R.id.status_bar_spacer)
        onlineStatusText = view.findViewById(R.id.status_bar_online_status)
        onlineStatusIcon = view.findViewById(R.id.status_bar_online_circle)
        networkNameTextView = view.findViewById(R.id.status_bar_network_name)
    }

    private fun isOnline(networkName: String) {
        onlineStatusText.text = "Online"
        onlineStatusSpacer.visibility = View.VISIBLE
        onlineStatusIcon.setImageResource(R.drawable.green_circle)
        networkNameTextView.text = networkName
    }

    private fun isOffLine() {
        onlineStatusText.text = "Offline"
        onlineStatusIcon.setImageResource(R.drawable.red_circle)
        onlineStatusSpacer.visibility = View.INVISIBLE
        networkNameTextView.text = ""
    }
}

data class OnlineStatusData(var isOnline: Boolean = false, var networkName: String = "")