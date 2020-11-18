package com.centurylink.biwf.screens.home.devices

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DevicesCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.FragmentDevicesBinding
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.home.devices.adapter.DeviceListAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import java.util.*
import javax.inject.Inject

/**
 * Devices fragment - This class handle common methods related to devices screen
 *
 * @constructor Create empty Devices fragment
 */
class DevicesFragment : BaseFragment(), DeviceListAdapter.DeviceItemClickListener,
    RecyclerViewExpandableItemManager.OnGroupCollapseListener,
    RecyclerViewExpandableItemManager.OnGroupExpandListener {
    private var eimSavedState: Parcelable? = null
    private lateinit var deviceListAdapter: DeviceListAdapter
    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var devicesCoordinator: DevicesCoordinator
    private lateinit var binding: FragmentDevicesBinding
    private var mWrappedAdapter: RecyclerView.Adapter<*>? = null
    private var mRecyclerViewExpandableItemManager: RecyclerViewExpandableItemManager? = null
    private var mRecyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var mRecyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var mRecyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private val SAVED_STATE_EXPANDABLE_ITEM_MANAGER =
        "RecyclerViewExpandableItemManager"
    var isRefresh = false
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
            updateDevicesListFlow.observe {
                deviceListAdapter.notifyDataSetChanged()
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
        eimSavedState =
            savedInstanceState?.getParcelable<Parcelable>(SAVED_STATE_EXPANDABLE_ITEM_MANAGER)
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

    /**
     * On removed devices clicked - It will handle on remove device click listener
     *
     * @param deviceInfo - The clicked device info
     */
    override fun onRemovedDevicesClicked(deviceInfo: DevicesData) {
        devicesViewModel.logRemoveDevicesItemClick()
        blockDeviceMac = deviceInfo.stationMac!!
        val nickName = if (!deviceInfo.mcAfeeName.isNullOrEmpty()) {
            deviceInfo.mcAfeeName
        } else {
            deviceInfo.hostName ?: ""
        }
        blockDeviceMac = deviceInfo.stationMac ?: ""
        showConfirmationDialog(
            nickName?.toUpperCase(Locale.getDefault())?.capitalize()
        )
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
        // ** Set the colors for the Pull To Refresh View
        activity?.let {
            ContextCompat.getColor(
                it,
                R.color.white
            )
        }?.let {
            binding.pullToRefresh.setProgressBackgroundColorSchemeColor(
                it
            )
        }
        binding.pullToRefresh.setColorSchemeColors(Color.GRAY)
        binding.pullToRefresh.setOnRefreshListener {
            if (!isRefresh) {
                devicesViewModel.initApis()
                isRefresh = true
            }
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

        val mLayoutManager: LinearLayoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }

            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        mRecyclerViewExpandableItemManager = RecyclerViewExpandableItemManager(eimSavedState)
        mRecyclerViewExpandableItemManager?.setOnGroupExpandListener(this)
        mRecyclerViewExpandableItemManager?.setOnGroupCollapseListener(this)
        mRecyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()

        val animator: RecyclerView.ItemAnimator? = binding.devicesList.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        mRecyclerViewTouchActionGuardManager?.isEnabled = true
        mRecyclerViewDragDropManager = RecyclerViewDragDropManager()
        mRecyclerViewSwipeManager = RecyclerViewSwipeManager()
        deviceListAdapter = DeviceListAdapter(
            deviceList = deviceStatus.deviceSortMap,
            deviceListItemClickListener = this
        )
        mWrappedAdapter =
            deviceListAdapter.let { mRecyclerViewExpandableItemManager?.createWrappedAdapter(it) } // wrap for expanding

        mWrappedAdapter =
            mWrappedAdapter?.let { mRecyclerViewDragDropManager?.createWrappedAdapter(it) } // wrap for dragging

        mWrappedAdapter =
            mWrappedAdapter?.let { mRecyclerViewSwipeManager?.createWrappedAdapter(it) } // wrap for swiping
        binding.devicesList.layoutManager = mLayoutManager
        binding.devicesList.adapter = mWrappedAdapter // requires *wrapped* adapter
        mRecyclerViewTouchActionGuardManager?.attachRecyclerView(binding.devicesList)
        mRecyclerViewSwipeManager?.attachRecyclerView(binding.devicesList)
        mRecyclerViewDragDropManager?.attachRecyclerView(binding.devicesList)
        mRecyclerViewExpandableItemManager?.attachRecyclerView(binding.devicesList)
    }

    /**
     * Show confirmation dialog
     *
     * @param vendorName
     */
    private fun showConfirmationDialog(vendorName: String?) {
        activity?.supportFragmentManager?.let {
            CustomDialogGreyTheme(
                getString(R.string.restore_access_confirmation_title, vendorName),
                getString(R.string.restore_access_confirmation_msg),
                getString(R.string.restore),
                getString(
                    R.string.text_header_cancel
                ),
                ::onDialogCallback
            ).show(it, DevicesFragment::class.simpleName)
        }
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
        super.onResume()
    }

    override fun onGroupExpand(
        groupPosition: Int,
        fromUser: Boolean,
        payload: Any?
    ) {
        adjustScrollPositionOnGroupExpanded(groupPosition)
    }

    private fun adjustScrollPositionOnGroupExpanded(groupPosition: Int) {
        mRecyclerViewExpandableItemManager?.scrollToGroup(
            groupPosition,
            0,
            0,
            0
        )
    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
    }
}
