package com.centurylink.biwf.utility

import android.text.Editable
import android.text.TextWatcher

fun afterTextChanged(listener: TextWatcher.(Editable) -> Unit) = object : TextWatcher {
    override fun afterTextChanged(editText: Editable) {
        listener(editText)
    }

    override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}
}