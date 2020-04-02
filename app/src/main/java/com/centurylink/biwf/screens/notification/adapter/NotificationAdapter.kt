package com.centurylink.biwf.screens.notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.screens.notification.NotificationActivity

/**
 * Notification Adapter used for the purpose of displaying the Notification  List items in the UI
 */
class NotificationAdapter(
    private var notificationListItems: MutableList<Notification>,
    private val notificationItemClickListener: NotificationItemClickListener
) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return ItemType.fromCode(viewType).buildView(parent, viewType, getUnReadItemCount())
    }

    override fun getItemCount(): Int {
        return notificationListItems.size
    }


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val notificationItem: Notification = notificationListItems[position]
        holder.bind(notificationItem, notificationItemClickListener, getUnReadItemCount())
    }

    override fun getItemViewType(position: Int): Int {
        val notificationItem: Notification = notificationListItems[position]
        return when (notificationItem.id) {
            NotificationActivity.KEY_UNREAD_HEADER -> ItemType.HEADER_TYPE_UNREAD.code
            NotificationActivity.KEY_READ_HEADER -> ItemType.HEADER_TYPE_READ.code
            else -> {
                if (notificationItem.isUnRead) {
                    ItemType.ITEM_TYPE_UNREAD.code
                } else {
                    ItemType.ITEM_TYPE_READ.code
                }
            }
        }
    }

    fun updateList(updatedList: MutableList<Notification>) {
        notificationListItems = updatedList
        notifyDataSetChanged()
    }

    private fun getUnReadItemCount(): Int {
        return notificationListItems.filter { it.isUnRead }.size
    }
}

sealed class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener, unreadItemCount: Int
    )
}

/**
 * Header View holder class responsible for displaying the header in the RecyclerView
 */
class UnReadHeaderViewHolder(view: View) : CustomViewHolder(view) {
    private val context: Context = view.context

    private var unReadNotificationCount: TextView = view.findViewById(R.id.notification_unread)

    private var markAllReadView: TextView = view.findViewById(R.id.notification_mark_as_read)

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener, unreadItemCount: Int
    ) {
        val unreadValue: String = context.getString(R.string.notification_screen_unread, 5)
        unReadNotificationCount.text = unreadValue
        markAllReadView.setOnClickListener {
            // your code to perform when the user clicks on the button
            notificationItemClickListener.markAllNotificationAsRead()
        }
    }
}

class ReadHeaderViewHolder(view: View) : CustomViewHolder(view) {

    private var clearAllView: TextView = view.findViewById(R.id.notification_clear)

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener, unreadItemCount: Int
    ) {
        clearAllView.setOnClickListener {
            // your code to perform when the user clicks on the button
            notificationItemClickListener.clearAllReadNotification()
        }
    }
}

/**
 * Item View holder class responsible for displaying the Items in the RecyclerView
 */
class UnReadItemViewHolder(view: View) : CustomViewHolder(view) {

    private var notificationTitle: TextView = view.findViewById(R.id.tvNotificationTitle)

    private var notificationDetail: TextView = view.findViewById(R.id.tvNotificationBody)

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener, unreadItemCount: Int
    ) {
        notificationTitle.text = notificationItem.name
        notificationDetail.text = notificationItem.description
        notificationDetail.setOnClickListener {
            // your code to perform when the user clicks on the button
            notificationItemClickListener.onNotificationItemClick(notificationItem)
        }
    }
}

/**
 * Item View holder class responsible for displaying the Items in the RecyclerView
 */
class ReadItemViewHolder(view: View) : CustomViewHolder(view) {

    private var notificationTitle: TextView = view.findViewById(R.id.tvReadNotificationTitle)

    private var notificationDetail: TextView = view.findViewById(R.id.tvReadNotificationBody)

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener, unreadItemCount: Int
    ) {
        notificationTitle.text = notificationItem.name
        notificationDetail.text = notificationItem.description
    }
}


/**
 * Enum class for identifying the type to be Header or Item
 */
enum class ItemType(val code: Int) {

    HEADER_TYPE_UNREAD(0) {
        override fun buildView(
            parent: ViewGroup,
            viewType: Int,
            unreadItemCount: Int
        ): CustomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_list_header_unread, parent, false)
            return UnReadHeaderViewHolder(view)
        }
    },
    ITEM_TYPE_UNREAD(1) {
        override fun buildView(
            parent: ViewGroup,
            viewType: Int,
            unreadItemCount: Int
        ): CustomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_item_unread, parent, false)
            return UnReadItemViewHolder(view)
        }
    },
    HEADER_TYPE_READ(2) {
        override fun buildView(
            parent: ViewGroup,
            viewType: Int,
            unreadItemCount: Int
        ): CustomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_list_header_read, parent, false)
            return ReadHeaderViewHolder(view)
        }
    },
    ITEM_TYPE_READ(3) {
        override fun buildView(
            parent: ViewGroup,
            viewType: Int,
            unreadItemCount: Int
        ): CustomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_item_read, parent, false)
            return ReadItemViewHolder(view)
        }
    };

    companion object {
        // return the enum that matches the Int code
        fun fromCode(code: Int): ItemType = values().first { code == it.code }
    }

    abstract fun buildView(parent: ViewGroup, viewType: Int, unreadItemCount: Int): CustomViewHolder
}

interface NotificationItemClickListener {

    /**
     * Handle click event on Item click
     *
     */
    fun onNotificationItemClick(notificationItem: Notification)

    /**
     * Handle click event on the clearall in header
     */
    fun clearAllReadNotification()

    /**
     * Handle click event on the Mark All UnRead
     */
    fun markAllNotificationAsRead()
}