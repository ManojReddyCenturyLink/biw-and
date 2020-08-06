package com.centurylink.biwf.screens.qrcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityScanCodeBinding
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.getViewModel
import javax.inject.Inject

class QrScanActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: QRScanViewModel.Factory

    private lateinit var binding: ActivityScanCodeBinding

    override val viewModel by lazy {
        getViewModel<QRScanViewModel>(
            viewModelFactory.withInput(
                intent.getSerializableExtra(WIFI_DETAILS) as WifiInfo
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustScreenBrightness()
        initHeaders()
        observeViews()
    }

    private fun adjustScreenBrightness() {
        // Updating the screen brightness to full
        val layout = window.attributes
        layout.screenBrightness = 1f
        window.attributes = layout
    }

    private fun initHeaders() {
        binding.incHeader.apply {
            subHeaderLeftIcon.visibility = View.INVISIBLE
            subheaderCenterTitle.text = getText(R.string.join_code)
            subheaderRightActionTitle.text = getText(R.string.header_done)
            subheaderRightActionTitle.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViews() {
        viewModel.qrScanFlow.observe {
            binding.barcodeView.setImageBitmap(it.wifiQrCode)
            binding.networkNameTv.text = it.name
        }
    }

    companion object {
        const val WIFI_DETAILS = "WIFI_INFO"
        const val ON_COLOR_QR: Long = 0xFF001E60
        const val OFF_COLOR_QR: Long = 0xFFFFFFFF
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, QrScanActivity::class.java)
                .putExtra(WIFI_DETAILS, bundle.getSerializable(WIFI_DETAILS))
        }
    }
}
