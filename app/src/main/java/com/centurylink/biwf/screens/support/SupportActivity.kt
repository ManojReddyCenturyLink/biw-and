package com.centurylink.biwf.screens.support

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.centurylink.biwf.R
import com.centurylink.biwf.databinding.ActivitySupportBinding

class SupportActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, SupportActivity::class.java)
    }
    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.SupportText.setOnClickListener{
            startActivity(FAQActivity.newIntent(this))
        }
    }
}
