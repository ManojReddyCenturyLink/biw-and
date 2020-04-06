package com.centurylink.biwf.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DashboardCoordinator
import com.centurylink.biwf.databinding.FragmentDashboardBinding
import com.centurylink.biwf.di.viewModelFactory.DaggerViewModelFactory
import javax.inject.Inject

class DashboardFragment : BaseFragment(){

    @Inject
    lateinit var dashboardCoordinator: DashboardCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }
    private lateinit var binding : FragmentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        dashboardCoordinator.navigator.activity = activity
        dashboardCoordinator.observeThis(viewModel.myState)
        initOnClicks()
        return binding.root
    }

    private fun initOnClicks() {
        binding.incStatus.changeAppointment.setOnClickListener{ viewModel.getChangeAppointment()}
        binding.incWelcomeCard.btnCancel.setOnClickListener { hideWelcomeCard() }
    }

    private fun hideWelcomeCard(){
        binding.incWelcomeCard.root.visibility = View.GONE
    }
}