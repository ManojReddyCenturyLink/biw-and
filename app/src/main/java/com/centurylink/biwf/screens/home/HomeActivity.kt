package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }
    @Inject
    lateinit var homeCoordinator: HomeCoordinator
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)

        viewModel = HomeViewModel()
        viewModel.apply {
        }

        homeCoordinator.navigator.activity = this
        homeCoordinator.observeThis(viewModel.myState)

        initOnClicks()
    }

    private fun initOnClicks() {
       binding.supportButton.setOnClickListener { viewModel.onSupportClicked() }
    }
}
