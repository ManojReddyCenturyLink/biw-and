package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.FAQCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityFaqBinding
import com.centurylink.biwf.model.support.Videofaq
import com.centurylink.biwf.screens.subscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.subscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.adapter.ExpandableContentAdapter
import com.centurylink.biwf.screens.support.adapter.FAQVideoViewAdapter
import com.centurylink.biwf.screens.support.adapter.VideoItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import javax.inject.Inject


class FAQActivity : BaseActivity(), VideoItemClickListener {

    @Inject
    lateinit var faqCoordinator: FAQCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    private val faqViewModel by lazy {
        ViewModelProvider(this, factory).get(FAQViewModel::class.java)
    }
    private lateinit var binding: ActivityFaqBinding
    private var videoList: List<Videofaq> = mutableListOf()
    private var questionList: HashMap<String, String> = HashMap<String, String>()
    private lateinit var videoAdapter: FAQVideoViewAdapter
    private lateinit var questionAdapter: ExpandableContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        setHeightofActivity()

        faqViewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
            faqVideoData.observe(this@FAQActivity, Observer {
                prepareVideoRecyclerView(it)
            })
            faqQuestionsData.observe(this@FAQActivity, Observer {
                prepareQuestionRecyclerView(it)
            })
        }
        faqCoordinator.observeThis(faqViewModel.myState)

        initHeaders()
        initView()
        getFAQInformation()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onVideoItemClicked(videoFAQ: Videofaq) {
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
        binding.activityHeaderView.subHeaderTitle.text = screenTitle
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subHeaderRightIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
    }

    private fun getFAQInformation() {
        faqViewModel.getFAQDetails().observe(this) {
            when {
                it.status.isLoading() -> {
                }
                it.status.isSuccessful() -> {
                    faqViewModel.sortQuestionsAndVideos(it.data!!.videolist, it.data!!.questionlist)
                    displaySortedFAQ()
                }
                it.status.isError() -> {

                }
            }
        }
    }

    private fun prepareVideoRecyclerView(videolist: List<Videofaq>) {
        videoList = videolist
        videoAdapter = FAQVideoViewAdapter(videoList, this)
        binding.faqVideoList.adapter = videoAdapter
    }

    private fun prepareQuestionRecyclerView(questionlist: HashMap<String, String>) {
        questionList = questionlist
        questionAdapter = ExpandableContentAdapter(questionList)
        binding.questionsAnswersListView.setAdapter(questionAdapter)
    }

    private fun initView() {
        binding.faqContactUs.contactUsHeading.visibility = View.GONE
        binding.faqContactUs.scheduleCallbackTextview.setOnClickListener { faqViewModel.navigateToScheduleCallback() }
        binding.faqVideoList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.faqVideoList.isNestedScrollingEnabled = false
        binding.questionsAnswersListView.isNestedScrollingEnabled = false
        binding.faqVideoList.addItemDecoration(myDivider)
    }

    private fun displaySortedFAQ() {}

    companion object {
        const val FAQ_TITLE: String = "FaqTitle"
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, FAQActivity::class.java).putExtra(
                FAQ_TITLE, bundle.getString(FAQ_TITLE)
            )
        }
    }
}