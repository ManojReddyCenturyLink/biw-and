package com.centurylink.biwf.screens.emptydesitination

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.centurylink.biwf.R

class ProfileActivity : AppCompatActivity() {

    companion object{
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }
}
