package com.centurylink.biwf.screens.home.dashboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
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
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.screens.home.dashboard.adapter.WifiDevicesAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
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
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.appointment_date
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.appointment_date_time_card
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.appointment_time
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.incWelcomeCard
import kotlinx.android.synthetic.main.widget_status_enroute.view.incEnrouteCard
import kotlinx.android.synthetic.main.widget_status_enroute.view.technician_name
import kotlinx.android.synthetic.main.widget_status_work_begun.view.incWipCard
import kotlinx.android.synthetic.main.widget_welcome_card.view.msg
import kotlinx.android.synthetic.main.widget_welcome_card.view.msg_dismiss_button
import kotlinx.android.synthetic.main.widget_welcome_card.view.title
import javax.inject.Inject

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
    //private var destinationLatLng = LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.checkForOngoingSpeedTest()
    }

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
        dashboardViewModel.downloadSpeed.observe {
            binding.incSpeedTest.downloadSpeed.text = it
        }
        dashboardViewModel.uploadSpeed.observe {
            binding.incSpeedTest.uploadSpeed.text = it
        }
        dashboardViewModel.latestSpeedTest.observe {
            binding.incSpeedTest.lastSpeedTestTime.text = it
        }
        dashboardViewModel.progressVisibility.observe {
            binding.incSpeedTest.uploadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
            binding.incSpeedTest.downloadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
            binding.incSpeedTest.downloadProgressIcon.visibility =
                if (it) View.VISIBLE else View.INVISIBLE
            binding.incSpeedTest.uploadProgressIcon.visibility =
                if (it) View.VISIBLE else View.INVISIBLE
        }
        dashboardViewModel.speedTestButtonState.observe { binding.incSpeedTest.runSpeedTestDashboard.isActivated = it }
        initOnClicks()
        dashboardViewModel.myState.observeWith(dashboardCoordinator)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupMap()
        initWifiScanViews()
    }

    override fun retryClicked() {
        dashboardViewModel.initApis()
    }

    private fun initViews() {
        if (dashboardViewModel.isExistingUser.value) {
            incSpeedTest.visibility = View.VISIBLE
            binding.connectedDevicesCard.root.visibility = View.VISIBLE
            dashboardViewModel.startSpeedTest()
            // TODO right now this feature is not in active so commenting for now
           // observeNotificationViews()
            observeWifiDetailsViews()
        } else {
            getAppointmentStatus()
        }
    }

    private fun initOnClicks() {
        binding.incSpeedTest.runSpeedTestDashboard.setOnClickListener { dashboardViewModel.startSpeedTest() }
        binding.incScheduled.appointmentChangeBtn.setOnClickListener { dashboardViewModel.getChangeAppointment() }
        binding.incScheduled.appointmentCancelBtn.setOnClickListener { showCancellationConfirmationDialaog() }
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
        binding.incCompleted.getStartedBtn.setOnClickListener {
            dashboardViewModel.getStartedClicked()
            viewClickListener.onGetStartedClick(false)
        }
        binding.connectedDevicesCard.root.setOnClickListener { viewClickListener.onViewDevicesClick() }
    }

    private fun setupMap() {
        val fm = childFragmentManager
        enrouteMapFragment = fm.findFragmentById(R.id.map_enroute_status) as SupportMapFragment
        workBegunMapFragment = fm.findFragmentById(R.id.map_work_begun) as SupportMapFragment
        enrouteMapFragment?.getMapAsync(enrouteOnMapReadyCallback)
        workBegunMapFragment?.getMapAsync(mOnMapReadyCallback)
    }

    private var enrouteOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 16.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.blue_marker))
                )
                /*We’re not going to be getting technician values until after MVP, so commenting for now
                addMarker(
                    MarkerOptions().position(destinationLatLng)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )*/
                animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 10f))
            }
        }

    private var mOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 16.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.blue_marker))
                )
            }
        }

    private fun getAppointmentStatus() {
        dashboardViewModel.dashBoardDetailsInfo.observe {
            if (it is DashboardViewModel.AppointmentScheduleState) {
                incScheduled.visibility = View.VISIBLE
                incScheduled.appointment_date_time_card.appointment_date.text =
                    it.serviceAppointmentDate
                incScheduled.appointment_date_time_card.appointment_time.text = getString(
                    R.string.text_time_details,
                    it.serviceAppointmentStartTime,
                    it.serviceAppointmentEndTime
                )
                incScheduled.incWelcomeCard.msg_dismiss_button.setOnClickListener {
                    incScheduled.incWelcomeCard.visibility = View.GONE
                }
            }
            if (it is DashboardViewModel.AppointmentEngineerStatus) {
                incEnroute.visibility = View.VISIBLE
                incScheduled.visibility = View.GONE
                incEnroute.technician_name.text = it.serviceEngineerName
                incEnroute.appointment_time.text = it.serviceAppointmentTime
                incEnroute.incEnrouteCard.title.text =
                    resources.getString(R.string.technician_on_the_way)
                incEnroute.incEnrouteCard.msg.text =
                    resources.getString(R.string.enroute_notification_message)
                incEnroute.incEnrouteCard.msg_dismiss_button.setOnClickListener {
                    incEnroute.incEnrouteCard.visibility = View.GONE
                }
                originLatLng = LatLng(it.serviceLatitude.toDouble(), it.serviceLongitude.toDouble())
            }
            if (it is DashboardViewModel.AppointmentEngineerWIP) {
                incWorkBegun.visibility = View.VISIBLE
                incEnroute.visibility = View.GONE
                incWorkBegun.technician_name.text = it.serviceEngineerName
                incWorkBegun.incWipCard.title.text = resources.getString(R.string.work_in_progress)
                incWorkBegun.incWipCard.msg.text = resources.getString(R.string.work_begun_message)
                incWorkBegun.incWipCard.msg_dismiss_button.setOnClickListener {
                    incWorkBegun.incWipCard.visibility = View.GONE
                }
                originLatLng = LatLng(it.serviceLatitude.toDouble(), it.serviceLongitude.toDouble())
            }
            if (it is DashboardViewModel.AppointmentComplete) {
                incCompleted.visibility = View.VISIBLE
                incWorkBegun.visibility = View.GONE
            }
            if (it is DashboardViewModel.AppointmentCanceled) {
                incScheduled.visibility = View.GONE
                incCanceled.visibility = View.VISIBLE
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

    private fun observeWifiDetailsViews() {
        dashboardViewModel.wifiListDetails.observe {
            prepareRecyclerView(it.wifiListDetails)
        }

        dashboardViewModel.wifiListDetailsUpdated.observe {
            prepareRecyclerView(it.wifiListDetails)
        }
    }

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

    fun setListener(clickListener: ViewClickListener) {
        this.viewClickListener = clickListener
    }

    private fun initWifiScanViews() {
        binding.wifiScanList.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
    }

    private fun showCancellationConfirmationDialaog() {
        CustomDialogGreyTheme(
            getString(R.string.installation_cancellation_confirmation_title),
            getString(R.string.installation_cancellation_confirmation_msg),
            getString(R.string.cancel_it),
            getString(R.string.keep_it),
            ::onDialogCallback
        ).show(fragManager!!, DashboardFragment::class.simpleName)
    }

    // Callbacks for the Dialog
    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                dashboardViewModel.requestAppointmentCancellation()
            }
            AlertDialog.BUTTON_NEGATIVE -> {
            }
        }
    }

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

    private fun prepareRecyclerView(wifiList: MutableList<WifiInfo>) {
        wifiDevicesAdapter = WifiDevicesAdapter(wifiList, this)
        binding.wifiScanList.adapter = wifiDevicesAdapter
    }

    override fun onWifiQRScanImageClicked(wifidetails: WifiInfo) {
        dashboardViewModel.navigateToQRScan(wifidetails)
    }

    override fun onWifiNameClicked(networkName: String) {
        dashboardViewModel.navigateToNetworkInformation(networkName)
    }

    override fun onWifiNetworkStatusImageClicked(wifidetails: WifiInfo) {
        dashboardViewModel.wifiNetworkEnablement(wifidetails)
    }
}
