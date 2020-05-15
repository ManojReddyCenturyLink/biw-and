package com.centurylink.biwf.coordinators

/**
 * Interface that each navigation Coordinator should implement.
 */
interface Coordinator<T : Any> {
    /**
     * Causes the app to navigate to the given [destination].
     */
    fun navigateTo(destination: T)
}
