package com.centurylink.biwf.screens.home.devices.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.DeviceStatus

class DeviceListAdapter(private val deviceList: HashMap<DeviceStatus, List<DevicesData>>) :
    BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): DeviceStatus {
        when (groupPosition) {
            0 -> return DeviceStatus.CONNECTED_DEVICES
            1 -> return DeviceStatus.BLOCKED_DEVICES
        }
        return DeviceStatus.CONNECTED_DEVICES
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
        var convertView = convertView
        return if (groupPosition == 0) {
            val layoutInflater =
                parent?.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.layout_header_devicesconnected, null)
            val deviceCount =
                convertView!!.findViewById<TextView>(R.id.devices_group_count)
            deviceCount.text = parent.context!!.getString(
                R.string.connected_devices,
                getChildrenCount(groupPosition)
            )
            convertView
        } else {
            val layoutInflater =
                parent?.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.layout_devicelist_group_blocked, null)
            convertView
        }
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        when (groupPosition) {
            0 -> return deviceList[DeviceStatus.CONNECTED_DEVICES]!!.size
            1 -> return deviceList[DeviceStatus.BLOCKED_DEVICES]!!.size
        }
        return 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): DevicesData {
        return if (groupPosition == 0) {
            deviceList[DeviceStatus.CONNECTED_DEVICES]!![childPosition]
        } else {
            deviceList[DeviceStatus.BLOCKED_DEVICES]!![childPosition]
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
        var convertView = convertView
        if (groupPosition == 0) {
            if (convertView == null) {
                val connectedData = getChild(groupPosition, childPosition)
                val layoutInflater =
                    parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = layoutInflater.inflate(R.layout.layout_connected_devices, null)
                val deviceName =
                    convertView!!.findViewById<TextView>(R.id.device_name)
                deviceName.text = connectedData.hostName
            }
            return convertView
        } else {
            if (convertView == null) {
                val blockedData = getChild(groupPosition, childPosition)
                val layoutInflater =
                    parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = layoutInflater.inflate(R.layout.layout_blocked_devices, null)
                val blockeddeviceName =
                    convertView!!.findViewById<TextView>(R.id.blocked_device_name)
                blockeddeviceName.text = blockedData.hostName
            }
            return convertView
        }
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return deviceList.size
    }
}