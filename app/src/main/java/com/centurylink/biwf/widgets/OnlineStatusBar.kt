package com.centurylink.biwf.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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

    fun setOnlineStatus(boolean: Boolean) {
        if (boolean) isOnline() else isOffLine()
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_online_bar, this, true)
        onlineStatusText = view.findViewById(R.id.status_bar_online_status)
        onlineStatusIcon = view.findViewById(R.id.status_bar_online_circle)
    }

    private fun isOnline() {
        onlineStatusText.text = "Internet online"
        onlineStatusIcon.setImageResource(R.drawable.green_circle)
    }

    private fun isOffLine() {
        onlineStatusText.text = "Internet offline"
        onlineStatusIcon.setImageResource(R.drawable.red_circle)
    }
}