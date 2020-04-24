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

    private val scheduleCallbackViewModel by lazy {
        ViewModelProvider(this, factory).get(ScheduleCallbackViewModel::class.java)
    }
    private lateinit var adapter: ScheduleCallbackAdapter
    private lateinit var binding: ActivityScheduleCallbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleCallbackBinding.inflate(layoutInflater)
        scheduleCallbackViewModel.apply {
        }
        scheduleCallbackCoordinator.observeThis(scheduleCallbackViewModel.myState)
        setContentView(binding.root)
        scheduleCallbackViewModel.apply {
            prepareRecyclerView(topicList)
        }
        initHeaders()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        scheduleCallbackCoordinator.navigator.activity = this
    }

    override fun onItemClick(item: TopicList) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initHeaders() {
        binding.scheduleCallbackRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val screenTitle: String = getString(R.string.schedule_callback)
        binding.incHeader.subheaderCenterTitle.text = screenTitle
        binding.incHeader.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.incHeader.subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
        binding.incHeader.subheaderRightActionTitle.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
        binding.callUsNowTextview.setOnClickListener { scheduleCallbackViewModel.launchCallDialer() }
    }

    private fun prepareRecyclerView(list: MutableList<TopicList>) {
        adapter = ScheduleCallbackAdapter(this, this, list)
        binding.scheduleCallbackRecyclerview.adapter = adapter
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ScheduleCallbackActivity::class.java)
        }
    }
}