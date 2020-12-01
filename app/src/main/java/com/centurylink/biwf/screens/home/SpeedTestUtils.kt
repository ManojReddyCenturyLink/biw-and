package com.centurylink.biwf.screens.home

import com.centurylink.biwf.model.assia.ModemInfo

class SpeedTestUtils {
    companion object {

        var speedTestEnable = false

        fun isSpeedTestAvailable(): Boolean {
            return true
        }

        fun setSpeedTestStatus(modemInfo: ModemInfo) {
            speedTestEnable = modemInfo.speedTestEnable
        }
    }
}
