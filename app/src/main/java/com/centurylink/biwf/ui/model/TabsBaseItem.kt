package com.centurylink.biwf.ui.model

import androidx.annotation.StringRes

open class TabsBaseItem (val indextype: Int, @StringRes val titleRes: Int){

    companion object {
        const val DEVICES = 0
        const val DASHBOARD = 1
        const val ACCOUNT = 2
        const val NOTIFICATION = 3
    }

    override fun hashCode(): Int {
        return indextype
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabsBaseItem

        if (indextype != other.indextype) return false
        if (titleRes != other.titleRes) return false

        return true
    }

}