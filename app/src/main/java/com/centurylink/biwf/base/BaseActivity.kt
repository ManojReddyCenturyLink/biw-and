package com.centurylink.biwf.base

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.utility.LiveDataObserver
import dagger.android.AndroidInjection

/**
 * Base class for holding common functionality that will be used across screens.
 */
abstract class BaseActivity : AppCompatActivity(), LiveDataObserver {

    override val lifecycleOwner: LifecycleOwner get() = this
    private var progressView: View? = null
    private var retryView: View? = null
    private var retryOverlayView: View? = null
    private var layoutView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
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

    fun displayToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
        if (this.progressView != null && layoutView != null) {
            this.progressView?.visibility = if (showProgress) View.VISIBLE else View.GONE
            this.layoutView?.visibility = if (showProgress) View.INVISIBLE else View.VISIBLE
        }
        if (this.retryOverlayView != null)
            this.retryOverlayView?.visibility = View.GONE
    }

    fun showRetry(showReload: Boolean) {
        if (this.retryOverlayView != null && this.progressView != null && layoutView != null) {
            this.progressView?.visibility = View.GONE
            this.retryOverlayView?.visibility = if (showReload) View.VISIBLE else View.GONE
            this.layoutView?.visibility = if (showReload) View.GONE else View.VISIBLE
        }
    }

    fun hideProgress() {
        if (this.progressView != null)
            this.progressView?.visibility = View.GONE
        if (this.retryOverlayView != null)
            this.retryOverlayView?.visibility = View.GONE
        this.layoutView?.visibility = View.VISIBLE
    }

    open fun retryClicked() {}
}
