package com.centurylink.biwf

import io.mockk.MockKAnnotations
import org.junit.Before

abstract class BaseTest {

    @Before
    fun baseSetup() {
        MockKAnnotations.init(this)
    }
}
