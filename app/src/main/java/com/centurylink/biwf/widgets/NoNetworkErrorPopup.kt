package com.centurylink.biwf.widgets

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager

/**
 * It will handle callback events in case of no network device
 *
 * @constructor Create empty No network error popup
 */
class NoNetworkErrorPopup {

    companion object {

        /**
         * Show no internet dialog
         *
         * @param fragmentManager - fragmentManager instance
         * @param className - class name as string
         */
        fun showNoInternetDialog(
            fragmentManager: FragmentManager,
            className: String?
        ) {
            CustomDialogBlueTheme(
                title = "No network",
                message = "Please check your internet connection.",
                buttonText = "OK",
                isErrorPopup = true,
                callback = ::onDialogCallback
            ).show(
                fragmentManager,
                className
            )
        }

        /**
         * On dialog callback lister dialog
         *
         * @param buttonType - dialog button value
         */
        private fun onDialogCallback(buttonType: Int) {
            when (buttonType) {
                AlertDialog.BUTTON_POSITIVE -> {
                }
            }
        }
    }
}