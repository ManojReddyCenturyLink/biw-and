package com.centurylink.biwf.screens.home.devices.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.centurylink.biwf.R
import com.centurylink.biwf.model.devices.DeviceConnectionStatus
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.DeviceStatus
import com.centurylink.biwf.screens.home.devices.adapter.DeviceListAdapter.MyChildViewHolder
import com.centurylink.biwf.screens.home.devices.adapter.DeviceListAdapter.MyGroupViewHolder
import com.centurylink.biwf.screens.networkstatus.ModemUtils
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableSwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import timber.log.Timber

internal class DeviceListAdapter(
    var deviceList: HashMap<DeviceStatus, MutableList<DevicesData>>,
    private val deviceListItemClickListener: DeviceItemClickListener
) : AbstractExpandableItemAdapter<MyGroupViewHolder, MyChildViewHolder>(),
    ExpandableDraggableItemAdapter<MyGroupViewHolder, MyChildViewHolder>,
    ExpandableSwipeableItemAdapter<MyGroupViewHolder, MyChildViewHolder> {

    abstract class MyBaseViewHolder(v: View) :
        AbstractDraggableSwipeableItemViewHolder(v), ExpandableItemViewHolder {
        var mContainer: ConstraintLayout = v.findViewById(R.id.container)
        private val mExpandState = ExpandableItemState()
        override fun getSwipeableContainerView(): View {
            return mContainer
        }

        override fun getExpandStateFlags(): Int {
            return mExpandState.flags
        }

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        override fun getExpandState(): ExpandableItemState {
            return mExpandState
        }
    }

    class MyGroupViewHolder(v: View) : MyBaseViewHolder(v) {
        var mHeaderNameTv: TextView = v.findViewById(R.id.header_name)
        var mGroupCountTv: TextView = v.findViewById(R.id.devices_group_count)
        var mArrowIv: ImageView = v.findViewById(R.id.devices_header_arrow)
        var mTapToRestoreTv: TextView = v.findViewById(R.id.devices_tap_to_remove_item)
    }

    class MyChildViewHolder(v: View) : MyBaseViewHolder(v) {
        var mChildNameTv: TextView = v.findViewById(R.id.device_name)
        var mSignalStrengthIv: ImageView = v.findViewById(R.id.iv_network_type)
        var mProgressIconIv: ProgressBar = v.findViewById(R.id.progress_icon)
    }

    override fun getGroupCount(): Int {
        return deviceList.size
    }

    override fun getChildCount(groupPosition: Int): Int {
        return when (groupPosition) {
            0 -> if (deviceList[DeviceStatus.CONNECTED].isNullOrEmpty()) 0 else deviceList[DeviceStatus.CONNECTED]!!.size
            1 -> deviceList[DeviceStatus.BLOCKED]!!.size
            else -> 0
        }
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupItemViewType(groupPosition: Int): Int {
        return groupPosition
    }

    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int {
        return groupPosition
    }

    override fun onCreateGroupViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyGroupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == 0) {
            return MyGroupViewHolder(
                inflater.inflate(
                    R.layout.layout_header_devices_connected,
                    parent,
                    false
                )
            )
        }
        return MyGroupViewHolder(
            inflater.inflate(
                R.layout.layout_devicelist_group_blocked,
                parent,
                false
            )
        )
    }

    override fun onCreateChildViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyChildViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == 0) {
            return MyChildViewHolder(
                inflater.inflate(
                    R.layout.layout_connected_devices,
                    parent,
                    false
                )
            )
        }
        return MyChildViewHolder(
            inflater.inflate(
                R.layout.layout_blocked_devices,
                parent,
                false
            )
        )
    }

    override fun onBindGroupViewHolder(
        holder: MyGroupViewHolder,
        groupPosition: Int,
        viewType: Int
    ) {
        val totalConnectedDevices = getChildrenCount(groupPosition)

        if (groupPosition == 0) {
            holder.mArrowIv.visibility = if (totalConnectedDevices == 0) View.GONE else View.VISIBLE
            holder.mGroupCountTv.text = totalConnectedDevices.toString()
            if (holder.expandState.isExpanded) {
                holder.mArrowIv.setImageResource(R.drawable.ic_faq_down)
            } else {
                holder.mArrowIv.setImageResource(R.drawable.ic_icon_right)
            }
            if (totalConnectedDevices == 1) {
                holder.mHeaderNameTv.text = "Connected device"
            } else {
                holder.mHeaderNameTv.text = "Connected devices"
            }
            holder.mArrowIv.visibility = View.VISIBLE
            holder.mTapToRestoreTv.visibility = View.GONE
            holder.mGroupCountTv.visibility = View.VISIBLE
        } else {
            if (totalConnectedDevices == 1) {
                holder.mHeaderNameTv.text = "Removed device"
            } else {
                holder.mHeaderNameTv.text = "Removed devices"
            }
            holder.mTapToRestoreTv.visibility = View.VISIBLE
            holder.mArrowIv.visibility = View.GONE
            holder.mGroupCountTv.visibility = View.GONE
        }
    }

    private fun getChildrenCount(groupPosition: Int): Int {
        return when (groupPosition) {
            0 -> if (deviceList[DeviceStatus.CONNECTED].isNullOrEmpty()) 0 else deviceList[DeviceStatus.CONNECTED]!!.size
            1 -> deviceList[DeviceStatus.BLOCKED]!!.size
            else -> 0
        }
    }

    private fun getChild(groupPosition: Int, childPosition: Int): DevicesData {
        return when (groupPosition) {
            0 -> deviceList[DeviceStatus.CONNECTED]!![childPosition]
            1 -> deviceList[DeviceStatus.BLOCKED]!![childPosition]
            else -> deviceList[DeviceStatus.CONNECTED]!![childPosition]
        }
    }

    override fun onBindChildViewHolder(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int,
        viewType: Int
    ) {
        if (groupPosition == 0) {
            val connectedData = getChild(groupPosition, childPosition)
            var nickName = ""
            nickName = if (!connectedData.mcAfeeName.isNullOrEmpty()) {
                connectedData.mcAfeeName
            } else {
                connectedData.hostName ?: ""
            }
            holder.mChildNameTv.text = nickName
            when (connectedData.deviceConnectionStatus) {
                DeviceConnectionStatus.LOADING -> {
                    holder.mProgressIconIv.visibility = View.VISIBLE
                    holder.mSignalStrengthIv.visibility = View.GONE
                }
                DeviceConnectionStatus.FAILURE,
                DeviceConnectionStatus.DEVICE_CONNECTED,
                DeviceConnectionStatus.PAUSED,
                DeviceConnectionStatus.MODEM_OFF -> {
                    holder.mProgressIconIv.visibility = View.GONE
                    holder.mSignalStrengthIv.visibility = View.VISIBLE
                }
            }
            holder.mSignalStrengthIv.setImageResource(
                ModemUtils.getConnectionStatusIconForDeviceList(devicesData = connectedData)
            )

            holder.mContainer.setOnClickListener {
                deviceListItemClickListener.onConnectedDevicesClicked(
                    devicesInfo = connectedData
                )
            }
            holder.mSignalStrengthIv.setOnClickListener {
                deviceListItemClickListener.onConnectionStatusChanged(connectedData)
                notifyDataSetChanged()
            }
        } else if (groupPosition == 1) {

            val blockedData = getChild(groupPosition, childPosition)

            var nickName = ""
            nickName = if (!blockedData.mcAfeeName.isNullOrEmpty()) {
                blockedData.mcAfeeName
            } else {
                blockedData.hostName ?: ""
            }
            holder.mChildNameTv.text = nickName
            holder.mContainer.setOnClickListener {
                deviceListItemClickListener.onRemovedDevicesClicked(
                    devicesInfo = blockedData
                )
            }
        }
    }

    override fun onCheckCanExpandOrCollapseGroup(
        holder: MyGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int,
        expand: Boolean
    ): Boolean {
        if (groupPosition == 0) {
            return true
        }
        return false
    }

    override fun getInitialGroupExpandedState(groupPosition: Int): Boolean {
        return true
    }

    override fun onCheckGroupCanStartDrag(
        holder: MyGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Boolean {

        return true
    }

    override fun onCheckChildCanStartDrag(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Boolean {

        return true
    }

    override fun onGetGroupItemDraggableRange(
        holder: MyGroupViewHolder,
        groupPosition: Int
    ): ItemDraggableRange? {
        // no drag-sortable range specified
        return null
    }

    override fun onGetChildItemDraggableRange(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int
    ): ItemDraggableRange? {
        // no drag-sortable range specified
        return null
    }

    override fun onCheckGroupCanDrop(
        draggingGroupPosition: Int,
        dropGroupPosition: Int
    ): Boolean {
        return true
    }

    override fun onCheckChildCanDrop(
        draggingGroupPosition: Int,
        draggingChildPosition: Int,
        dropGroupPosition: Int,
        dropChildPosition: Int
    ): Boolean {
        return true
    }

    override fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        Timber.e("on move group item function")
    }

    override fun onMoveChildItem(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int
    ) {
        Timber.e("on move child item function")
    }

    override fun onGroupDragStarted(groupPosition: Int) {
        notifyDataSetChanged()
    }

    override fun onChildDragStarted(groupPosition: Int, childPosition: Int) {
        notifyDataSetChanged()
    }

    override fun onGroupDragFinished(
        fromGroupPosition: Int,
        toGroupPosition: Int,
        result: Boolean
    ) {
        notifyDataSetChanged()
    }

    override fun onChildDragFinished(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int,
        result: Boolean
    ) {
        notifyDataSetChanged()
    }

    override fun onGetGroupItemSwipeReactionType(
        holder: MyGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Int {
        return if (onCheckGroupCanStartDrag(holder, groupPosition, x, y)) {
            SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H
        } else SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onGetChildItemSwipeReactionType(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Int {
        return if (onCheckChildCanStartDrag(holder, groupPosition, childPosition, x, y)) {
            SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H
        } else SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onSwipeGroupItemStarted(
        holder: MyGroupViewHolder,
        groupPosition: Int
    ) {
        notifyDataSetChanged()
    }

    override fun onSwipeChildItemStarted(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int
    ) {
        notifyDataSetChanged()
    }

    override fun onSetGroupItemSwipeBackground(
        holder: MyGroupViewHolder,
        groupPosition: Int,
        type: Int
    ) {
        Timber.e("on set group item swipe background function")
    }

    override fun onSetChildItemSwipeBackground(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int,
        type: Int
    ) {
        Timber.e("on set child item swipe background function")
    }

    override fun onSwipeGroupItem(
        holder: MyGroupViewHolder,
        groupPosition: Int,
        result: Int
    ): SwipeResultAction? {

        return null
    }

    override fun onSwipeChildItem(
        holder: MyChildViewHolder,
        groupPosition: Int,
        childPosition: Int,
        result: Int
    ): SwipeResultAction? {

        return null
    }

    init {
        setHasStableIds(true)
    }

    interface DeviceItemClickListener {
        /**
         * Handle click event on Connected Devices Item click
         *
         */
        fun onConnectedDevicesClicked(devicesInfo: DevicesData)

        /**
         * Handle click event on Connected Devices Item click
         *
         */
        fun onRemovedDevicesClicked(devicesInfo: DevicesData)

        /**
         * Handle click event on Connection Pause or Resume
         *
         */
        fun onConnectionStatusChanged(isPaused: DevicesData)
    }
}
