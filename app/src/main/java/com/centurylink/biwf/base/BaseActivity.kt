package com.centurylink.biwf.base

import android.os.Bundle
import android.util.DisplayMetrics
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

    override val liveDataLifecycleOwner: LifecycleOwner get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }

     fun setHeightofActivity(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(getWindow().getAttributes())

        val dialogWindowWidth = (displayWidth * 1f).toInt()
        // Set alert dialog height equal to screen height 90%
        val dialogWindowHeight = (displayHeight * 0.98f).toInt()
        layoutParams.width = dialogWindowWidth
        layoutParams.height = dialogWindowHeight
        getWindow().setAttributes(layoutParams)
    }
     fun displayToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}