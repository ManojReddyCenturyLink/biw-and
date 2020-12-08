package com.centurylink.biwf.widgets

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.centurylink.biwf.R

/**
 * It will handle callback events in case of modem reboot success
 *
 * @property rebootCallback - callback instance if modem reboot
 * @constructor Create empty Modem reboot failure dialog
 */
class ModemRebootFailureDialog(
    private val rebootCallback: Callback
) : CustomDialogGreyTheme({ buttonType ->
    if (buttonType == AlertDialog.BUTTON_POSITIVE) {
        rebootCallback.onRetryModemRebootClicked()
    } else if (buttonType == AlertDialog.BUTTON_NEGATIVE) {
        rebootCallback.onRetryModemRebootCanceled()
    }
}) {

    /**
     * On create - called to do initial creation of the fragment.
     *
     * @param savedInstanceState - Bundle: If the fragment is being re-created from a previous
     *                             saved state, this is the state. This value may be null
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.modem_reboot_error_title)
        message = getString(R.string.modem_reboot_error_message)
        positiveText = getString(R.string.modem_reboot_error_button_positive)
        negativeText = getString(R.string.modem_reboot_error_button_negative)
    }

    /**
     * Callback interface for retry modem reboot
     *
     * @constructor Create empty Callback
     */
    interface Callback {
        fun onRetryModemRebootClicked()
        fun onRetryModemRebootCanceled()
    }
}
