package com.centurylink.biwf.widgets

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager

class NoNetworkErrorPopup {

    companion object {

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

        private fun onDialogCallback(buttonType: Int) {
            when (buttonType) {
                AlertDialog.BUTTON_POSITIVE -> {
                }
            }
        }
    }
}