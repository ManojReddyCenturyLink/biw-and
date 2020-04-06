package com.centurylink.biwf.screens.changeappointment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.centurylink.biwf.R

class ChangeAppointmentActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, ChangeAppointmentActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_appointment)
    }
}
