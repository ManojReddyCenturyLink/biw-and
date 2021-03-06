package com.centurylink.biwf.base

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.utility.AppUtil
import com.centurylink.biwf.utility.Events
import com.centurylink.biwf.utility.GlobalBus
import com.centurylink.biwf.utility.LiveDataObserver
import com.centurylink.biwf.widgets.ModemRebootFailureDialog
import com.centurylink.biwf.widgets.ModemRebootSuccessDialog
import dagger.android.AndroidInjection
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Base class for holding common functionality that will be used across Activities. All the Activities
 */
abstract class BaseActivity : AppCompatActivity(), LiveDataObserver,
    ModemRebootFailureDialog.Callback {

    override val lifecycleOwner: LifecycleOwner get() = this
    private var progressView: View? = null
    private var retryView: View? = null
    private var retryOverlayView: View? = null
    private var layoutView: View? = null

    internal abstract val viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }

    /**
     * Set activity height incase it needs to be displayed as a dialog / of different height
     *
     */
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

    /**
     * Function to set the Progress/Retry/Root view when the APi is loading.
     *
     * @param progressView - The Progress bar view
     * @param retryView - The Retry layout view
     * @param layout - The root view
     * @param retryOverlayView The RetryOverlayview.
     */
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

    /**
     * Function to show Progress and hide other views.
     *
     * @param showProgress
     */
    fun showProgress(showProgress: Boolean) {
        this.progressView?.visibility = if (showProgress) View.VISIBLE else View.GONE
        this.layoutView?.visibility = if (showProgress) View.GONE else View.VISIBLE
        this.retryOverlayView?.visibility = View.GONE
    }

    /**
     * Function to show Retry view when the API calls fail and hide other views.
     *
     * @param showReload
     */
    fun showRetry(showReload: Boolean) {
        this.progressView?.visibility = View.GONE
        this.retryOverlayView?.visibility = if (showReload) View.VISIBLE else View.GONE
        this.layoutView?.visibility = if (showReload) View.GONE else View.VISIBLE
    }

    /**
     * functions shows  the modem reboot success dialog
     *
     */
    fun showModemRebootSuccessDialog() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!AppUtil.rebootStatus) {
                AppUtil.rebootStatus = true
                AppUtil.rebootOnGoingStatus = false
                viewModel.onRebootDialogShown()
                ModemRebootSuccessDialog().show(
                    supportFragmentManager,
                    callingActivity?.className
                )
            }
        }
    }

    /**
     * Function shows the  modem reboot error dialog.
     *
     */
    fun showModemRebootErrorDialog() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!AppUtil.rebootStatus) {
                AppUtil.rebootStatus = true
                AppUtil.rebootOnGoingStatus = false
                viewModel.onRebootDialogShown()
                ModemRebootFailureDialog(this).show(
                    supportFragmentManager,
                    callingActivity?.className
                )
            }
        }
    }

    override fun onRetryModemRebootClicked() {
        AppUtil.rebootOnGoingStatus = false
        viewModel.rebootModem()
    }

    override fun onRetryModemRebootCanceled() {
        AppUtil.rebootOnGoingStatus = false
        viewModel.rebootCanceled()
    }

    open fun retryClicked() {
        Timber.e("retry clicked")
    }

    override fun onStart() {
        super.onStart()
        // Register this fragment to listen to event.
        GlobalBus.bus!!.register(this)
    }

    /**
     * Dialog shown when the dialog gets rebooted in the UI.
     *
     */
    @Subscribe
    open fun getMessage(modemRebootMessage: Events.ModemRebootMessage) {
        if (modemRebootMessage.status) {
            viewModel.logModemRebootSuccessDialog()
            showModemRebootSuccessDialog()
        } else {
            viewModel.logModemRebootErrorDialog()
            viewModel.clearRebootProgress()
            showModemRebootErrorDialog()
        }
    }

    override fun onStop() {
        super.onStop()
        GlobalBus.bus!!.unregister(this)
    }
}
