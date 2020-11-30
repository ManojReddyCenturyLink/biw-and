package com.centurylink.biwf.widgets

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import timber.log.Timber

/**
 * It will handle callback events in case of any general error ecccours
 *
 * @constructor Create empty General error pop up
 */
class GeneralErrorPopUp {

    companion object {

        /**
         * It will show the general error dialog
         *
         * @param fragmentManager - fragment manager instance
         * @param className - class name as string
         */
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

        /**
         * On dialog callback lister dialog
         *
         * @param buttonType - dialog button value
         */
        private fun onDialogCallback(buttonType: Int) {
            when (buttonType) {
                AlertDialog.BUTTON_POSITIVE -> {
                    Timber.e("positive button pressed")
                }
            }
        }
    }
}
