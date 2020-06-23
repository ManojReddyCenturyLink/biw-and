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

class DeviceListAdapter(private val deviceList: HashMap<DeviceStatus, List<DevicesData>>) :
    BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): DeviceStatus {
        return if (groupPosition == 0) {
            DeviceStatus.CONNECTED
        } else {
            DeviceStatus.BLOCKED
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
        var recyclerGroupView = convertView
        val layoutInflater =
            parent?.context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return if (groupPosition == 0) {
            recyclerGroupView = layoutInflater.inflate(R.layout.layout_header_devicesconnected, parent)
            val deviceCount =
                recyclerGroupView!!.findViewById<TextView>(R.id.devices_group_count)
            val listStatusIcon =
                recyclerGroupView.findViewById<ImageView>(R.id.devices_header_arrow)
            deviceCount.text = parent.context!!.getString(
                R.string.connected_devices,
                getChildrenCount(groupPosition)
            )

            if (isExpanded) {
                listStatusIcon.setImageResource(R.drawable.ic_faq_down)
            } else {
                listStatusIcon.setImageResource(R.drawable.ic_icon_right)
            }
            recyclerGroupView
        } else {
            recyclerGroupView = layoutInflater.inflate(R.layout.layout_devicelist_group_blocked, parent)
            recyclerGroupView
        }
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return if (groupPosition == 0) {
            deviceList[DeviceStatus.CONNECTED]!!.size
        } else {
            deviceList[DeviceStatus.BLOCKED]!!.size
        }
    }

    override fun getChild(groupPosition: Int, childPosition: Int): DevicesData {
        return if (groupPosition == 0) {
            deviceList[DeviceStatus.CONNECTED]!![childPosition]
        } else {
            deviceList[DeviceStatus.BLOCKED]!![childPosition]
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
            recyclerChildView = layoutInflater.inflate(R.layout.layout_connected_devices, parent)
            val deviceName =
                recyclerChildView!!.findViewById<TextView>(R.id.device_name)
            deviceName.text = connectedData.hostName
        } else if (groupPosition == 1) {
            val blockedData = getChild(groupPosition, childPosition)
            recyclerChildView = layoutInflater.inflate(R.layout.layout_blocked_devices, parent)
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
}