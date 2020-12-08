package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.home.SpeedTestUtils
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.AppUtil
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import com.centurylink.biwf.widgets.NoNetworkErrorPopup.Companion.showNoInternetDialog
import com.salesforce.android.chat.ui.ChatUI
import com.salesforce.android.chat.ui.ChatUIClient
import javax.inject.Inject

class SupportActivity : BaseActivity(), SupportItemClickListener {

    @Inject
    lateinit var supportCoordinator: SupportCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(SupportViewModel::class.java)
    }

    private val fragmentManager = supportFragmentManager
    private lateinit var adapter: SupportFAQAdapter
    private lateinit var binding: ActivitySupportBinding
    private lateinit var chatUIClient: ChatUIClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        viewModel.myState.observeWith(supportCoordinator)
        initHeaders()
        initButtonStates()
        initLiveChat()
        initViews()
        observeViews()
    }

    override fun onFaqItemClick(item: String) {
        viewModel.navigateToFAQList(item)
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

    override fun retryClicked() {
        showProgress(true)
        viewModel.initApis()
    }

    private fun initHeaders() {
        val screenTitle: String = getString(R.string.support)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logDoneButtonClick()
                finish()
            }
        }
    }

    private fun initButtonStates() {
        viewModel.networkStatus.observe {
            if (SpeedTestUtils.isSpeedTestAvailable()) {
                speedTestButtonStates(it)
            } else {
                rebootModemButtonStates(it)
            }
        }
    }

    private fun speedTestButtonStates(it: Boolean) {
        binding.incTroubleshootingNoSpeedTest.root.visibility = View.GONE
        binding.incTroubleshooting.root.visibility = View.VISIBLE
        if (!it) {
            binding.incTroubleshooting.runSpeedTestButton.isActivated = false
            binding.incTroubleshooting.runSpeedTestButton.isEnabled = false
            binding.incTroubleshooting.rebootModemButton.isActivated = false
            binding.incTroubleshooting.rebootModemButton.isEnabled = false
        } else {
            binding.incTroubleshooting.runSpeedTestButton.isActivated = true
            binding.incTroubleshooting.runSpeedTestButton.isEnabled = true
            binding.incTroubleshooting.rebootModemButton.isActivated = true
            binding.incTroubleshooting.rebootModemButton.isEnabled = true
        }
    }

    private fun rebootModemButtonStates(it: Boolean) {
        val isExistingUser: Boolean = intent.getBooleanExtra(IS_EXISTING_USER, false)
        if (isExistingUser)
            binding.incTroubleshootingNoSpeedTest.root.visibility = View.VISIBLE
        else
            binding.incTroubleshootingNoSpeedTest.root.visibility = View.GONE
        binding.incTroubleshooting.root.visibility = View.GONE

        if (!it) {
            binding.incTroubleshootingNoSpeedTest.rebootModemButton.isActivated = false
            binding.incTroubleshootingNoSpeedTest.rebootModemButton.isEnabled = false
        } else {
            binding.incTroubleshootingNoSpeedTest.rebootModemButton.isActivated = true
            binding.incTroubleshootingNoSpeedTest.rebootModemButton.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        initButtonStates()
    }

    private fun observeViews() {
        viewModel.apply {
            faqSectionInfo.observe {
                prepareRecyclerView(it.questionMap)
            }

            viewModel.rebootDialogStatusFlow.observe {
                if (it) {
                    setRebootButtonVisibilityWithoutSpeedTest(false)
                }
            }

            viewModel.rebootOngoingFlow.observe {
                if (it) {
                    binding.incTroubleshootingNoSpeedTest.rebootModemButton.visibility =
                        View.GONE
                    binding.incTroubleshooting.rebootModemButton.isActivated = true
                    binding.incTroubleshooting.rebootModemButton.isEnabled = true
                    binding.incTroubleshootingNoSpeedTest.rebootingModemButton.root.visibility =
                        View.VISIBLE
                }
            }

            viewModel.rebootCanceledFlow.observe {
                if (it) {
                    binding.incTroubleshootingNoSpeedTest.rebootModemButton.visibility =
                        View.VISIBLE
                    binding.incTroubleshooting.rebootModemButton.isActivated = true
                    binding.incTroubleshooting.rebootModemButton.isEnabled = true
                    binding.incTroubleshootingNoSpeedTest.rebootingModemButton.root.visibility =
                        View.GONE
                }
            }
        }
    }

    private fun initViews() {
        val isExistingUser: Boolean = intent.getBooleanExtra(IS_EXISTING_USER, false)
        viewModel.setExistingUserState(isExistingUser)
        if (isExistingUser) binding.incTroubleshooting.root.visibility = View.VISIBLE
        else binding.incTroubleshooting.root.visibility = View.GONE
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.supportScrollView,
            binding.retryOverlay.root
        )
        viewModel.apply {

            if (SpeedTestUtils.isSpeedTestAvailable()) {
                speedTestObserver()
                modemRebootObserverWithSpeedTest()
            } else {
                modemRebootObserverWithoutSpeedTest()
            }
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            detailedRebootStatusFlow.observe { rebootStatus ->
                if (SpeedTestUtils.isSpeedTestAvailable()) {
                    speedTestButtonObserver(rebootStatus)
                } else {
                    rebootModemButtonObserver(rebootStatus)
                }
            }
        }
        binding.supportFaqTopicsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.incTroubleshooting.apply {
            runSpeedTestButton.setOnClickListener { viewModel.startSpeedTest() }
            if (SpeedTestUtils.isSpeedTestAvailable()) {
                performRebootWithSpeedTest()
            } else {
                performRebootWithoutSpeedTest()
            }
        }

        binding.incContactUs.liveChatTextview.setOnClickListener {
            viewModel.logLiveChatLaunch()
            if (AppUtil.isOnline(this)) {
                chatUIClient.startChatSession(this)
            } else {
                showNoInternetDialog(fragmentManager, callingActivity?.className)
            }
        }
        binding.incContactUs.scheduleCallbackRow.setOnClickListener {
            viewModel.launchScheduleCallback(
                isExistingUser
            )
        }
        initButtonStates()
    }

    private fun performRebootWithSpeedTest() {
        binding.incTroubleshooting.apply {
            rebootModemButton.setOnClickListener {
                if (!binding.incTroubleshooting.downloadProgressIcon.isVisible) {
                    handleModemDialogSelection()
                }
            }
        }
    }

    private fun performRebootWithoutSpeedTest() {
        binding.incTroubleshootingNoSpeedTest.apply {
            rebootModemButton.setOnClickListener {
                handleModemDialogSelection()
            }
        }
    }

    private fun speedTestButtonObserver(rebootStatus: ModemRebootMonitorService.RebootState) {
        when (rebootStatus) {
            ModemRebootMonitorService.RebootState.READY -> {
                setRebootButtonVisibility(false)
                setRunSpeedTestButtonVisibility(false)
            }
            ModemRebootMonitorService.RebootState.ONGOING -> {
                setRebootButtonVisibility(true)
                setRunSpeedTestButtonVisibility(true)
            }
            ModemRebootMonitorService.RebootState.SUCCESS -> {
                setRebootButtonVisibility(false)
                setRunSpeedTestButtonVisibility(false)
                showModemRebootSuccessDialog()
            }
            ModemRebootMonitorService.RebootState.ERROR -> {
                setRebootButtonVisibility(false)
                setRunSpeedTestButtonVisibility(false)
                showModemRebootErrorDialog()
            }
        }
    }

    private fun rebootModemButtonObserver(rebootStatus: ModemRebootMonitorService.RebootState) {
        when (rebootStatus) {
            ModemRebootMonitorService.RebootState.READY -> {
                setRebootButtonVisibilityWithoutSpeedTest(false)
            }
            ModemRebootMonitorService.RebootState.ONGOING -> {
                setRebootButtonVisibilityWithoutSpeedTest(true)
            }
            ModemRebootMonitorService.RebootState.SUCCESS -> {
                setRebootButtonVisibilityWithoutSpeedTest(false)
                showModemRebootSuccessDialog()
            }
            ModemRebootMonitorService.RebootState.ERROR -> {
                setRebootButtonVisibilityWithoutSpeedTest(false)
                showModemRebootErrorDialog()
            }
        }
    }

    private fun modemRebootObserverWithSpeedTest() {
        binding.incTroubleshooting.root.visibility = View.VISIBLE
        binding.incTroubleshootingNoSpeedTest.root.visibility = View.GONE
        viewModel.apply {
            modemResetButtonState.observe {
                viewModel.networkStatus.observe { networkStatus ->
                    if (networkStatus) {
                        binding.incTroubleshooting.rebootModemButton.isActivated = it
                        binding.incTroubleshooting.rebootModemButton.isEnabled = it
                    }
                }
            }
        }
    }

    private fun modemRebootObserverWithoutSpeedTest() {
        binding.incTroubleshooting.root.visibility = View.GONE
        binding.incTroubleshootingNoSpeedTest.root.visibility = View.VISIBLE
        viewModel.apply {
            modemResetButtonState.observe {
                viewModel.networkStatus.observe { networkStatus ->
                    if (networkStatus) {
                        binding.incTroubleshootingNoSpeedTest.rebootModemButton.isActivated = it
                        binding.incTroubleshootingNoSpeedTest.rebootModemButton.isEnabled = it
                    }
                }
            }
        }
    }

    private fun speedTestObserver() {
        viewModel.apply {
            uploadSpeed.observe { binding.incTroubleshooting.uploadSpeed.text = it }
            downloadSpeed.observe { binding.incTroubleshooting.downloadSpeed.text = it }
            latestSpeedTest.observe { binding.incTroubleshooting.lastSpeedTestTime.text = it }
            progressVisibility.observe {
                binding.incTroubleshooting.uploadSpeed.visibility =
                    if (it) View.INVISIBLE else View.VISIBLE
                binding.incTroubleshooting.downloadSpeed.visibility =
                    if (it) View.INVISIBLE else View.VISIBLE
                binding.incTroubleshooting.downloadProgressIcon.visibility =
                    if (it) View.VISIBLE else View.INVISIBLE
                binding.incTroubleshooting.uploadProgressIcon.visibility =
                    if (it) View.VISIBLE else View.INVISIBLE
                binding.incTroubleshooting.runSpeedTestButton.isActivated = !it
                binding.incTroubleshooting.runSpeedTestButton.isEnabled = !it
            }
            speedTestError.observe {
                if (it) {
                    speedTestErrorDialog()
                }
            }
        }
    }

    private fun handleModemDialogSelection() {
        CustomDialogGreyTheme(
            getString(R.string.restart_modem_confirmation_title),
            getString(R.string.restart_modem_confirmation_message),
            getString(R.string.restart),
            getString(R.string.text_header_cancel),
            ::onScreenExitConfirmationDialogCallback
        ).show(
            supportFragmentManager,
            callingActivity?.className
        )
    }

    private fun onScreenExitConfirmationDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                setRebootButtonVisibilityWithoutSpeedTest(true)
                viewModel.rebootModem()
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                /* no-op */
            }
        }
    }

    private fun speedTestErrorDialog() {
        CustomDialogBlueTheme(
            title = getString(R.string.speed_test_error_title),
            message = getString(R.string.speed_test_error_message),
            buttonText = getString(R.string.ok),
            isErrorPopup = true,
            callback = ::onErrorDialogCallback
        ).show(
            supportFragmentManager,
            callingActivity?.className
        )
    }

    private fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                /** no op **/
            }
        }
    }

    private fun initLiveChat() {
        ChatUI.configure(viewModel.getLiveChatUIConfiguration()).createClient(this)
            .onResult { _, uiClient ->
                chatUIClient = uiClient
            }
    }

    private fun prepareRecyclerView(list: List<String>) {
        adapter = SupportFAQAdapter(this, this, list)
        binding.supportFaqTopicsRecyclerview.adapter = adapter
    }

    private fun setRebootButtonVisibility(restarting: Boolean) {
        binding.incTroubleshooting.rebootModemButton.visibility =
            if (restarting) View.GONE else View.VISIBLE
        binding.incTroubleshooting.rebootingModemButton.root.visibility =
            if (restarting) View.VISIBLE else View.GONE
    }

    private fun setRebootButtonVisibilityWithoutSpeedTest(restarting: Boolean) {
        binding.incTroubleshootingNoSpeedTest.rebootModemButton.visibility =
            if (restarting) View.GONE else View.VISIBLE
        binding.incTroubleshootingNoSpeedTest.rebootingModemButton.root.visibility =
            if (restarting) View.VISIBLE else View.GONE
    }

    private fun setRunSpeedTestButtonVisibility(restarting: Boolean) {
        binding.incTroubleshooting.runSpeedTestButton.isActivated =
            !restarting
        binding.incTroubleshooting.runSpeedTestButton.isEnabled =
            !restarting
    }

    companion object {
        const val REQUEST_TO_HOME: Int = 12200
        const val IS_EXISTING_USER = "IS_EXISTING_USER"

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, SupportActivity::class.java)
                .putExtra(IS_EXISTING_USER, bundle.getBoolean(IS_EXISTING_USER))
        }
    }
}
