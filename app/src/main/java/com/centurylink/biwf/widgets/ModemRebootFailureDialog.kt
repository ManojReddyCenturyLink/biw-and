package com.centurylink.biwf.widgets

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.centurylink.biwf.R

class ModemRebootFailureDialog(
    private val rebootCallback: Callback
) : CustomDialogGreyTheme(object : DialogCallback {
    override fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                rebootCallback.onRetryModemRebootClicked()
            }
        }
    }
}) {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        title = getString(R.string.modem_reboot_error_title)
        message = getString(R.string.modem_reboot_error_message)
        positiveText = getString(R.string.modem_reboot_error_button_positive)
        negativeText = getString(R.string.modem_reboot_error_button_negative)
    }

    interface Callback {
        fun onRetryModemRebootClicked()
    }
}
