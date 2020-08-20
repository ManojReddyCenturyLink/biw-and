package com.centurylink.biwf.base

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.utility.LiveDataObserver
import com.centurylink.biwf.widgets.ModemRebootFailureDialog
import com.centurylink.biwf.widgets.ModemRebootSuccessDialog
import dagger.android.AndroidInjection

/**
 * Base class for holding common functionality that will be used across screens.
 */
abstract class BaseActivity : AppCompatActivity(), LiveDataObserver, ModemRebootFailureDialog.Callback {

    override val lifecycleOwner: LifecycleOwner get() = this
    private var progressView: View? = null
    private var retryView: View? = null
    private var retryOverlayView: View? = null
    private var layoutView: View? = null

    internal abstract val viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        listenForRebootDialog()
    }

    fun setActivityHeight() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window.attributes)

        val dialogWindowWidth = (displayWidth * 1f).toInt()
        // Set alert dialog height equal to screen height 90%
        val dialogWindowHeight = (displayHeight * 0.98f).toInt()
        layoutParams.width = dialogWindowWidth
        layoutParams.height = dialogWindowHeight
        window.attributes = layoutParams
    }

    fun setApiProgressViews(
        progressView: View,
        retryView: View,
        layout: View,
        retryOverlayView: View
    ) {
        this.progressView = progressView
        this.retryView = retryView
        this.retryOverlayView = retryOverlayView
        this.layoutView = layout
        this.retryView?.setOnClickListener {
            retryClicked()
        }
    }

    fun showProgress(showProgress: Boolean) {
        this.progressView?.visibility = if (showProgress) View.VISIBLE else View.GONE
        this.layoutView?.visibility = if (showProgress) View.GONE else View.VISIBLE
        this.retryOverlayView?.visibility = View.GONE
    }

    fun showRetry(showReload: Boolean) {
        this.progressView?.visibility = View.GONE
        this.retryOverlayView?.visibility = if (showReload) View.VISIBLE else View.GONE
        this.layoutView?.visibility = if (showReload) View.GONE else View.VISIBLE
    }

    private fun listenForRebootDialog() {
        viewModel.rebootDialogFlow.observe { success ->
            if (success) {
                viewModel.logModemRebootSuccessDialog()
                showModemRebootSuccessDialog()
            } else {
                viewModel.logModemRebootErrorDialog()
                showModemRebootErrorDialog()
            }
        }
    }

    fun showModemRebootSuccessDialog() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.onRebootDialogShown()
            ModemRebootSuccessDialog().show(
                supportFragmentManager,
                callingActivity?.className
            )
        }
    }

    fun showModemRebootErrorDialog() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.onRebootDialogShown()
            ModemRebootFailureDialog(this).show(
                supportFragmentManager,
                callingActivity?.className
            )
        }
    }

    override fun onRetryModemRebootClicked() {
        viewModel.rebootModem()
    }

    open fun retryClicked() {}
}
