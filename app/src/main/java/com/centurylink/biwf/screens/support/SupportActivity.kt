package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding

import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory

import javax.inject.Inject

class SupportActivity : BaseActivity(), SupportItemClickListener {

    @Inject
    lateinit var supportCoordinator: SupportCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    private val supportViewModel by lazy {
        ViewModelProvider(this, factory).get(SupportViewModel::class.java)
    }

    private lateinit var adapter: SupportFAQAdapter
    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        supportViewModel.myState.observeWith(supportCoordinator)
        initViews()
        observeViews()

    }

    private fun observeViews(){
        supportViewModel.apply {
            faqSectionInfo.observe {
                prepareRecyclerView(it.questionMap)
            }
        }
    }

    override fun onFaqItemClick(item: String) {
        supportViewModel.navigateToFAQList(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION -> {
                if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
                    setResult(CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT)
                    finish()
                } else if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
            FAQActivity.REQUEST_TO_HOME -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initViews() {
        binding.supportFaqTopicsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.doneButtonSupport.setOnClickListener { finish() }

        binding.incTroubleshooting.apply {
            restartModemButton.setOnClickListener { supportViewModel.restartModem() }
            runSpeedTestButton.setOnClickListener { supportViewModel.runSpeedTest() }
            supportVisitWebsite.setOnClickListener {
                //TODO Add Website feature when url is available
            }
        }

        binding.incContactUs.liveChatTextview.setOnClickListener { }
        binding.incContactUs.scheduleCallbackRow.setOnClickListener { supportViewModel.launchScheduleCallback() }
    }


    private fun prepareRecyclerView(list: List<String>) {
        adapter = SupportFAQAdapter(this, this, list)
        binding.supportFaqTopicsRecyclerview.adapter = adapter
    }

    companion object {
        const val REQUEST_TO_HOME: Int = 12200

        fun newIntent(context: Context): Intent {
            return Intent(context, SupportActivity::class.java)
        }
    }
}
