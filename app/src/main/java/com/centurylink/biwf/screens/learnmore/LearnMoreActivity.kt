package com.centurylink.biwf.screens.learnmore

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.centurylink.biwf.R

class LearnMoreActivity : AppCompatActivity() {

    companion object{
        fun newIntent(context: Context) = Intent(context,LearnMoreActivity::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_more)
    }
}
