package com.centurylink.biwf.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.centurylink.biwf.utility.LiveDataObserver
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber

/**
 * Base class for Fragments all the Fragments class must derive this Base Class.
 */
abstract class BaseFragment : Fragment(), LiveDataObserver {

    private var progressView: View? = null
    private var retryView: View? = null
    private var retryOverlayView: View? = null
    private var layoutView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    /**
     * Function to display the Progress Bar view when the APi is loading.
     *
     * @param progressView - The Progress bar view
     * @param retryView - The Retry layout view
     * @param layout - The root view
     * @param retryOverlayView The RetryOverlayview.
     */
    fun setApiProgressViews(
        layout: View,
        progressView: View,
        retryView: View,
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

    open fun retryClicked() {
        Timber.e("retry clicked")
    }
}
