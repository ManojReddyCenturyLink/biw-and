package com.centurylink.biwf.screens.home.devices

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

class DevicesFragment : BaseFragment(), DeviceListAdapter.DeviceItemClickListener,
    CustomDialogGreyTheme.DialogCallback {

    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var devicesCoordinator: DevicesCoordinator

    private lateinit var binding: FragmentDevicesBinding

    private lateinit var deviceAdapter: DeviceListAdapter

    private var blockDeviceMac: String = ""
    private val supportFragmentManager by lazy { activity?.supportFragmentManager }

    private val devicesViewModel by lazy {
        ViewModelProvider(this, factory).get(DevicesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
        devicesViewModel.apply {
            devicesListFlow.observe {
                populateDeviceList(it)
            }
        }
    }

    override fun retryClicked() {
        showProgress(true)
        devicesViewModel.initApis()
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

    override fun onConnectedDevicesClicked(devicesInfo: DevicesData) {
        devicesViewModel.navigateToUsageDetails(devicesInfo)
    }

    override fun onRemovedDevicesClicked(deviceInfo: DevicesData) {
        blockDeviceMac = deviceInfo.stationMac!!
        showConfirmationDialog(deviceInfo.vendorName?.toLowerCase(Locale.getDefault())?.capitalize())
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
        binding.pullToRefresh.isEnabled =false
        binding.pullToRefresh.setOnRefreshListener {
            devicesViewModel.initApis()
            binding.pullToRefresh.isRefreshing = false
        }
        binding.devicesList.isEnabled = true
        binding.devicesList.setAdapter(deviceAdapter)
    }

    private fun observeViews() {
        devicesViewModel.apply {
            progressViewFlow.observe {
                showProgress(it)
            }
            errorMessageFlow.observe {
                showRetry(it.isNotEmpty())
            }
        }
    }

    private fun populateDeviceList(deviceStatus: DevicesViewModel.UIDevicesTypeDetails) {
        deviceAdapter.deviceList = deviceStatus.deviceSortMap
        deviceAdapter.isModemAlive = deviceStatus.isModemAlive
        deviceAdapter.notifyDataSetChanged()
        if (deviceAdapter.deviceList.size > 1) {
            binding.devicesList.expandGroup(1)
        }
        binding.devicesList.setOnGroupClickListener { _, _, groupPosition, _ ->
            if (groupPosition == 1) {
                binding.devicesList.expandGroup(1)
                return@setOnGroupClickListener true
            }
            return@setOnGroupClickListener false
        }
        binding.devicesList.expandGroup(0)
    }

    private fun showConfirmationDialog(vendorName: String?) {
        CustomDialogGreyTheme(
            getString(R.string.restore_access_confirmation_title, vendorName),
            getString(R.string.restore_access_confirmation_msg),
            getString(R.string.restore),
            getString(
                R.string.text_header_cancel
            )
        ).show(supportFragmentManager!!, activity)
    }

    // Callbacks for the Dialog
    override fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                if(blockDeviceMac.isNullOrEmpty()){
                   // devicesViewModel.unblockDevice(blockDeviceMac)
                   // blockDeviceMac = ""

                }
            }
            AlertDialog.BUTTON_NEGATIVE -> {
            }
        }
    }
}