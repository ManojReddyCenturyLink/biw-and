package com.centurylink.biwf.screens.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.databinding.LayoutAccountBinding
import com.centurylink.biwf.databinding.LayoutDashboardBinding
import com.centurylink.biwf.databinding.LayoutDevicesBinding
import com.centurylink.biwf.model.TabsBaseItem

@Suppress("UNCHECKED_CAST")
class TabsPagerRecyclerAdapter(private val mContext: Context) :
    ListAdapter<TabsBaseItem, TabsPagerRecyclerAdapter.BaseViewHolder>(
        TabAdapterDiffUtil()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        return when (viewType) {

            TabsBaseItem.DEVICES -> {

                val binding = DataBindingUtil.inflate<LayoutDevicesBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_devices,
                    parent,
                    false
                )
                DevicesViewHolder(binding)
            }
            TabsBaseItem.DASHBOARD -> {

                val binding = DataBindingUtil.inflate<LayoutDashboardBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_dashboard,
                    parent,
                    false
                )
                DashboardViewHolder(binding)
            }
            TabsBaseItem.ACCOUNT -> {

                val binding = DataBindingUtil.inflate<LayoutAccountBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_account,
                    parent,
                    false
                )
                AccountViewHolder(binding)
            }

            else -> {

                val binding = DataBindingUtil.inflate<LayoutDashboardBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_dashboard,
                    parent,
                    false
                )
                DashboardViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

        when (getItemViewType(holder.adapterPosition)) {

            TabsBaseItem.DEVICES -> {
                holder as DevicesViewHolder
            }
            TabsBaseItem.DASHBOARD -> {
                holder as DashboardViewHolder
            }
            TabsBaseItem.ACCOUNT -> {
                holder as AccountViewHolder
            }
            else -> {
                holder as DashboardViewHolder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.indextype
    }

    init {
        hasStableIds()
    }

    open inner class BaseViewHolder(v: View) : RecyclerView.ViewHolder(v)

    inner class DevicesViewHolder(private val binding: LayoutDevicesBinding) : BaseViewHolder(binding.root),
        View.OnClickListener {
        override fun onClick(v: View?) {
        }
    }

    inner class DashboardViewHolder(private val binding: LayoutDashboardBinding) :
        BaseViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {
        }
    }

    inner class AccountViewHolder(private val binding: LayoutAccountBinding) : BaseViewHolder(binding.root),
        View.OnClickListener {
        override fun onClick(v: View?) {
        }
    }

    class TabAdapterDiffUtil : DiffUtil.ItemCallback<TabsBaseItem>() {
        override fun areItemsTheSame(oldItem: TabsBaseItem, newItem: TabsBaseItem): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun areContentsTheSame(oldItem: TabsBaseItem, newItem: TabsBaseItem): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}