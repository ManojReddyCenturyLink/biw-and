package com.centurylink.biwf.widgets

import android.os.Bundle
import com.centurylink.biwf.R

class ModemRebootSuccessDialog : CustomDialogBlueTheme({ /* no-op */ }) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.modem_reboot_success_title)
        message = getString(R.string.modem_reboot_success_message)
        buttonText = getString(R.string.modem_reboot_success_button)
        isErrorPopup = false
    }
}
