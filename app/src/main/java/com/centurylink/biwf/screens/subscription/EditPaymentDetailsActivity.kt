package com.centurylink.biwf.screens.subscription

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityEditPaymentDetailsBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class EditPaymentDetailsActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(EditPaymentDetailsViewModel::class.java)
    }

    private val webViewClient by lazy {
        object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                viewModel.onWebViewProgress(
                    binding.webView.progress,
                    this@EditPaymentDetailsActivity
                )
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                viewModel.onWebViewError()
            }
        }
    }
    private lateinit var binding: ActivityEditPaymentDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHeaders()
        setupProgressViews()
        setupWebView()
        listenForProgressUpdates()
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.edit_billing_information)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackPress()
                setResult(REQUEST_TO_REFRESH_PAYMENT_TO_SUBSCRIPTION)
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logDonePress()
                setResult(REQUEST_TO_REFRESH_PAYMENT_MOVE_TO_ACCOUNTS)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        setResult(REQUEST_TO_REFRESH_PAYMENT_TO_SUBSCRIPTION)
        finish()
    }

    private fun setupProgressViews() {
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.webView,
            binding.retryOverlay.root
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.webViewClient = webViewClient
        binding.webView.settings.apply {
            javaScriptEnabled = true
        }
        viewModel.subscriptionUrlFlow.observe {
            binding.webView.loadUrl(it)
        }
    }

    private fun listenForProgressUpdates() {
        viewModel.progressViewFlow.observe {
            showProgress(it)
        }
        viewModel.errorMessageFlow.observe {
            showRetry(it.isNotEmpty())
        }
    }

    override fun retryClicked() {
        showProgress(true)
        viewModel.onRetryClicked()
    }

    companion object {
        const val REQUEST_TO_REFRESH_PAYMENT_MOVE_TO_ACCOUNTS= 1039
        const val REQUEST_TO_REFRESH_PAYMENT_TO_SUBSCRIPTION= 12339
        const val REQUEST_TO_EDIT_PAYMENT_DETAILS = 1103
        fun newIntent(context: Context) = Intent(context, EditPaymentDetailsActivity::class.java)
    }
}
