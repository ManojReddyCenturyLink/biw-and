package com.centurylink.biwf.screens.notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.screens.notification.NotificationActivity
import timber.log.Timber

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

    private fun getUnReadItemCount(): Int {
        return notificationListItems.filter { it.isUnRead }.size
    }
}

sealed class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener,
        unreadItemCount: Int
    )
}

/**
 * Header View holder class responsible for displaying the header in the RecyclerView
 */
class UnReadHeaderViewHolder(view: View) : CustomViewHolder(view) {

    private val context: Context = view.context
    private var unReadNotificationCount: TextView = view.findViewById(R.id.unread_notification_count)
    private var markAllReadView: TextView = view.findViewById(R.id.notification_list_unread_mark_as_read)
    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener,
        unreadItemCount: Int
    ) {
        if (unreadItemCount> 0) {
            val unreadValue: String =
                context.getString(R.string.unread_notification_count, unreadItemCount - 1)
            unReadNotificationCount.text = unreadValue
        }
        markAllReadView.setOnClickListener {
            // your code to perform when the user clicks on the button
            notificationItemClickListener.markAllNotificationAsRead()
        }
    }
}

class ReadHeaderViewHolder(view: View) : CustomViewHolder(view) {

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener,
        unreadItemCount: Int
    ) {
        Timber.e("Bind function")
    } }

/**
 * Item View holder class responsible for displaying the Items in the RecyclerView
 */
class UnReadItemViewHolder(view: View) : CustomViewHolder(view) {

    private var notificationTitle: TextView = view.findViewById(R.id.notification_list_unread_title)

    private var notificationDetail: TextView = view.findViewById(R.id.notification_list_unread_detail)

    private var notificationItemBackground: CardView = view.findViewById(R.id.notification_list_unread_card_background)

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener,
        unreadItemCount: Int
    ) {
        notificationTitle.text = notificationItem.name
        notificationDetail.text = notificationItem.description
        notificationItemBackground.setOnClickListener {
            // your code to perform when the user clicks on the button
            notificationItemClickListener.onNotificationItemClick(notificationItem)
        }
    }
}

/**
 * Item View holder class responsible for displaying the Items in the RecyclerView
 */
class ReadItemViewHolder(view: View) : CustomViewHolder(view) {

    private var notificationTitle: TextView = view.findViewById(R.id.notification_list_titleread)

    private var notificationDetail: TextView = view.findViewById(R.id.notification_list_titledetail)

    private var notificationItemBackground: ConstraintLayout = view.findViewById(R.id.notification_list_read_background)

    override fun bind(
        notificationItem: Notification,
        notificationItemClickListener: NotificationItemClickListener,
        unreadItemCount: Int
    ) {
        notificationTitle.text = notificationItem.name
        notificationDetail.text = notificationItem.description
        notificationItemBackground.setOnClickListener {
            // your code to perform when the user clicks on the button
            notificationItemClickListener.onNotificationItemClick(notificationItem)
        }
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
        /**
         * return the enum that matches the Int code
          */
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
