package com.centurylink.biwf.screens.networkstatus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityNetworkStatusBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class NetworkStatusActivity : BaseActivity() {
    private lateinit var bindings: ActivityNetworkStatusBinding

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(NetworkStatusViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityNetworkStatusBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        viewModel.apply {
            modemInfoFlow.observe {
                bindings.networkStatusModemSerialNumber.text = getString(R.string.serial_number, it.deviceId)
            }
            internetStatusFlow.observe {
                bindings.networkStatusInternetImageview.setImageDrawable(getDrawable(it.drawableId))
                bindings.networkStatusInternetStatus.text = getString(it.onlineStatus)
                bindings.networkStatusInternetStatusText.text = getString(it.subText)
                bindings.networkStatusModemImageview.setImageDrawable(getDrawable(it.drawableId))
                bindings.networkStatusModemStatus.text = getString(it.onlineStatus)
                bindings.networkStatusWifiButton.isActivated = it.isActive
                bindings.networkStatusWifiButtonText.isActivated = it.isActive
                bindings.networkStatusWifiImage.isActivated = it.isActive
                bindings.networkStatusWifiButtonText.text = getString(it.wifiNetworkButtonText)
                bindings.networkStatusWifiButtonActionText.text = getString(it.wifiButtonSubText)
                bindings.networkStatusGuestButton.isActivated = it.isActive
                bindings.networkStatusGuestButtonText.isActivated = it.isActive
                bindings.networkStatusGuestWifiImage.isActivated = it.isActive
                bindings.networkStatusGuestButtonText.text = getString(it.guestNetworkButtonText)
                bindings.networkStatusGuestButtonActionText.text = getString(it.guestNetworkButtonSubText)
            }

        }

        initClicks()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun initClicks() {
        // will remove once rest of the network calls are implemented
        bindings.networkStatusDoneButton.setOnClickListener { finish() }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, NetworkStatusActivity::class.java)
    }

}