package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinator
import com.centurylink.biwf.databinding.ActivityScheduleCallbackBinding
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.screens.support.adapter.ScheduleCallbackAdapter
import com.centurylink.biwf.screens.support.adapter.ScheduleCallbackItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class ScheduleCallbackActivity : BaseActivity(), ScheduleCallbackItemClickListener {

    @Inject
    lateinit var scheduleCallbackCoordinator: ScheduleCallbackCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    private val scheduleCallbackViewModel by lazy {
        ViewModelProvider(this, factory).get(ScheduleCallbackViewModel::class.java)
    }
    private lateinit var adapter: ScheduleCallbackAdapter
    private lateinit var binding: ActivityScheduleCallbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleCallbackBinding.inflate(layoutInflater)
        scheduleCallbackViewModel.myState.observeWith(scheduleCallbackCoordinator)
        setContentView(binding.root)
        navigator.observe(this)

        scheduleCallbackViewModel.apply {
            prepareRecyclerView(topicList)
        }

        initHeaders()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onItemClick(item: TopicList) {
        scheduleCallbackViewModel.navigateAdditionalInfoScreen(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TO_HOME -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun initHeaders() {
        binding.scheduleCallbackRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val screenTitle: String = getString(R.string.schedule_callback)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener { finish() }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        binding.callUsNowLayout.setOnClickListener { scheduleCallbackViewModel.launchCallDialer() }
    }

    private fun prepareRecyclerView(list: List<TopicList>) {
        adapter = ScheduleCallbackAdapter(this, this, list)
        binding.scheduleCallbackRecyclerview.adapter = adapter
    }

    companion object {
        const val REQUEST_TO_HOME: Int = 1100
        fun newIntent(context: Context): Intent {
            return Intent(context, ScheduleCallbackActivity::class.java)
        }
    }
}