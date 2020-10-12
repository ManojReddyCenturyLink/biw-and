package com.centurylink.biwf.coordinators

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.centurylink.biwf.BuildConfig
import com.centurylink.biwf.R
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.changeappointment.AppointmentBookedActivity
import com.centurylink.biwf.screens.changeappointment.ChangeAppointmentActivity
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity.Companion.REQUEST_TO_DEVICES
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.screens.home.account.PersonalInfoActivity
import com.centurylink.biwf.screens.home.account.PersonalInfoActivity.Companion.REQUEST_TO_ACCOUNT_FROM_PERSONAL_INFO
import com.centurylink.biwf.screens.login.LoginActivity
import com.centurylink.biwf.screens.networkstatus.NetworkStatusActivity
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.screens.qrcode.QrScanActivity
import com.centurylink.biwf.screens.subscription.EditPaymentDetailsActivity
import com.centurylink.biwf.screens.subscription.SubscriptionActivity
import com.centurylink.biwf.screens.subscription.SubscriptionStatementActivity
import com.centurylink.biwf.screens.support.FAQActivity
import com.centurylink.biwf.screens.support.SupportActivity
import com.centurylink.biwf.screens.support.schedulecallback.AdditionalInfoActivity
import com.centurylink.biwf.screens.support.schedulecallback.ContactInfoActivity
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackActivity
import com.centurylink.biwf.screens.support.schedulecallback.SelectTimeActivity
import com.centurylink.biwf.utility.WebLinkUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigator class will have centralized navigation logic for Navigation to another screen.
 *
 * @constructor Create  Navigator class.
 */
@Suppress("unused")
@Singleton
class Navigator @Inject constructor() : LifecycleObserver {

    private val activity: AppCompatActivity? get() = ActivityObserver.resumedActivity

    fun observe(activity: AppCompatActivity) {
        ActivityObserver.observe(activity)
    }

    /**
     * Navigate to home screen
     *
     */
    fun navigateToHomeScreen() {
        activity?.also {
            it.startActivity(HomeActivity.newIntent(it))
            it.finishAffinity()
        }
    }

    /**
     * Navigate to support screen.
     *
     */
    fun navigateToSupport() {
        val bundle = HomeCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                SupportActivity.newIntent(it, bundle),
                SupportActivity.REQUEST_TO_HOME
            )
        }
    }

    /**
     * Navigate to change appointment Screen.
     *
     */
    fun navigateToChangeAppointment() {
        activity?.also {
            it.startActivityForResult(
                ChangeAppointmentActivity.newIntent(it),
                ChangeAppointmentActivity.REQUEST_TO_DASHBOARD
            )
        }
    }

    /**
     * Navigate to notification list Screen.
     *
     */
    fun navigateToNotificationList() {
        activity?.also {
            it.startActivity(NotificationActivity.newIntent(it))
        }
    }

    /**
     * Navigate to Notification details Screen.
     *
     */
    fun navigateToNotificationDetails() {
        activity?.also {
            it.startActivityForResult(
                NotificationDetailsActivity.newIntent(
                    it,
                    NotificationCoordinatorDestinations.bundle
                ),
                NotificationDetailsActivity.REQUEST_TO_DISMISS
            )
        }
    }

    /**
     * Navigate to faq screen.
     *
     */
    fun navigateToFaq() {
        activity?.also {
            it.startActivityForResult(
                FAQActivity.newIntent(it, SupportCoordinatorDestinations.bundle),
                FAQActivity.REQUEST_TO_HOME
            )
        }
    }

    /**
     * Navigate to subscription Activity.
     *
     */
    fun navigateToSubscriptionActivity() {
        activity?.also {
            it.startActivityForResult(SubscriptionActivity.newIntent(it, HomeCoordinatorDestinations.bundle),
                SubscriptionActivity.REQUEST_TO_SUBSCRIPTION_DETAILS)
        }
    }

    /**
     * Navigate to mange subscription Screen.
     *
     */
    fun navigateToMangeSubscription() {
        activity?.also {
            it.startActivityForResult(
                CancelSubscriptionActivity.newIntent(it),
                CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION
            )
        }
    }

    /**
     * Navigate to schedule callback
     *
     */
    fun navigateToScheduleCallback() {
        val bundle = SupportCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                ScheduleCallbackActivity.newIntent(it, bundle),
                ScheduleCallbackActivity.REQUEST_TO_HOME
            )
        }
    }

    /**
     * Navigate to phone dialler.
     *
     */
    fun navigateToPhoneDialler() {
        WebLinkUtil.handleClick(activity!!.getString(R.string.tel).plus(BuildConfig.MOBILE_NUMBER), activity!!)
    }

    /**
     * Navigate to cancel subscription details Screen.
     *
     */
    fun navigateToCancelSubscriptionDetails() {
        activity?.also {
            it.startActivityForResult(
                CancelSubscriptionDetailsActivity.newIntent(it),
                CancelSubscriptionDetailsActivity.REQUEST_TO_CANCEL_SUBSCRIPTION
            )
        }
    }

    /**
     * Navigate to personal info activity
     *
     */
    fun navigateToPersonalInfoActivity() {
        val bundle = AccountCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                PersonalInfoActivity.newIntent(it, bundle),
                REQUEST_TO_ACCOUNT_FROM_PERSONAL_INFO
            )
        }
    }

    /**
     * Navigate to additional info Screen.
     *
     */
    fun navigateToAdditionalInfo() {
        val bundle = ScheduleCallbackCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                AdditionalInfoActivity.newIntent(it, bundle),
                AdditionalInfoActivity.REQUEST_TO_HOME
            )
        }
    }

    /**
     * Navigate to Edit payment details Screen.
     *
     */
    fun navigateToEditPaymentDetails() {
        activity?.also {
            it.startActivityForResult(
                EditPaymentDetailsActivity.newIntent(it),
                EditPaymentDetailsActivity.REQUEST_TO_EDIT_PAYMENT_DETAILS
            )
        }
    }

    /**
     * Navigate to bill statement Screen
     *
     */
    fun navigateToBillStatement() {
        val bundle = SubscriptionCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                SubscriptionStatementActivity.newIntent(it, bundle),
                SubscriptionStatementActivity.REQUEST_TO_STATEMENT
            )
        }
    }

    /**
     * Navigate to login screen
     *
     */
    fun navigateToLoginScreen() {
        activity?.also {
            it.startActivity(LoginActivity.newIntent(it))
            activity?.finish()
        }
    }

    /**
     * Navigate to NetworkInformationScreen
     *
     */
    fun navigateToNetworkInformationScreen() {
        activity?.also {
            it.startActivityForResult(NetworkStatusActivity.newIntent(it), 0)
        }
    }

    /**
     * Navigate to QRCode Scan
     *
     */
    fun navigateToQRCodeScan() {
        val bundle = DashboardCoordinatorDestinations.bundle
        activity?.also {
            it.startActivity(QrScanActivity.newIntent(it, bundle))
        }
    }

    /**
     * Navigate to Usage Details Screen.
     *
     */
    fun navigateToUsageDetailsActivity() {
        val bundle = DevicesCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                UsageDetailsActivity.newIntent(it, bundle),
                REQUEST_TO_DEVICES
            )
        }
    }

    /**
     * Navigate to appointment confirmation Screen.
     *
     */
    fun navigateToAppointmentConfirmation() {
        val bundle = ChangeAppointmentCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(
                AppointmentBookedActivity.newIntent(it, bundle),
                ChangeAppointmentActivity.REQUEST_TO_DASHBOARD
            )
        }
    }

    /**
     * Navigate to contactInfo Screen.
     *
     */
    fun navigateToContactInfo() {
        val bundle = AdditionalInfoCoordinatorDestinations.bundle
        activity?.also {
                it.startActivityForResult(ContactInfoActivity.newIntent(it, bundle),
                ContactInfoActivity.REQUEST_TO_HOME)
        }
    }

    /**
     * Navigate to selecttime Screen.
     *
     */
    fun navigateToSelectTime() {
        val bundle = ContactInfoCoordinatorDestinations.bundle
        activity?.also {
            it.startActivityForResult(SelectTimeActivity.newIntent(it, bundle),
            SelectTimeActivity.REQUEST_TO_HOME)
        }
    }

    private class ActivityObserver private constructor(
        private val activity: AppCompatActivity
    ) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroyed() {
            activity.lifecycle.removeObserver(this)
            observers -= this
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResumed() {
            observers -= this
            observers.add(0, this)
        }

        companion object {
            private val observers = mutableListOf<ActivityObserver>()

            val resumedActivity: AppCompatActivity?
                get() = observers
                    .firstOrNull()
                    ?.activity

            fun observe(activity: AppCompatActivity) =
                ActivityObserver(activity).also {
                    activity.lifecycle.addObserver(it)
                    observers.add(it)
                }
        }
    }
}
