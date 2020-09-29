package com.centurylink.biwf.widgets

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager

class GeneralErrorPopUp {

    companion object {

        fun showGeneralErrorDialog(
            fragmentManager: FragmentManager,
            className: String?
        ) {
            CustomDialogBlueTheme(
                title = "An error has occured",
                message = "We’re very sorry for the inconvenience, but we were unable to save your changes. Please try again later.",
                buttonText = "Discard changes and close",
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