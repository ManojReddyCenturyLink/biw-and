package com.centurylink.biwf.utility

import android.text.Editable
import android.text.TextWatcher
import timber.log.Timber

fun afterTextChanged(listener: TextWatcher.(Editable) -> Unit) = object : TextWatcher {
    override fun afterTextChanged(editText: Editable) {
        listener(editText)
    }

    override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
        Timber.e("Before Text changed")
    }

    override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
        Timber.e("After Text changed")
    }
}
