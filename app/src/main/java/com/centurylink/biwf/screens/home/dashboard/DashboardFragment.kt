package com.centurylink.biwf.screens.home.dashboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DashboardCoordinator
import com.centurylink.biwf.databinding.FragmentDashboardBinding
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject


class DashboardFragment : BaseFragment() {

    override val lifecycleOwner: LifecycleOwner = this

    private var newUser: Boolean = false

    @Inject
    lateinit var dashboardCoordinator: DashboardCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory
    private val dashboardViewModel by lazy {
        ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }
    private var unreadNotificationList: MutableList<Notification> = mutableListOf()

    private lateinit var binding: FragmentDashboardBinding

    private var mEnrouteMapFragment: SupportMapFragment? = null

    private var mWorkbegunMapFragment: SupportMapFragment? = null

    private val USER_LAT_LNG = LatLng(39.742043, -104.991531)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
        //Service call and check user type
        newUser = arguments!!.getBoolean(KEY_NEW_USER, newUser)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        /*Added dummy state variable to test layout for different scenarios */
        binding.states = newUser
        initOnClicks()
        getAppointmentStatus()
        getNotificationInformation()
        binding.executePendingBindings()
        dashboardViewModel.myState.observeWith(dashboardCoordinator)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager

        mEnrouteMapFragment = fm.findFragmentById(R.id.map_enroute_status) as SupportMapFragment
        mWorkbegunMapFragment = fm.findFragmentById(R.id.map_work_begun) as SupportMapFragment

        mEnrouteMapFragment?.getMapAsync(enrouteOnMapReadyCallback)
        mWorkbegunMapFragment?.getMapAsync(mOnMapReadyCallback)
    }

    private var enrouteOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                //googleMap.isIndoorEnabled = true
                moveCamera(CameraUpdateFactory.newLatLngZoom(USER_LAT_LNG, 8.0f))
                addMarker(
                    MarkerOptions().position(USER_LAT_LNG)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )
            }
        }

    private var mOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                //googleMap.isIndoorEnabled = true
                moveCamera(CameraUpdateFactory.newLatLngZoom(USER_LAT_LNG, 6.0f))
                addMarker(
                    MarkerOptions().position(USER_LAT_LNG)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )
            }
        }

    private fun getAppointmentStatus() {
        dashboardViewModel.appointmentStatusFlow.observe {
            when {
                it.equals("Scheduled") || it.equals("Dispatched") || it.equals("None") -> {
                    binding.incStarted.root.visibility = View.VISIBLE
                }
                it.equals("Enroute") -> {
                    binding.incEnroute.root.visibility = View.VISIBLE
                    binding.incEnroute.incProgress.progressStateTwo.background =
                        resources.getDrawable(R.drawable.dark_blue_background_rounded_corner)
                }
                it.equals("Work Begun") -> {
                    binding.incWorkBegun.root.visibility = View.VISIBLE
                    binding.incWorkBegun.incProgress.progressStateTwo.background =
                        resources.getDrawable(R.drawable.dark_blue_background_rounded_corner)
                    binding.incWorkBegun.incProgress.progressStateThree.background =
                        resources.getDrawable(R.drawable.dark_blue_background_rounded_corner)
                }
                it.equals("Completed") -> {
                    binding.incCompleted.root.visibility = View.VISIBLE
                    binding.incCompleted.incProgress.progressStateTwo.background =
                        resources.getDrawable(R.drawable.dark_blue_background_rounded_corner)
                    binding.incCompleted.incProgress.progressStateThree.background =
                        resources.getDrawable(R.drawable.dark_blue_background_rounded_corner)
                    binding.incCompleted.incProgress.progressStateFour.background =
                        resources.getDrawable(R.drawable.dark_blue_background_rounded_corner)
                }
            }
        }
    }

    private fun getNotificationInformation() {
        dashboardViewModel.getNotificationDetails().observe(this) {
            when {
                it.status.isLoading() -> {
                }
                it.status.isSuccessful() -> {
                    dashboardViewModel.displaySortedNotifications(it.data!!.notificationlist)
                    displaySortedNotification()
                }
                it.status.isError() -> {
                }
            }
        }
    }

    private fun displaySortedNotification() {
        dashboardViewModel.getNotificationMutableLiveData().observe(viewLifecycleOwner, Observer {
            addNotificationStack(it)
        })
    }

    private fun addNotificationStack(notificationList: MutableList<Notification>) {
        unreadNotificationList = notificationList
        if (unreadNotificationList.isNotEmpty()) {
            when (unreadNotificationList.size) {
                1 -> {
                    binding.middleCard.visibility = View.GONE
                    binding.bottomCard.visibility = View.GONE
                }
                2 -> {
                    binding.bottomCard.visibility = View.GONE
                }
            }
            binding.notificationTitle.text = unreadNotificationList.get(0).name
            binding.notificationMsg.text = unreadNotificationList.get(0).description
        } else {
            binding.topCard.visibility = View.GONE
            binding.middleCard.visibility = View.GONE
            binding.bottomCard.visibility = View.GONE
        }
    }

    private fun initOnClicks() {
        binding.incStarted.appointmentChangeBtn.setOnClickListener { dashboardViewModel.getChangeAppointment() }
        binding.incWelcomeCard.notificationDismissButton.setOnClickListener { hideWelcomeCard() }
        binding.notificationDismissButton.setOnClickListener {
            dashboardViewModel.markNotificationAsRead(unreadNotificationList.get(0))
            displaySortedNotification()
        }
        binding.topCard.setOnClickListener {
            dashboardViewModel.navigateToNotificationDetails(unreadNotificationList.get(0))
        }
    }

    private fun hideWelcomeCard() {
        binding.incWelcomeCard.root.visibility = View.GONE
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

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        private const val KEY_NEW_USER = "NEW_USER"

        operator fun invoke(newUser: Boolean) = DashboardFragment().apply {
            arguments = Bundle().apply { putBoolean(KEY_NEW_USER, newUser) }
        }
    }
}