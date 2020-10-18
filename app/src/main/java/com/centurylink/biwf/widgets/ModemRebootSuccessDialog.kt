package com.centurylink.biwf.widgets

import android.os.Bundle
import com.centurylink.biwf.R

/**
 * It will handle callback events in case of modem reboot success
 *
 * @constructor Create empty Modem reboot success dialog
 */
class ModemRebootSuccessDialog : CustomDialogBlueTheme({ /* no-op */ }) {

    /**
     * On create - called to do initial creation of the fragment.
     *
     * @param savedInstanceState - Bundle: If the fragment is being re-created from a previous
     *                             saved state, this is the state. This value may be null
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.modem_reboot_success_title)
        message = getString(R.string.modem_reboot_success_message)
        buttonText = getString(R.string.modem_reboot_success_button)
        isErrorPopup = false
    }
}
