package com.centurylink.biwf.screens.subscription.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.centurylink.biwf.R
import kotlinx.android.synthetic.main.cancellation_reason_item.view.*

class CancellationReasonAdapter(
    internal var context: Context,
    internal var cancellationList:Array<String>
) :
    BaseAdapter() {

    internal var inflter: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflter.inflate(R.layout.cancellation_reason_item, null)
        val cancellationItem: TextView = view.cancellation_reason_item
        cancellationItem.text = cancellationList[position]
        return view
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0

    }

    override fun getCount(): Int {
        return cancellationList.size
    }
}