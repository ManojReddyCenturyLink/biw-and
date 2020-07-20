package com.centurylink.biwf.widgets

import android.content.Context
import com.centurylink.biwf.R

class ModemRebootSuccessDialog : CustomDialogBlueTheme() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        title = getString(R.string.modem_reboot_success_title)
        message = getString(R.string.modem_reboot_success_message)
        buttonText = getString(R.string.modem_reboot_success_button)
        isErrorPopup = false
        callback = object: ErrorDialogCallback {
            override fun onErrorDialogCallback(buttonType: Int) {
                // no-op
            }
        }
    }
}