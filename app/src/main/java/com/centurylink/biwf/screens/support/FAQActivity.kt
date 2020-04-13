package com.centurylink.biwf.screens.support

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
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
        fun newIntent(context: Context) = Intent(context, FAQActivity::class.java)
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
        //setContentView(R.layout.activity_faq)
        setContentView(binding.root)
        setHeightofActivity()
        faqViewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
        }
        initView()
        getFAQInformation()
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

    override fun onResume() {
        super.onResume()
        faqCoordinator.navigator.activity = this
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onVideoItemClicked(videoFAQ: Videofaq) {
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
        setExpandableListViewHeight(binding.questionsAnswersListView, -1);
    }

    private fun initView(){
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
       // binding.questionsListView.isNestedScrollingEnabled=false
        faqViewModel.getVideoFAQLiveData().observe(this, Observer {
            prepareVideoRecyclerView(it)
        })
    }

    private fun setExpandableListViewHeight(
        listView: ExpandableListView,
        group: Int
    ) {
        val listAdapter =
            listView.expandableListAdapter as ExpandableListAdapter
        var totalHeight = 0
        val desiredWidth: Int = View.MeasureSpec.makeMeasureSpec(
            listView.width,
            View.MeasureSpec.EXACTLY
        )
        for (i in 0 until listAdapter.groupCount) {
            val groupItem: View = listAdapter.getGroupView(i, false, null, listView)
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += groupItem.getMeasuredHeight()
            if (listView.isGroupExpanded(i) && i != group
                || !listView.isGroupExpanded(i) && i == group
            ) {
                for (j in 0 until listAdapter.getChildrenCount(i)) {
                    val listItem: View = listAdapter.getChildView(
                        i, j, false, null,
                        listView
                    )
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.getMeasuredHeight()
                }
            }
        }
        val params = listView.layoutParams
        var height = (totalHeight
                + listView.dividerHeight * (listAdapter.groupCount - 1))
        if (height < 10) height = 200
        params.height = height
        listView.layoutParams = params
        listView.requestLayout()
    }
}