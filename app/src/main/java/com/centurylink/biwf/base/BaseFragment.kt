package com.centurylink.biwf.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.centurylink.biwf.utility.LiveDataObserver
import dagger.android.support.AndroidSupportInjection

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

    fun displayToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

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

    open fun retryClicked() {}
}