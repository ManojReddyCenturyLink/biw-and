package com.centurylink.biwf.screens.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.databinding.LayoutAccountBinding
import com.centurylink.biwf.databinding.LayoutDashboardBinding
import com.centurylink.biwf.databinding.LayoutDevicesBinding
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.screens.home.account.AccountFragment
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.screens.home.devices.DevicesFragment
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class TabsPagerRecyclerAdapter(
    private val mContext: Context,
    private val viewClickListener: DashboardFragment.ViewClickListener
) :
    ListAdapter<TabsBaseItem, TabsPagerRecyclerAdapter.BaseViewHolder>(
        TabAdapterDiffUtil()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        return when (viewType) {

            TabsBaseItem.DEVICES -> {
                val binding =
                    LayoutDevicesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                DevicesViewHolder(binding)
            }
            TabsBaseItem.ACCOUNT -> {
                val binding =
                    LayoutAccountBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                AccountViewHolder(binding)
            }
            else -> {
                val binding =
                    LayoutDashboardBinding.inflate(
                        LayoutInflater.from(parent.context),
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
                holder.setupFragment()
            }
            TabsBaseItem.DASHBOARD -> {
                holder as DashboardViewHolder
                holder.setupFragment()
            }
            TabsBaseItem.ACCOUNT -> {
                holder as AccountViewHolder
                holder.setupFragment()
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

    inner class DevicesViewHolder(private val binding: LayoutDevicesBinding) :
        BaseViewHolder(binding.root),
        View.OnClickListener {
        override fun onClick(v: View?) {
            Timber.e("onclick function for device view holder")
        }

        fun setupFragment() {
            val activity = mContext as AppCompatActivity
            val myFragment: DevicesFragment = DevicesFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.devices_container, myFragment).commit()
        }
    }

    inner class DashboardViewHolder(private val binding: LayoutDashboardBinding) :
        BaseViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {
            Timber.e("onclick function for dashboard view holder")
        }

        fun setupFragment() {
            val activity = mContext as AppCompatActivity
            val newUser = getItem(adapterPosition).bundle.getBoolean("NEW_USER", false)
            val myFragment: DashboardFragment = DashboardFragment(newUser)
            myFragment.setListener(viewClickListener)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container, myFragment).commit()
        }
    }

    inner class AccountViewHolder(private val binding: LayoutAccountBinding) :
        BaseViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {
            Timber.e("onclick function for account view holder")
        }

        fun setupFragment() {
            val activity = mContext as AppCompatActivity
            val myFragment: Fragment = AccountFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.account_container, myFragment).commit()
        }
    }

    class TabAdapterDiffUtil : DiffUtil.ItemCallback<TabsBaseItem>() {
        override fun areItemsTheSame(oldItem: TabsBaseItem, newItem: TabsBaseItem): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: TabsBaseItem, newItem: TabsBaseItem): Boolean {
            return false
        }
    }
}
