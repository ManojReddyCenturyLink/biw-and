package com.centurylink.biwf.screens.home.devices.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.DeviceStatus

class DeviceListAdapter(
    private val deviceList: HashMap<DeviceStatus, List<DevicesData>>,
    private val deviceItemClickListener: DeviceItemClickListener
) :
    BaseExpandableListAdapter() {

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
        var recylerGroupView = convertView
        return if (groupPosition == 0) {
            val layoutInflater =
                parent?.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            recylerGroupView = layoutInflater.inflate(R.layout.layout_header_devicesconnected, null)
            val deviceCount =
                recylerGroupView!!.findViewById<TextView>(R.id.devices_group_count)
            val listStatusIcon =
                recylerGroupView.findViewById<ImageView>(R.id.devices_header_arrow)
            deviceCount.text = getChildrenCount(groupPosition).toString()
            if (isExpanded) {
                listStatusIcon.setImageResource(R.drawable.ic_faq_down)
            } else {
                listStatusIcon.setImageResource(R.drawable.ic_icon_right)
            }
            recylerGroupView
        } else {
            val layoutInflater =
                parent?.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            recylerGroupView =
                layoutInflater.inflate(R.layout.layout_devicelist_group_blocked, null)
            recylerGroupView
        }
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return when (groupPosition) {
            0 -> deviceList[DeviceStatus.CONNECTED]!!.size
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
        var recyclerChildView = convertView
        val layoutInflater =
            parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (groupPosition == 0) {
            val connectedData = getChild(groupPosition, childPosition)
            recyclerChildView = layoutInflater.inflate(R.layout.layout_connected_devices, null)
            val deviceName =
                recyclerChildView!!.findViewById<TextView>(R.id.device_name)
            val deviceSignalStrength =
                recyclerChildView.findViewById<ImageView>(R.id.iv_network_type)
            deviceName.text = connectedData.hostName

            deviceSignalStrength.setImageResource(setSignalStatus(connectedData.rssi!!,connectedData.connectedInterface))

            recyclerChildView.setOnClickListener {
                deviceItemClickListener.onDevicesClicked(
                    devicesInfo = connectedData
                )
            }
        } else if (groupPosition == 1) {
            val blockedData = getChild(groupPosition, childPosition)
            recyclerChildView = layoutInflater.inflate(R.layout.layout_blocked_devices, null)
            val blockedDeviceName =
                recyclerChildView!!.findViewById<TextView>(R.id.blocked_device_name)
            blockedDeviceName.text = blockedData.hostName
        }
        return recyclerChildView!!
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return deviceList.size
    }

    private fun setSignalStatus(signalStrength: Int,connectionMode:String?): Int {
        if (!connectionMode.isNullOrEmpty() && connectionMode.equals("Ethernet", true)) {
           return R.drawable.ic_ethernet
        }
        return when (signalStrength) {
            in -50..-1 -> {
                R.drawable.ic_strong_signal
            }
            in -51 downTo -75 -> {
                R.drawable.ic_medium_signal
            }
            in -76 downTo -90 -> {
                R.drawable.ic_weak_signal
            }
            else -> {
                R.drawable.ic_off
            }
        }
    }

    interface DeviceItemClickListener {

        /**
         * Handle click event on Item click
         *
         */
        fun onDevicesClicked(devicesInfo: DevicesData)

    }
}