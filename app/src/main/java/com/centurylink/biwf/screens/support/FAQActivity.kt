package com.centurylink.biwf.screens.support

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var questionList: List<Videofaq> = mutableListOf()
    private lateinit var videoAdapter: FAQVideoViewAdapter
    private lateinit var questionAdapter: FAQVideoViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        faqViewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
        }
        initView()
        getFAQInformation()
    }

    private fun getFAQInformation() {
        Log.i("Pravin","FAQ Informations ")
        faqViewModel.getFAQDetails().observe(this) {
            when {
                it.status.isLoading() -> {
                }
                it.status.isSuccessful() -> {
                    Log.i("Pravin","FAQ information Success")
                    Log.i("Pravin","FAQ information Success")
                    faqViewModel.sortQuestionsAndVideos(it.data!!.videolist,it.data!!.questionlist)
                    displaySortedFAQ()
                }
                it.status.isError() -> {
                    Log.i("Pravin","FAQ information Failure"+it.errorMessage)
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

    private fun prepareQuestionRecyclerView(questionlist: List<Videofaq>) {
        questionList = questionlist
        questionAdapter = FAQVideoViewAdapter(questionList, this)
        binding.questionsListView.adapter = questionAdapter
    }

    private fun initView(){
        binding.faqVideoList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.faqVideoList.addItemDecoration(myDivider)

        binding.questionsListView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val VericalDivider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        VericalDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.questionsListView.addItemDecoration(VericalDivider)
    }

    private fun displaySortedFAQ() {
        faqViewModel.getQuestionFAQLiveData().observe(this, Observer {
            prepareQuestionRecyclerView(it)
        })
        faqViewModel.getVideoFAQLiveData().observe(this, Observer {
            prepareVideoRecyclerView(it)
        })
    }
}