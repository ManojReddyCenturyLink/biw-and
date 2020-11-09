package com.centurylink.biwf.screens.home.dashboard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DashboardCoordinator
import com.centurylink.biwf.databinding.FragmentDashboardBinding
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.screens.home.SpeedTestUtils
import com.centurylink.biwf.screens.home.dashboard.adapter.WifiDevicesAdapter
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_dashboard.incCanceled
import kotlinx.android.synthetic.main.fragment_dashboard.incCompleted
import kotlinx.android.synthetic.main.fragment_dashboard.incEnroute
import kotlinx.android.synthetic.main.fragment_dashboard.incScheduled
import kotlinx.android.synthetic.main.fragment_dashboard.incSpeedTest
import kotlinx.android.synthetic.main.fragment_dashboard.incWorkBegun
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.*
import kotlinx.android.synthetic.main.widget_installation_completed.view.*
import kotlinx.android.synthetic.main.widget_status_enroute.view.*
import kotlinx.android.synthetic.main.widget_status_work_begun.view.*
import kotlinx.android.synthetic.main.widget_welcome_card.view.msg
import kotlinx.android.synthetic.main.widget_welcome_card.view.msg_dismiss_button
import kotlinx.android.synthetic.main.widget_welcome_card.view.title
import javax.inject.Inject

/**
 * Dashboard fragment - Fragment class for the Dashboard document details
 *
 * @constructor Create empty Dashboard fragment
 */
class DashboardFragment : BaseFragment(), WifiDevicesAdapter.WifiDeviceClickListener {
    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var dashboardCoordinator: DashboardCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private lateinit var wifiDevicesAdapter: WifiDevicesAdapter

    private val dashboardViewModel by lazy {
        ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var viewClickListener: ViewClickListener
    private val fragManager by lazy { activity?.supportFragmentManager }

    private var unreadNotificationList: MutableList<Notification> = mutableListOf()
    private var enrouteMapFragment: SupportMapFragment? = null
    private var workBegunMapFragment: SupportMapFragment? = null
    private var originLatLng = LatLng(0.0, 0.0)
    private var speedTestCount: Int = 0
    // private var destinationLatLng = LatLng(0.0, 0.0)

    /**
     * On create - The onCreate method is called when Fragment should create its View
     *                  object hierarchy
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    /**
     * On resume - Called when the fragment is visible to the user and actively running
     *
     */
    override fun onResume() {
        super.onResume()
        dashboardViewModel.apply {
            logScreenLaunch()
            if (SpeedTestUtils.isSpeedTestAvailable()) {
                checkForOngoingSpeedTest()
            }
        }
        initButtonStates()
        updateView()
        listenForRebootDialog()
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
        binding = FragmentDashboardBinding.inflate(inflater)
        setApiProgressViews(
            binding.dashboardViews,
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.retryOverlay.root
        )
        dashboardViewModel.progressViewFlow.observe {
            showProgress(it)
        }
        dashboardViewModel.errorMessageFlow.observe {
            showRetry(it.isNotEmpty())
        }

        dashboardViewModel.cancelAppointmentError.observe {
            CustomDialogBlueTheme(
                getString(R.string.error_title),
                it,
                getString(R.string.ok),
                true,
                ::onErrorDialogCallback
            ).show(fragManager!!, DashboardFragment::class.simpleName)
        }
        dashboardViewModel.downloadSpeed.observe {
            binding.incSpeedTest.downloadSpeed.text = it
        }
        dashboardViewModel.uploadSpeed.observe {
            binding.incSpeedTest.uploadSpeed.text = it
        }
        dashboardViewModel.latestSpeedTest.observe {
            binding.incSpeedTest.lastSpeedTestTime.text = it
        }

        dashboardViewModel.connectedDevicesNumber.observe {
            binding.connectedDevicesCard.devicesConnectedNo.text = it
        }
        dashboardViewModel.progressVisibility.observe {
            binding.incSpeedTest.uploadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
            binding.incSpeedTest.downloadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
            binding.incSpeedTest.downloadProgressIcon.visibility =
                if (it) View.VISIBLE else View.INVISIBLE
            binding.incSpeedTest.uploadProgressIcon.visibility =
                if (it) View.VISIBLE else View.INVISIBLE
        }
        dashboardViewModel.speedTestError.observe {
            if (it) {
                CustomDialogBlueTheme(
                    title = getString(R.string.speed_test_error_title),
                    message = getString(R.string.speed_test_error_message),
                    buttonText = getString(R.string.ok),
                    isErrorPopup = true,
                    callback = ::onErrorDialogCallback
                ).show(fragManager!!, DashboardFragment::class.simpleName)
            }
        }
        dashboardViewModel.speedTestButtonState.observe { speedTestButtonState ->
            dashboardViewModel.networkStatus.observe { networkStatusOnline ->
                if (networkStatusOnline) {
                    binding.incSpeedTest.runSpeedTestDashboard.isActivated = speedTestButtonState
                    binding.incSpeedTest.runSpeedTestDashboard.isEnabled = speedTestButtonState
                } else {
                    binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
                    binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
                }
            }
        }
        dashboardViewModel.detailedRebootStatusFlow.observe { rebootState ->
            if (rebootState == ModemRebootMonitorService.RebootState.ONGOING) {
                dashboardViewModel.networkStatus.observe { networkStatusOnline ->
                    if (networkStatusOnline) {
                        binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
                        binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
                    } else {
                        binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
                        binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
                    }
                }
            }
        }
        initButtonStates()
        initOnClicks()
        dashboardViewModel.myState.observeWith(dashboardCoordinator)
        return binding.root
    }

    /**
     * On view created - This gives subclasses a chance to initialize themselves once they know
     *                   their view hierarchy has been completely created
     *
     * @param view-View: The View returned by onCreateView(android.view.LayoutInflater,
     *                   android.view.ViewGroup, android.os.Bundle).
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed
     *                            from a previous saved state as given here. This value may be null.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupMap()
        initWifiScanViews()
        listenForRebootDialog()
    }

    /**
     * Retry clicked - It will handle the retry functionality
     */
    override fun retryClicked() {
        showProgress(true)
        dashboardViewModel.initDevicesApis()
    }

    /**
     * Init views - It will initialises the views
     */
    private fun initViews() {
        // TODO right now this feature is not in active so commenting for now
        // observeNotificationViews()
        observeAccountStatusViews()
        observeWifiDetailsViews()
        getAppointmentStatus()
    }

    /**
     * Init button states - It will initialises the buttons states
     */
    private fun initButtonStates() {
        dashboardViewModel.networkStatus.observe { networkStatusOnline ->
            if (!networkStatusOnline) {
                binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
                binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
            }
        }
    }

    /**
     * Observe account status views - It will observe the account status views
     */
    private fun observeAccountStatusViews() {
        dashboardViewModel.isAccountStatus.observe {
            if (it) {
                incCompleted.visibility = View.GONE
                if (SpeedTestUtils.isSpeedTestAvailable()) {
                    displaySpeedTest()
                } else {
                    displayNoSpeedTest()
                }
            } else {
                if (dashboardViewModel.installationStatus) {
                    if (SpeedTestUtils.isSpeedTestAvailable()) {
                        displaySpeedTest()
                    } else {
                        displayNoSpeedTest()
                    }
                } else {
                    incSpeedTest.visibility = View.GONE
                    binding.connectedDevicesCard.root.visibility = View.GONE
                    binding.layoutNetworkList.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Display speed test - It will display the speed test details
     */
    private fun displaySpeedTest() {
        incCompleted.visibility = View.GONE
        incSpeedTest.visibility = View.VISIBLE
        binding.connectedDevicesCard.root.visibility = View.VISIBLE
        binding.layoutNetworkList.visibility = View.VISIBLE
        binding.layoutNetworkList.setBackgroundResource(R.drawable.round_background_no_border)
        dashboardViewModel.networkStatus.observe { networkStatusOnline ->
            if (networkStatusOnline && speedTestCount < 1) {
                dashboardViewModel.startSpeedTest(false)
                speedTestCount++
                binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
                binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
            }
        }
    }

    /**
     * Display Dashboard UI
     */
    private fun displayDashboardUI() {
        binding.connectedDevicesCard.root.visibility = View.VISIBLE
        binding.layoutNetworkList.visibility = View.VISIBLE
        binding.layoutNetworkList.rootView.network_status.visibility = View.GONE
        binding.layoutNetworkList.rootView.tap_to_edit_network.visibility = View.GONE
        binding.layoutNetworkList.rootView.view_divider.visibility = View.GONE
    }

    /**
     * Display no speed test - It will hide the speed test details
     */
    private fun displayNoSpeedTest() {
        incCompleted.visibility = View.GONE
        incSpeedTest.visibility = View.GONE
        displayDashboardUI()
        dashboardViewModel.networkStatus.observe { networkStatusOnline ->
            if (networkStatusOnline && speedTestCount < 1) {
                dashboardViewModel.startSpeedTest(false)
                speedTestCount++
                binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
                binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
            }
        }
    }

    /**
     * Init on clicks - It will initialises the click events
     */
    private fun initOnClicks() {
        binding.incSpeedTest.runSpeedTestDashboard.setOnClickListener {
            dashboardViewModel.startSpeedTest(
                true
            )
        }
        binding.incScheduled.appointmentChangeBtn.setOnClickListener { dashboardViewModel.getChangeAppointment() }
        binding.incScheduled.appointmentCancelBtn.clickWithDebounce {
            dashboardViewModel.logCancelAppointmentClick()
            if (dashboardViewModel.readAppointmentType().contains(HomeViewModel.intsall)) {
                showCancellationConfirmationDialog(getString(R.string.installation_cancellation_confirmation_msg))
            } else {
                showCancellationConfirmationDialog("")
            }
        }
        binding.notificationDismissButton.setOnClickListener {
            if (unreadNotificationList.isNotEmpty()) {
                dashboardViewModel.markNotificationAsRead(unreadNotificationList[0])
            }
        }
        binding.topCard.setOnClickListener {
            if (unreadNotificationList.isNotEmpty()) {
                dashboardViewModel.navigateToNotificationDetails(unreadNotificationList[0])
            }
        }
        binding.connectedDevicesCard.root.setOnClickListener {
            dashboardViewModel.logViewDevicesClick()
            viewClickListener.onViewDevicesClick()
        }
        binding.incCanceled.youAreAllSetMsg.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse(getString(R.string.tel_url))
            startActivity(intent)
        }
        binding.tapToEditNetwork.setOnClickListener { dashboardViewModel.navigateToNetworkInformation() }
    }

    private fun setupMap() {
        val fm = childFragmentManager
        enrouteMapFragment = fm.findFragmentById(R.id.map_enroute_status) as SupportMapFragment
        workBegunMapFragment = fm.findFragmentById(R.id.map_work_begun) as SupportMapFragment
        enrouteMapFragment?.getMapAsync(enrouteOnMapReadyCallback)
        workBegunMapFragment?.getMapAsync(mOnMapReadyCallback)
    }

    /**
     * Enroute on map ready callback - It will handle the enroute state map call back listeners
     */
    private var enrouteOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 16.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.purple_marker))
                )
                /*We’re not going to be getting technician values until after MVP, so commenting for now
                addMarker(
                    MarkerOptions().position(destinationLatLng)
                        .icon(bitMapFromVector(R.drawable.light_blue_marker))
                )*/
                animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 10f))
            }
        }

    /**
     * Map ready callback It will handle the map call back listeners
     */
    private var mOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 16.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.light_blue_marker))
                )
            }
        }

    /**
     * Get appointment status - It will get appointment status info
     */
    private fun getAppointmentStatus() {

        dashboardViewModel.dashBoardDetailsInfo.observe {
            if (it is DashboardViewModel.AppointmentScheduleState) {
                if (it.jobType.contains(HomeViewModel.intsall)) {
                    incScheduled.schedule_appointment_status_title.text =
                        resources.getString(R.string.fiber_installation_status)
                    incScheduled.schedule_appointment_status_progress_state.text =
                        resources.getString(R.string.installation_scheduled)
                    binding.connectedDevicesCard.root.visibility = View.GONE
                    dashboardViewModel.clearNotificationStatus(ServiceStatus.SCHEDULED.name)
                    if (dashboardViewModel.readNotificationStatus(ServiceStatus.SCHEDULED.name)) {
                        incScheduled.incWelcomeCard.visibility = View.GONE
                    } else {
                        incScheduled.incWelcomeCard.visibility = View.VISIBLE
                    }
                    binding.layoutNetworkList.visibility = View.GONE
                } else {
                    incScheduled.schedule_appointment_status_title.text =
                        resources.getString(R.string.service_appointment_status)
                    incScheduled.schedule_appointment_status_progress_state.text =
                        resources.getString(R.string.service_appointment_scheduled)
                    incScheduled.incWelcomeCard.visibility = View.GONE
                }
                if (dashboardViewModel.readCancellationAppointmentStatus()) {
                    if (it.jobType.contains(HomeViewModel.intsall)) {
                        incCanceled.visibility = View.VISIBLE
                        incScheduled.visibility = View.GONE
                    } else {
                        incCanceled.visibility = View.GONE
                        incScheduled.visibility = View.GONE
                    }
                } else {
                    dashboardViewModel.clearAppointmentCancellationStatus()
                    incScheduled.visibility = View.VISIBLE
                    incCanceled.visibility = View.GONE
                    if (!it.jobType.contains(HomeViewModel.intsall)) {
                        incScheduled.incWelcomeCard.visibility = View.GONE
                        displayDashboardUI()
                    }
                }

                incScheduled.schedule_appointment_date_time_card.schedule_appointment_date.text =
                    it.serviceAppointmentDate
                incScheduled.schedule_appointment_date_time_card.schedule_appointment_time.text =
                    getString(
                        R.string.text_time_details,
                        it.serviceAppointmentStartTime,
                        it.serviceAppointmentEndTime
                    )
                incScheduled.incWelcomeCard.msg_dismiss_button.setOnClickListener {
                    dashboardViewModel.logDismissNotification(ServiceStatus.SCHEDULED.name)
                    incScheduled.incWelcomeCard.visibility = View.GONE
                }
                dashboardViewModel.logAppointmentStatusState(1)
                incEnroute.visibility = View.GONE
                incWorkBegun.visibility = View.GONE
                incCompleted.visibility = View.GONE
            }
            if (it is DashboardViewModel.AppointmentEngineerStatus) {
                if (it.jobType.contains(HomeViewModel.intsall)) {
                    incEnroute.enroute_appointment_status_title.text =
                        resources.getString(R.string.fiber_installation_status)
                    incEnroute.incEnrouteCard.msg.text =
                        resources.getString(R.string.enroute_notification_message)
                    binding.connectedDevicesCard.root.visibility = View.GONE
                    binding.layoutNetworkList.visibility = View.GONE
                } else {
                    incEnroute.enroute_appointment_status_title.text =
                        resources.getString(R.string.service_appointment_status)
                    incEnroute.incEnrouteCard.msg.text =
                        resources.getString(R.string.service_appointment_enroute_notification_message)
                    displayDashboardUI()
                }
                incEnroute.enroute_technician_name.text = it.serviceEngineerName
                incEnroute.enroute_appointment_time.text = it.serviceAppointmentTime
                incEnroute.incEnrouteCard.title.text =
                    resources.getString(R.string.technician_on_the_way)
                dashboardViewModel.clearNotificationStatus(ServiceStatus.EN_ROUTE.name)
                if (dashboardViewModel.readNotificationStatus(ServiceStatus.EN_ROUTE.name)) {
                    incEnroute.incEnrouteCard.visibility = View.GONE
                } else {
                    incEnroute.incEnrouteCard.visibility = View.VISIBLE
                }
                incEnroute.incEnrouteCard.msg_dismiss_button.setOnClickListener {
                    dashboardViewModel.logDismissNotification(ServiceStatus.EN_ROUTE.name)
                    incEnroute.incEnrouteCard.visibility = View.GONE
                }
                originLatLng = LatLng(it.serviceLatitude.toDouble(), it.serviceLongitude.toDouble())
                dashboardViewModel.logAppointmentStatusState(2)

                incScheduled.visibility = View.GONE
                incEnroute.visibility = View.VISIBLE
                incWorkBegun.visibility = View.GONE
                incCompleted.visibility = View.GONE
                incCanceled.visibility = View.GONE
            }
            if (it is DashboardViewModel.AppointmentEngineerWIP) {
                if (it.jobType.contains(HomeViewModel.intsall)) {
                    incWorkBegun.work_begun_appointment_status_title.text =
                        resources.getString(R.string.fiber_installation_status)
                    incWorkBegun.work_begun_appointment_status_progress_state.text =
                        resources.getString(R.string.installation_underway)
                    incWorkBegun.incWipCard.msg.text =
                        resources.getString(R.string.work_begun_message)
                    incWorkBegun.work_begun_appointment_text.text =
                        resources.getString(R.string.is_setting_up_your_network_now)
                    binding.connectedDevicesCard.root.visibility = View.GONE
                    binding.layoutNetworkList.visibility = View.GONE
                } else {
                    incWorkBegun.work_begun_appointment_status_title.text =
                        resources.getString(R.string.service_appointment_status)
                    incWorkBegun.work_begun_appointment_status_progress_state.text =
                        resources.getString(R.string.service_underway)
                    incWorkBegun.incWipCard.msg.text =
                        resources.getString(R.string.service_appointment_work_begun_message)
                    incWorkBegun.work_begun_appointment_text.text =
                        resources.getString(R.string.is_working_now)
                    displayDashboardUI()
                }
                incWorkBegun.work_begun_technician_name.text = it.serviceEngineerName
                incWorkBegun.incWipCard.title.text =
                    resources.getString(R.string.work_in_progress)
                dashboardViewModel.clearNotificationStatus(ServiceStatus.WORK_BEGUN.name)
                if (dashboardViewModel.readNotificationStatus(ServiceStatus.WORK_BEGUN.name)) {
                    incWorkBegun.incWipCard.visibility = View.GONE
                } else {
                    incWorkBegun.incWipCard.visibility = View.VISIBLE
                }
                incWorkBegun.incWipCard.msg_dismiss_button.setOnClickListener {
                    dashboardViewModel.logDismissNotification(ServiceStatus.WORK_BEGUN.name)
                    incWorkBegun.incWipCard.visibility = View.GONE
                }
                originLatLng = LatLng(it.serviceLatitude.toDouble(), it.serviceLongitude.toDouble())
                dashboardViewModel.logAppointmentStatusState(3)

                incScheduled.visibility = View.GONE
                incEnroute.visibility = View.GONE
                incWorkBegun.visibility = View.VISIBLE
                incCompleted.visibility = View.GONE
                incCanceled.visibility = View.GONE
            }
            if (it is DashboardViewModel.AppointmentComplete) {
                val appointmentNumber = it.appointmentNumber
                if (it.jobType.contains(HomeViewModel.intsall)) {
                    incCompleted.installation_complete_title.text =
                        resources.getString(R.string.installation_complete)
                    incCompleted.you_are_all_set_msg.text =
                        resources.getString(R.string.the_network_is_ready_for_you_to_connect_and_start_enjoying_your_blazing_fast_internet)
                    incCompleted.get_started_btn.text =
                        resources.getString(R.string.get_started)
                    binding.incCompleted.getStartedBtn.setOnClickListener {
                        incCompleted.visibility = View.GONE
                        dashboardViewModel.getStartedClicked(appointmentNumber)
                        viewClickListener.onGetStartedClick(false)
                    }
                    binding.connectedDevicesCard.root.visibility = View.GONE
                    binding.layoutNetworkList.visibility = View.GONE
                } else {
                    incCompleted.installation_complete_title.text =
                        resources.getString(R.string.service_appointment_status_complete)
                    incCompleted.you_are_all_set_msg.text =
                        resources.getString(R.string.service_appointment_all_set_status)
                    incCompleted.get_started_btn.text =
                        resources.getString(R.string.dismiss)
                    binding.incCompleted.getStartedBtn.setOnClickListener {
                        incCompleted.visibility = View.GONE
                        dashboardViewModel.getStartedClicked(appointmentNumber)
                        viewClickListener.onGetStartedClick(false)
                    }
                    displayDashboardUI()
                }
                incWorkBegun.visibility = View.GONE
                incScheduled.visibility = View.GONE
                incEnroute.visibility = View.GONE
                incCompleted.visibility = View.VISIBLE
                dashboardViewModel.clearNotificationStatus(ServiceStatus.COMPLETED.name)
                dashboardViewModel.logAppointmentStatusState(4)
            }
            if (it is DashboardViewModel.AppointmentCanceled) {
                dashboardViewModel.clearNotificationStatus(ServiceStatus.CANCELED.name)
                if (it.jobType.contains(HomeViewModel.intsall)) {
                    incCanceled.visibility = View.VISIBLE
                } else {
                    incCanceled.visibility = View.GONE
                    displayDashboardUI()
                }
                incScheduled.visibility = View.GONE
                incEnroute.visibility = View.GONE
                incWorkBegun.visibility = View.GONE
                incCompleted.visibility = View.GONE
                dashboardViewModel.clearNotificationStatus(ServiceStatus.COMPLETED.name)
                dashboardViewModel.logAppointmentStatusState(5)
            }
        }
    }

    // TODO right now this feature is not in active so commenting for now
    private fun observeNotificationViews() {
        dashboardViewModel.notificationListDetails.observe {
            dashboardViewModel.displaySortedNotifications(it.notificationlist)
        }
        dashboardViewModel.notifications.observe {
            addNotificationStack(it)
        }
    }

    /**
     * Observe wifi details views - It will observe the wifi details from viewmodel
     *
     */
    private fun observeWifiDetailsViews() {
        dashboardViewModel.wifiListDetails.observe {
            prepareRecyclerView(it.wifiListDetails)
        }

        dashboardViewModel.wifiListDetailsUpdated.observe {
            prepareRecyclerView(it.wifiListDetails)
        }
    }

    /**
     * Add notification stack - It will help to add notification into stack
     *
     * @param notificationList
     */
    private fun addNotificationStack(notificationList: MutableList<Notification>) {
        unreadNotificationList = notificationList
        if (unreadNotificationList.isNotEmpty()) {
            binding.topCard.visibility = View.VISIBLE
            binding.middleCard.visibility = View.VISIBLE
            binding.bottomCard.visibility = View.VISIBLE
            when (unreadNotificationList.size) {
                1 -> {
                    binding.middleCard.visibility = View.GONE
                    binding.bottomCard.visibility = View.GONE
                }
                2 -> {
                    binding.bottomCard.visibility = View.GONE
                }
            }
            binding.notificationTitle.text = unreadNotificationList[0].name
            binding.notificationMsg.text = unreadNotificationList[0].description
        } else {
            binding.topCard.visibility = View.GONE
        }
    }

    /**
     * Bit map from vector - It will help to get the bitmap
     *
     * @param vectorResID -vector resource id to get the bitmap
     * @return - BitmapDescriptor- bitmap instance
     */
    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorResID)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Set listener- click event for view
     *
     * @param clickListener -listener instance
     */
    fun setListener(clickListener: ViewClickListener) {
        this.viewClickListener = clickListener
    }

    /**
     * Init wifi scan views - initialises wifi scan views
     */
    private fun initWifiScanViews() {
        binding.wifiScanList.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
    }

    /**
     * Show cancellation confirmation dialog - It will display cancellation confirmation dialog
     */
    private fun showCancellationConfirmationDialog(msg: String) {
        CustomDialogGreyTheme(
            getString(R.string.installation_cancellation_confirmation_title),
            msg,
            getString(R.string.cancel_it),
            getString(R.string.keep_it),
            ::onDialogCallback
        ).show(fragManager!!, DashboardFragment::class.simpleName)
    }

    fun View.clickWithDebounce(debounceTime: Long = 600L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()

                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    /**
     * On dialog callback- callback listener for the dialog
     *
     * @param buttonType - It will define which button is clicked
     */
    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                dashboardViewModel.logCancelAppointmentAlertClick(false)
                dashboardViewModel.requestAppointmentCancellation()
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                dashboardViewModel.logCancelAppointmentAlertClick(true)
            }
        }
    }

    /**
     * Speed test dialog callback - callback listener for the speed test dialog
     *
     * @param buttonType - It will define which button is clicked
     */
    private fun speedTestDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                dashboardViewModel.startSpeedTest(true)
            }
        }
    }

    /**
     * View click listener - interface for click events
     *
     * @constructor Create empty View click listener
     */
    interface ViewClickListener {
        /**
         * Handle click event
         */
        fun onGetStartedClick(newUser: Boolean)

        fun onViewDevicesClick()
    }

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        private const val KEY_NEW_USER = "NEW_USER"
        const val REFRESH_APPOINTMENT = 2984
        operator fun invoke(newUser: Boolean) = DashboardFragment().apply {
            arguments = Bundle().apply { putBoolean(KEY_NEW_USER, newUser) }
        }
    }

    /**
     * Prepare recycler view - it will initialises the recyclerview
     *
     * @param wifiList - wifilist to display
     */
    private fun prepareRecyclerView(wifiList: MutableList<WifiInfo>) {
        wifiDevicesAdapter = WifiDevicesAdapter(wifiList, this)
        binding.wifiScanList.adapter = wifiDevicesAdapter
    }

    /**
     * Update view - It will call the update devices api from viewmodel
     */
    fun updateView() {
        dashboardViewModel.initDevicesApis()
    }

    /**
     * On wifi q r scan image clicked -  it will help to navigate to qrscan screen
     *
     * @param wifidetails - wifidetails instance
     */
    override fun onWifiQRScanImageClicked(wifidetails: WifiInfo) {
        dashboardViewModel.navigateToQRScan(wifidetails)
    }

    /**
     * On wifi name clicked -it will help to navigate to wifi details screen
     *
     * @param networkName - slected network name
     */
    override fun onWifiNameClicked(networkName: String) {
        dashboardViewModel.navigateToNetworkInformation()
    }

    /**
     * On wifi network status image clicked
     *
     * @param wifidetails - wifidetails instance
     */
    override fun onWifiNetworkStatusImageClicked(wifidetails: WifiInfo) {
        dashboardViewModel.wifiNetworkEnablement(wifidetails)
    }

    /**
     * On wifi name clicked -it will help to navigate to wifi details screen
     *
     * @param networkName - slected network name
     */
    override fun onNavigateToNetworkInfo() {
        dashboardViewModel.navigateToNetworkInformation()
    }

    /**
     * Listen for reboot dialog -it will help to observe reboot devices status
     */
    private fun listenForRebootDialog() {
        dashboardViewModel.rebootDialogFlow.observe { success ->
            dashboardViewModel.networkStatus.observe { networkStatusOnline ->
                if (networkStatusOnline && success) {
                    binding.incSpeedTest.runSpeedTestDashboard.isActivated = true
                    binding.incSpeedTest.runSpeedTestDashboard.isEnabled = true
                } else {
                    binding.incSpeedTest.runSpeedTestDashboard.isActivated = false
                    binding.incSpeedTest.runSpeedTestDashboard.isEnabled = false
                }
            }
        }
    }

    /**
     * On error dialog callback- error dialog callback event
     *
     * @param buttonType - It will define which button is clicked
     */
    private fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                /** no op **/
            }
        }
    }
}
