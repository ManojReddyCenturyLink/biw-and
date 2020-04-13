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
import com.centurylink.biwf.databinding.ActivityFaqBinding
import com.centurylink.biwf.model.support.Videofaq
import com.centurylink.biwf.screens.support.adapter.ExpandableContentAdapter
import com.centurylink.biwf.screens.support.adapter.FAQVideoViewAdapter
import com.centurylink.biwf.screens.support.adapter.VideoItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import javax.inject.Inject


class FAQActivity : BaseActivity(),VideoItemClickListener{

    companion object {
        const val faqTitle: String = "FaqTitle"
        const val requestToHome :Int= 1100;

        fun newIntent(context: Context, bundle:Bundle) :Intent {
            return Intent(context, FAQActivity::class.java).putExtra(
                faqTitle, bundle.getString(faqTitle)
            )
        }
    }

    @Inject
    lateinit var faqCoordinator: FAQCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val faqViewModel by lazy {
        ViewModelProvider(this, factory).get(FAQViewModel::class.java)
    }
    private lateinit var binding: ActivityFaqBinding
    private var videoList: List<Videofaq> = mutableListOf()
    private var questionList: HashMap<String,String> = HashMap<String,String>()
    private lateinit var videoAdapter: FAQVideoViewAdapter
    private lateinit var questionAdapter: ExpandableContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        faqViewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
        }
        initHeaders()
        initView()
        getFAQInformation()
    }

    override fun onResume() {
        super.onResume()
        faqCoordinator.navigator.activity = this
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onVideoItemClicked(videoFAQ: Videofaq) {
    }

    private fun initHeaders(){
        var screenTitle :String= intent.getStringExtra(faqTitle)
        binding.activityHeaderView.subHeaderTitle.text=screenTitle
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subHeaderRightIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish() }
        binding.activitySupportView.supportCallUsLink.visibility=View.GONE
    }
    private fun getFAQInformation() {
        faqViewModel.getFAQDetails().observe(this) {
            when {
                it.status.isLoading() -> {
                }
                it.status.isSuccessful() -> {
                    faqViewModel.sortQuestionsAndVideos(it.data!!.videolist,it.data!!.questionlist)
                    displaySortedFAQ()
                }
                it.status.isError() -> {

                }
            }
        }
    }

    private fun prepareVideoRecyclerView( videolist: List<Videofaq>) {
        videoList = videolist
        videoAdapter = FAQVideoViewAdapter(videoList, this)
        binding.faqVideoList.adapter = videoAdapter
    }

    private fun prepareQuestionRecyclerView(questionlist: HashMap<String,String>) {
        questionList = questionlist
        questionAdapter = ExpandableContentAdapter(questionList)
        binding.questionsAnswersListView.setAdapter(questionAdapter)
    }

    private fun initView(){
        binding.activitySupportView.contactUsHeading.visibility= View.GONE
        binding.faqVideoList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.faqVideoList.addItemDecoration(myDivider)
    }

    private fun displaySortedFAQ() {
        faqViewModel.getQuestionFAQLiveData().observe(this, Observer {
            prepareQuestionRecyclerView(it)
        })
        binding.faqVideoList.isNestedScrollingEnabled=false
        binding.questionsAnswersListView.isNestedScrollingEnabled=false
        faqViewModel.getVideoFAQLiveData().observe(this, Observer {
            prepareVideoRecyclerView(it)
        })
    }
}