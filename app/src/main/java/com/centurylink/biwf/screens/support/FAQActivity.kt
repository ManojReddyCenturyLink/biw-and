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
import com.centurylink.biwf.model.support.Videofaq
import com.centurylink.biwf.screens.support.adapter.ExpandableContentAdapter
import com.centurylink.biwf.screens.support.adapter.VideoItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
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

    private lateinit var questionAdapter: ExpandableContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        faqViewModel.setFilteredSelection(intent.getStringExtra(FAQ_TITLE)!!)
        navigator.observe(this)
        faqViewModel.myState.observeWith(faqCoordinator)

        initHeaders()
        initView()
        observeViews()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun observeViews() {
        faqViewModel.apply {
            faqDetailsInfo.observe {
                prepareQuestionRecyclerView(it.questionMap)
            }
        }
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
        binding.activityHeaderView.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener { finish() }
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
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
            scheduleCallbackRow.setOnClickListener { faqViewModel.navigateToScheduleCallback() }
        }
        binding.faqVideoList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.faqVideoList.isNestedScrollingEnabled = false
        binding.questionsAnswersListView.isNestedScrollingEnabled = false
        binding.faqVideoList.addItemDecoration(myDivider)
    }

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
