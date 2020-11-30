package com.centurylink.biwf.utility

import org.greenrobot.eventbus.EventBus

/**
 * Global bus  - GlobalBus class used to initialise the event bus object
 *
 * @constructor Create empty Events
*/
object GlobalBus {
    private var sBus: EventBus? = null
    @JvmStatic
    val bus: EventBus?
        get() {
            if (sBus == null) sBus = EventBus.getDefault()
            return sBus
        }
}
