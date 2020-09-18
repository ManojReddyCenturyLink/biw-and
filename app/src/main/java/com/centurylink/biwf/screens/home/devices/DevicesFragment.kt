package com.centurylink.biwf.screens.home.devices

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DevicesCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.FragmentDevicesBinding
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.adapter.DeviceListAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class DevicesFragment : BaseFragment(), DeviceListAdapter.DeviceItemClickListener {

    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var devicesCoordinator: DevicesCoordinator

    private lateinit var binding: FragmentDevicesBinding

    private lateinit var deviceAdapter: DeviceListAdapter
    var isRefresh = false

    private var blockDeviceMac: String = ""

    private val devicesViewModel by lazy {
        ViewModelProvider(this, factory).get(DevicesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
        devicesViewModel.apply {
            devicesListFlow.observe {
                Log.i("JAQUAR", "STATE CHANGED")
                populateDeviceList(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDevicesBinding.inflate(inflater)
        devicesViewModel.myState.observeWith(devicesCoordinator)
        setApiProgressViews(
            binding.dashboardViews,
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.retryOverlay.root
        )
        initViews()
        observeViews()
        return binding.root
    }

    override fun retryClicked() {
        showProgress(true)
        devicesViewModel.initApis()
    }

    override fun onConnectedDevicesClicked(devicesInfo: DevicesData) {
        devicesViewModel.navigateToUsageDetails(devicesInfo)
    }

    override fun onRemovedDevicesClicked(deviceInfo: DevicesData) {
        disableSwipeToRefresh()
        devicesViewModel.logRemoveDevicesItemClick()
        blockDeviceMac = deviceInfo.stationMac!!
        showConfirmationDialog(
            deviceInfo.hostName?.toUpperCase(Locale.getDefault())?.capitalize()
        )
    }

    override fun onConnectionStatusChanged(devicesData: DevicesData) {
        devicesViewModel.logConnectionStatusChanged(devicesData.isPaused)
        devicesViewModel.updatePauseResumeStatus(devicesData)
    }

    private fun initViews() {
        deviceAdapter = DeviceListAdapter(
            deviceList = HashMap(),
            deviceItemClickListener = this
        )
        //** Set the colors for the Pull To Refresh View
        binding.pullToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                activity!!,
                R.color.white
            )
        )
        binding.pullToRefresh.setColorSchemeColors(Color.GRAY)
        binding.pullToRefresh.setOnRefreshListener {
            if (!isRefresh) {
                devicesViewModel.initApis()
                isRefresh = true
            }
        }
        binding.devicesList.isEnabled = true
        binding.devicesList.setAdapter(deviceAdapter)

        binding.devicesList.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    enableSwipeToRefresh()
                }
                MotionEvent.ACTION_CANCEL -> {
                    enableSwipeToRefresh()
                }

            }
            false
        })

        binding.devicesList.setOnGroupCollapseListener {
            disableSwipeToRefresh()
        }

        binding.devicesList.setOnGroupExpandListener {
            disableSwipeToRefresh()
        }
    }

    private fun observeViews() {
        devicesViewModel.apply {
            progressViewFlow.observe {
                showProgress(it)
                stopSwipeToRefresh()
            }
            errorMessageFlow.observe {
                showRetry(it.isNotEmpty())
                stopSwipeToRefresh()
            }
        }
    }

    private fun populateDeviceList(deviceStatus: DevicesViewModel.UIDevicesTypeDetails) {
        deviceAdapter.deviceList.clear()
        deviceAdapter.notifyDataSetChanged()
        deviceAdapter.deviceList = deviceStatus.deviceSortMap
        deviceAdapter.isModemAlive = deviceStatus.isModemAlive
        deviceAdapter.notifyDataSetChanged()
        if (deviceAdapter.deviceList.size > 1) {
            binding.devicesList.expandGroup(1)
        }
        if (!deviceStatus.deviceSortMap[DeviceStatus.CONNECTED].isNullOrEmpty()) {
            binding.devicesList.expandGroup(0)
        }
        binding.devicesList.setOnGroupClickListener { _, _, groupPosition, _ ->
            disableSwipeToRefresh()
            return@setOnGroupClickListener false
        }

    }

    private fun showConfirmationDialog(vendorName: String?) {
        CustomDialogGreyTheme(
            getString(R.string.restore_access_confirmation_title, vendorName),
            getString(R.string.restore_access_confirmation_msg),
            getString(R.string.restore),
            getString(
                R.string.text_header_cancel
            ),
            ::onDialogCallback
        ).show(activity?.supportFragmentManager!!, DevicesFragment::class.simpleName)
    }

    // Callbacks for the Dialog
    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                devicesViewModel.logRestoreConnection(true)
                if (!blockDeviceMac.isNullOrEmpty()) {
                    devicesViewModel.unblockDevice(blockDeviceMac)
                    blockDeviceMac = ""
                }
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                devicesViewModel.logRestoreConnection(false)
                blockDeviceMac = ""
            }
        }
    }

    private fun stopSwipeToRefresh() {
        binding.pullToRefresh.isRefreshing = false
        isRefresh = false
    }

    private fun disableSwipeToRefresh() {
        binding.pullToRefresh.isEnabled = false
    }

    private fun enableSwipeToRefresh() {
        binding.pullToRefresh.isEnabled = true
    }

    override fun onResume() {
        disableSwipeToRefresh()
        super.onResume()
    }
}