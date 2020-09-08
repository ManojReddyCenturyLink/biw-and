package com.centurylink.biwf.screens.home.devices.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.centurylink.biwf.R
import com.centurylink.biwf.databinding.LayoutBlockedDevicesBinding
import com.centurylink.biwf.databinding.LayoutConnectedDevicesBinding
import com.centurylink.biwf.databinding.LayoutDevicelistGroupBlockedBinding
import com.centurylink.biwf.databinding.LayoutHeaderDevicesConnectedBinding
import com.centurylink.biwf.model.devices.DeviceConnectionStatus
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.DeviceStatus
import com.centurylink.biwf.screens.networkstatus.ModemUtils

class DeviceListAdapter(
    var deviceList: HashMap<DeviceStatus, MutableList<DevicesData>>,
    private val deviceItemClickListener: DeviceItemClickListener
) :
    BaseExpandableListAdapter() {
    var isModemAlive: Boolean = true

    override fun getGroup(groupPosition: Int): DeviceStatus {
        return when (groupPosition) {
            0 -> {
                DeviceStatus.CONNECTED
            }
            1 -> {
                DeviceStatus.BLOCKED
            }
            else -> {
                DeviceStatus.CONNECTED
            }
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        val layoutInflater =
            parent?.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return (if (groupPosition == 0) {
            val headerDevicesConnectedBinding =
                LayoutHeaderDevicesConnectedBinding.inflate(layoutInflater)
            val deviceCount = headerDevicesConnectedBinding.devicesGroupCount
            val connectedDeviceLabel = headerDevicesConnectedBinding.devicesGroupConnectedDevices
            val totalConnectedDevices = getChildrenCount(groupPosition)
            val listStatusIcon = headerDevicesConnectedBinding.devicesHeaderArrow
            listStatusIcon.visibility = if (totalConnectedDevices == 0) View.GONE else View.VISIBLE
            if (totalConnectedDevices == 1) {
                connectedDeviceLabel.text =
                    parent.context.getText(R.string.connected_devices_singular)
            } else {
                connectedDeviceLabel.text =
                    parent.context.getText(R.string.connected_devices_plural)
            }
            deviceCount.text = totalConnectedDevices.toString()

            if (isExpanded) {
                listStatusIcon.setImageResource(R.drawable.ic_faq_down)
            } else {
                listStatusIcon.setImageResource(R.drawable.ic_icon_right)
            }
            headerDevicesConnectedBinding.root
        } else {
            val layoutDeviceListGroupBlockedBinding =
                LayoutDevicelistGroupBlockedBinding.inflate(layoutInflater)
            layoutDeviceListGroupBlockedBinding.root
        })
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return when (groupPosition) {
            0 -> if (deviceList[DeviceStatus.CONNECTED].isNullOrEmpty()) 0 else deviceList[DeviceStatus.CONNECTED]!!.size
            1 -> deviceList[DeviceStatus.BLOCKED]!!.size
            else -> 0
        }

    }

    override fun getChild(groupPosition: Int, childPosition: Int): DevicesData {
        return when (groupPosition) {
            0 -> deviceList[DeviceStatus.CONNECTED]!![childPosition]
            1 -> deviceList[DeviceStatus.BLOCKED]!![childPosition]
            else -> deviceList[DeviceStatus.CONNECTED]!![childPosition]
        }
    }

    override fun getGroupId(groupPosition: Int): Long {
        return 0
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        return displayChildView(groupPosition, convertView, childPosition, parent)
    }

    private fun displayChildView(
        groupPosition: Int,
        convertView: View?,
        childPosition: Int,
        parent: ViewGroup?
    ): View {
        val layoutInflater =
            parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutConnectedDevicesBinding = LayoutConnectedDevicesBinding.inflate(layoutInflater)

        if (groupPosition == 0) {
            val connectedData = getChild(groupPosition, childPosition)
            val deviceName = layoutConnectedDevicesBinding.deviceName
            val deviceSignalStrength = layoutConnectedDevicesBinding.ivNetworkType
            val deviceLayout = layoutConnectedDevicesBinding.devicesListLayout
            val stateLoadingProgress = layoutConnectedDevicesBinding.progressIcon
            deviceName.text = connectedData.hostName
            //TODO Remove this when devices comes online
            when (connectedData.deviceConnectionStatus) {
                DeviceConnectionStatus.LOADING -> {
                    stateLoadingProgress.visibility = View.VISIBLE
                    deviceSignalStrength.visibility = View.GONE
                }
                DeviceConnectionStatus.DEVICE_CONNECTED,
                DeviceConnectionStatus.PAUSED,
                DeviceConnectionStatus.MODEM_OFF ->{
                    stateLoadingProgress.visibility = View.GONE
                    deviceSignalStrength.visibility = View.VISIBLE
                }
                DeviceConnectionStatus.FAILURE->{
                deviceSignalStrength.setColorFilter(Color.argb(255, 215, 255, 215))}
            }
            deviceSignalStrength.setImageResource(
                ModemUtils.getConnectionStatusIconForDeviceList(devicesData = connectedData)
            )
            deviceLayout.setOnClickListener {
                deviceItemClickListener.onConnectedDevicesClicked(
                    devicesInfo = connectedData
                )
            }
            deviceSignalStrength.setOnClickListener {
                deviceItemClickListener.onConnectionStatusChanged(connectedData)
                notifyDataSetChanged()
            }
            return layoutConnectedDevicesBinding.root
        } else if (groupPosition == 1) {
            val layoutBlockedDevicesBinding =
                LayoutBlockedDevicesBinding.inflate(layoutInflater)
            val deviceLayout = layoutBlockedDevicesBinding.blockedDeviceName
            val blockedData = getChild(groupPosition, childPosition)
            val blockedDeviceName = layoutBlockedDevicesBinding.blockedDeviceName

            blockedDeviceName.text = blockedData.hostName
            deviceLayout.setOnClickListener {
                deviceItemClickListener.onRemovedDevicesClicked(
                    devicesInfo = blockedData
                )
            }
            return layoutBlockedDevicesBinding.root
        }
        return layoutConnectedDevicesBinding.root
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return deviceList.size
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