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

    fun setOnlineStatus(onlineStatusData: OnlineStatusData) {
        if (onlineStatusData.isOnline) isOnline(onlineStatusData.networkName) else isOffLine()
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_online_bar, this, true)
        onlineStatusText = view.findViewById(R.id.status_bar_online_status)
        onlineStatusIcon = view.findViewById(R.id.status_bar_online_circle)
    }

    private fun isOnline(networkName: String) {
        onlineStatusText.text = "Internet online"
        onlineStatusIcon.setImageResource(R.drawable.green_circle)
    }

    private fun isOffLine() {
        onlineStatusText.text = "Internet offline"
        onlineStatusIcon.setImageResource(R.drawable.red_circle)
    }
}

data class OnlineStatusData(var isOnline: Boolean = false, var networkName: String = "")