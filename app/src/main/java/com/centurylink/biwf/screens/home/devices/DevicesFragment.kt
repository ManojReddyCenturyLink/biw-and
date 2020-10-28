package com.centurylink.biwf.screens.home.devices

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
import android.widget.AbsListView.TRANSCRIPT_MODE_NORMAL
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

/**
 * Devices fragment - This class handle common methods related to devices screen
 *
 * @constructor Create empty Devices fragment
 */
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
    private var isRefresh = false

    private var blockDeviceMac: String = ""

    private val devicesViewModel by lazy {
        ViewModelProvider(this, factory).get(DevicesViewModel::class.java)
    }

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
        devicesViewModel.apply {
            devicesListFlow.observe {
                populateDeviceList(it)
            }
        }
    }

    /**
     * On create view - The onCreateView method is called when Fragment should create its View
     *                  object hierarchy
     *
     * @param inflater - LayoutInflater: The LayoutInflater object that can be used to
     *                   inflate any views in the fragment,
     * @param container - ViewGroup: If non-null, this is the parent view that the fragment's UI
     *                    should be attached to. The fragment should not add the view itself,
     *                    but this can be used to generate the LayoutParams of the view.
     *                    This value may be null.
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed
     * @return - Return the View for the fragment's UI, or null.
     */
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

    /**
     * Retry clicked - It will handle the retry functionality
     *
     */
    override fun retryClicked() {
        showProgress(true)
        devicesViewModel.initApis()
    }

    /**
     * On connected devices clicked - It will handle on connected device click listener
     *
     * @param devicesInfo - The clicked device info
     */
    override fun onConnectedDevicesClicked(devicesInfo: DevicesData) {
        devicesViewModel.navigateToUsageDetails(devicesInfo)
    }

    //TODO: Commenting temporarily remove devices api is not working.
    /**
     * On removed devices clicked - It will handle on remove device click listener
     *
     * @param deviceInfo - The clicked device info
     */
    override fun onRemovedDevicesClicked(deviceInfo: DevicesData) {
//        binding.devicesList.transcriptMode = TRANSCRIPT_MODE_ALWAYS_SCROLL
//        disableSwipeToRefresh()
//        devicesViewModel.logRemoveDevicesItemClick()
//        blockDeviceMac = deviceInfo.stationMac!!
//        val nickName = if (!deviceInfo.mcAfeeName.isNullOrEmpty()) {
//            deviceInfo.mcAfeeName
//        } else {
//            deviceInfo.hostName ?: ""
//        }
//        showConfirmationDialog(
//            nickName?.toUpperCase(Locale.getDefault())?.capitalize()
//        )
    }

    /**
     * On connection status changed
     *
     * @param devicesData
     */
    override fun onConnectionStatusChanged(devicesData: DevicesData) {
        devicesViewModel.logConnectionStatusChanged(devicesData.isPaused)
        devicesViewModel.updatePauseResumeStatus(devicesData)
    }

    /**
     * Init views - It initializes the device fragment views
     *
     */
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

    /**
     * Observe views - It is used to observe views
     *
     */
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
            devicesViewModel.logListExpandCollapse()
            return@setOnGroupClickListener false
        }
    }

    /**
     * Show confirmation dialog
     *
     * @param vendorName
     */
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

    /**
     * On dialog callback - It will handle the dialog callback listeners
     *
     * @param buttonType - It returns the which button type is pressed negative or positive
     */
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

    /**
     * Stop swipe to refresh
     *
     */
    private fun stopSwipeToRefresh() {
        binding.pullToRefresh.isRefreshing = false
        isRefresh = false
    }

    /**
     * Disable swipe to refresh
     *
     */
    private fun disableSwipeToRefresh() {
        binding.pullToRefresh.isEnabled = false
    }

    /**
     * Enable swipe to refresh
     *
     */
    private fun enableSwipeToRefresh() {
        binding.pullToRefresh.isEnabled = true
    }

    /**
     * On resume - Called when the fragment is visible to the user and actively running
     *
     */
    override fun onResume() {
        devicesViewModel.logScreenLaunch()
        binding.devicesList.transcriptMode = TRANSCRIPT_MODE_NORMAL
        disableSwipeToRefresh()
        super.onResume()
    }
}