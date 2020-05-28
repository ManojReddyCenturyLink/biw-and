package com.centurylink.biwf.service.impl.integration

import android.content.Context
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream

class LocalJsonParser {
    companion object {
        fun inputStreamToString(inputStream: InputStream): String {
            try {
                val bytes = ByteArray(inputStream.available())
                inputStream.read(bytes, 0, bytes.size)
                return String(bytes)
            } catch (e: IOException) {
                return ""
            }
        }
    }
}

