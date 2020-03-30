package com.centurylink.biwf

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule

abstract class ViewModelBaseTest : BaseTest() {

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()
}