package com.centurylink.biwf.screens.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.widget_notification_stack.view.*
import javax.inject.Inject

class DashboardFragment : BaseFragment() {

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
    }
    override val liveDataLifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var dashboardCoordinator: DashboardCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    private val dashboardViewModel by lazy {
        ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }
    private var unreadNotificationList: MutableList<Notification> = mutableListOf()

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        /*Added dummy state variable to test layout for different scenarios */
        binding.states = 2
        initOnClicks()
        getNotificationInformation()
        binding.executePendingBindings()
        dashboardCoordinator.observeThis(dashboardViewModel.myState)
        return binding.root
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
        if(unreadNotificationList.isNotEmpty()){
            when(unreadNotificationList.size){
                1->{
                    binding.incNotificationStack.middleCardview.visibility = View.GONE
                    binding.incNotificationStack.bottomCardview.visibility = View.GONE
                }
                2->{
                    binding.incNotificationStack.bottomCardview.visibility = View.GONE
                }
            }
            binding.incNotificationStack.topCardview.notification_title.text = unreadNotificationList.get(0).name
            binding.incNotificationStack.topCardview.notification_msg.text = unreadNotificationList.get(0).description
        }else{
            binding.incNotificationStack.root.visibility = View.GONE
        }
    }

    private fun initOnClicks() {
        binding.incStatus.appointmentChangeLink.setOnClickListener { dashboardViewModel.getChangeAppointment() }
        binding.incWelcomeCard.welcomeCardCancelButton.setOnClickListener { hideWelcomeCard() }
        binding.incWelcomeCard.welcomeCardStatusTitle.setOnClickListener { updateUI()
        binding.incNotificationStack.topCardview.notification_dismiss_button.setOnClickListener {
            dashboardViewModel.markNotificationAsRead(unreadNotificationList.get(0))
            displaySortedNotification()
        }
        binding.incNotificationStack.topCardview.setOnClickListener {
            dashboardViewModel.navigateToNotificationDetails(unreadNotificationList.get(0)) }
        }
    }

    private fun hideWelcomeCard() {
        binding.incWelcomeCard.root.visibility = View.GONE
    }

    private fun updateUI(){
        binding.states = 1
    }
}