package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.FAQCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityFaqBinding
import com.centurylink.biwf.screens.support.adapter.ExpandableContentAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.salesforce.android.chat.core.ChatConfiguration
import com.salesforce.android.chat.ui.ChatUI
import com.salesforce.android.chat.ui.ChatUIClient
import com.salesforce.android.chat.ui.ChatUIConfiguration
import javax.inject.Inject

class FAQActivity : BaseActivity(){

    @Inject
    lateinit var faqCoordinator: FAQCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(FAQViewModel::class.java)
    }

    private lateinit var binding: ActivityFaqBinding

    private lateinit var questionAdapter: ExpandableContentAdapter

    private lateinit var chatUIClient: ChatUIClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.faqListLayout,
            binding.retryOverlay.root
        )
        viewModel.apply {
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
        }
        viewModel.setFilteredSelection(intent.getStringExtra(FAQ_TITLE)!!)
        navigator.observe(this)
        viewModel.myState.observeWith(faqCoordinator)
        initLiveChat()
        initHeaders()
        initView()
        observeViews()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun retryClicked() {
        showProgress(true)
        viewModel.initApis()
    }

    private fun observeViews() {
        viewModel.apply {
            faqDetailsInfo.observe {
                prepareQuestionRecyclerView(it.questionMap)
            }
        }
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
        val screenTitle: String = intent.getStringExtra(FAQ_TITLE)!!
        binding.activityHeaderView.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackButtonClick()
                finish() }
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logDoneButtonClick()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun prepareQuestionRecyclerView(questionlist: HashMap<String, String>) {
        questionAdapter = ExpandableContentAdapter(questionlist)
        binding.questionsAnswersListView.setAdapter(questionAdapter)
    }

    private fun initView() {
        binding.faqContactUs.apply {
            contactUsHeading.visibility = View.GONE
            scheduleCallbackRow.setOnClickListener { viewModel.navigateToScheduleCallback() }
            liveChatTextview.setOnClickListener {
                viewModel.logLiveChatLaunch()
                chatUIClient?.startChatSession(
                    this@FAQActivity
                )
            }
        }
        binding.faqVideoList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.faqVideoList.isNestedScrollingEnabled = false
        binding.questionsAnswersListView.isNestedScrollingEnabled = false
        binding.questionsAnswersListView.setOnGroupClickListener { expandableListView, view, i, l ->
            if (expandableListView.isGroupExpanded(i)) {
                viewModel.logItemCollapsed()
            } else {
                viewModel.logItemExpanded()
            }
            return@setOnGroupClickListener false
        }
        binding.faqVideoList.addItemDecoration(myDivider)
    }

    private fun initLiveChat() {
        val chatConfiguration =
            ChatConfiguration.Builder(
                ORG_ID,
                BUTTON_ID,
                DEPLOYMENT_ID,
                AGENT_POD
            ).build()

        ChatUI.configure(ChatUIConfiguration.create(chatConfiguration)).createClient(this)
            .onResult { _, uiClient ->
                chatUIClient = uiClient
            }
    }

    companion object {
        const val FAQ_TITLE: String = "FaqTitle"
        const val AGENT_POD = "d.la1-c1cs-ord.salesforceliveagent.com"
        const val ORG_ID = "00Df0000002HOQc"
        const val DEPLOYMENT_ID = "572f0000000Cauc"
        const val BUTTON_ID = "573f000000000zz"
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, FAQActivity::class.java).putExtra(
                FAQ_TITLE, bundle.getString(FAQ_TITLE)
            )
        }
    }
}
