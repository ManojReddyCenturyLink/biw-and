package com.centurylink.biwf.screens.home.dashboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DashboardCoordinator
import com.centurylink.biwf.databinding.FragmentDashboardBinding
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
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

    private val originLatLng = LatLng(12.9121, 77.6446)
    private val destinationLatLng = LatLng(12.9304, 77.6784)
    lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
        //Service call and check user type
        //newUser = arguments!!.getBoolean(KEY_NEW_USER, newUser)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        initOnClicks()
        getAppointmentStatus()
        observeNotificationViews()
        binding.executePendingBindings()
        dashboardViewModel.myState.observeWith(dashboardCoordinator)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
    }

    private fun setupMap() {
        val fm = childFragmentManager

        mEnrouteMapFragment = fm.findFragmentById(R.id.map_enroute_status) as SupportMapFragment
        mWorkbegunMapFragment = fm.findFragmentById(R.id.map_work_begun) as SupportMapFragment

//        val url: String = getDirectionsUrl(originLatLng, destinationLatLng)
//
//        // Start downloading json data from Google Directions API
//        DownloadTask().execute(url)

        mEnrouteMapFragment?.getMapAsync(enrouteOnMapReadyCallback)
        mWorkbegunMapFragment?.getMapAsync(mOnMapReadyCallback)
    }

    private var enrouteOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            mMap = googleMap
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 14.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.blue_marker))
                )
                addMarker(
                    MarkerOptions().position(destinationLatLng)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )
                animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 10f))
            }
        }

    private var mOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 18.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )
            }
        }

    private fun getAppointmentStatus() {
        dashboardViewModel.dashBoardDetailsInfo.observe {
            Log.d("2ee", "" + it)
            when (it.toString()) {
                "Scheduled", "Dispatched", "None" -> {
                    binding.incStarted.root.visibility = View.VISIBLE
                }
                "Enroute" -> {
                    binding.incEnroute.root.visibility = View.VISIBLE
                }
                "Work Begun" -> {
                    binding.incWorkBegun.root.visibility = View.VISIBLE
                }
                "Completed" -> {
                    binding.incCompleted.root.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeNotificationViews() {
        dashboardViewModel.notificationListDetails.observe {
            dashboardViewModel.displaySortedNotifications(it.notificationlist)
            displaySortedNotification()
        }
    }

    private fun displaySortedNotification() {
        dashboardViewModel.notifications.observe {
            addNotificationStack(it)
        }
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
            binding.notificationTitle.text = unreadNotificationList[0].name
            binding.notificationMsg.text = unreadNotificationList[0].description
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
            dashboardViewModel.markNotificationAsRead(unreadNotificationList[0])
            displaySortedNotification()
        }
        binding.topCard.setOnClickListener {
            dashboardViewModel.navigateToNotificationDetails(unreadNotificationList[0])
        }
        binding.incCompleted.getStartedBtn.setOnClickListener {
            dashboardViewModel.onGetStartedClick()
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


//    private class DownloadTask :
//        AsyncTask<String?, Void?, String>() {
//
//        override fun onPostExecute(result: String) {
//            super.onPostExecute(result)
//            val parserTask = ParserTask()
//            parserTask.execute(result)
//        }
//
//        override fun doInBackground(vararg params: String?): String {
//            var data = ""
//            try {
//                data = downloadUrl(params[0]!!)
//            } catch (e: Exception) {
//                Log.d("Background Task", e.toString())
//            }
//            return data
//        }
//
//        private fun downloadUrl(strUrl: String): String {
//            var data = ""
//            var iStream: InputStream? = null
//            var urlConnection: HttpURLConnection? = null
//            try {
//                val url = URL(strUrl)
//                urlConnection = url.openConnection() as HttpURLConnection
//                urlConnection.connect()
//                iStream = urlConnection.inputStream
//                val br =
//                    BufferedReader(InputStreamReader(iStream))
//                val sb = StringBuffer()
//                var line: String? = ""
//                while (br.readLine().also { line = it } != null) {
//                    sb.append(line)
//                }
//                data = sb.toString()
//                br.close()
//            } catch (e: java.lang.Exception) {
//                Log.d("Exception", e.toString())
//            } finally {
//                iStream!!.close()
//                urlConnection!!.disconnect()
//            }
//            return data
//        }
//    }
//
//    private fun getDirectionsUrl(
//        origin: LatLng,
//        dest: LatLng
//    ): String? { // Origin of route
//        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
//        // Destination of route
//        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
//        //setting transportation mode
//        val mode = "mode=driving"
//        val sensor = "sensor=false"
//        // Building the parameters to the web service
//        val parameters = "$str_origin&$str_dest&$sensor&$mode"
//        // Output format
//        val output = "json"
//        // Building the url to the web service
//        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyAoMWNi1LtAdDTvRT26zCwsy7qslHLqQTE"
//    }
//
//
//    /**
//     * A class to parse the JSON format
//     */
//    private class ParserTask :
//        AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
//
//        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
//            val points: ArrayList<*> = ArrayList<String>()
//            val lineOptions = PolylineOptions()
//            for (i in result!!.indices) {
//                val path =
//                    result[i]
//                for (j in path.indices) {
//                    val point = path[j]
//                    val lat = point["lat"]!!.toDouble()
//                    val lng = point["lng"]!!.toDouble()
//                    val position = LatLng(lat, lng)
//                    points.add(position)
//                }
//                lineOptions.addAll(points)
//                lineOptions.width(12f)
//                lineOptions.color(R.color.online_green)
//                lineOptions.geodesic(true)
//            }
//            // Drawing polyline in the Google Map
//            if (points.size != 0) mMap.addPolyline(lineOptions)
//        }
//
//        override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>>? {
//            val jObject: JSONObject
//            var routes: List<List<HashMap<String, String>>>? =
//                null
//            try {
//                jObject = JSONObject(params[0])
//                val parser = DirectionsJSONParser()
//                routes = parser.parse(jObject)
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//            return routes
//        }
//    }

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        private const val KEY_NEW_USER = "NEW_USER"

        operator fun invoke(newUser: Boolean) = DashboardFragment().apply {
            arguments = Bundle().apply { putBoolean(KEY_NEW_USER, newUser) }
        }
    }
}