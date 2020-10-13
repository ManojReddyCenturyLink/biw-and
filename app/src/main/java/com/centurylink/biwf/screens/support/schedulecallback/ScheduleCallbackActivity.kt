package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.BuildConfig
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

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(ScheduleCallbackViewModel::class.java)
    }
    private lateinit var adapter: ScheduleCallbackAdapter
    private lateinit var binding: ActivityScheduleCallbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleCallbackBinding.inflate(layoutInflater)
        viewModel.myState.observeWith(scheduleCallbackCoordinator)
        setContentView(binding.root)
        navigator.observe(this)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.scheduleCallbackLayout,
            binding.retryOverlay.root
        )
        viewModel.apply {
            progressViewFlow.observe { showProgress(it) }
            prepareRecyclerView(topicList)
        }

        initHeaders()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onItemClick(item: TopicList) {
        viewModel.navigateAdditionalInfoScreen(item)
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
        val isExistingUserState = intent.getBooleanExtra(IS_EXISTING_USER, false)
        viewModel.setIsExistingUserState(isExistingUserState)
        binding.scheduleCallbackRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val screenTitle: String = getString(R.string.schedule_callback)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackButtonClick()
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logCancelButtonClick()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        binding.callUsNowTextview.text= resources.getString(R.string.call_us_now_at).plus(" ").plus(BuildConfig.MOBILE_NUMBER)
        binding.callUsNowLayout.setOnClickListener { viewModel.launchCallDialer() }
    }

    private fun prepareRecyclerView(list: List<TopicList>) {
        adapter = ScheduleCallbackAdapter(this, this, list)
        binding.scheduleCallbackRecyclerview.adapter = adapter
    }

    companion object {
        const val REQUEST_TO_HOME: Int = 1100
        const val IS_EXISTING_USER = "IS_EXISTING_USER"
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, ScheduleCallbackActivity::class.java)
                .putExtra(IS_EXISTING_USER, bundle.getBoolean(IS_EXISTING_USER))
        }
    }
}
