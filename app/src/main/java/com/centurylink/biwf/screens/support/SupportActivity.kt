package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.centurylink.biwf.R
import com.centurylink.biwf.coordinators.NotificationCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity

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
            var bundle= Bundle()
            bundle.putString(FAQActivity.faqTitle,"Wifi Connection")
            startActivityForResult(FAQActivity.newIntent(this,bundle),FAQActivity.requestToDashBoard)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            FAQActivity.requestToDashBoard->{
                if(resultCode== Activity.RESULT_OK){
                    finish()
                }
            }
        }
    }
}
