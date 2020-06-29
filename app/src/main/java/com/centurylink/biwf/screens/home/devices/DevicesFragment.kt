package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DevicesCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.FragmentDevicesBinding
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.adapter.DeviceListAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

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

    override fun onDevicesClicked(devicesInfo: DevicesData) {
        devicesViewModel.navigateToUsageDetails(devicesInfo)
    }

    private fun initViews() {
        deviceAdapter = DeviceListAdapter(
            deviceList = HashMap<DeviceStatus, List<DevicesData>>(),
            deviceItemClickListener = this
        )
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
    }
}