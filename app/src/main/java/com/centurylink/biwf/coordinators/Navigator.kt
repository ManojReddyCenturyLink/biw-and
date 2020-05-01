package com.centurylink.biwf.coordinators

import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.centurylink.biwf.R
import com.centurylink.biwf.screens.changeappointment.ChangeAppointmentActivity
import com.centurylink.biwf.screens.emptydesitination.ProfileActivity
import com.centurylink.biwf.screens.forgotpassword.ForgotPasswordActivity
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.screens.learnmore.LearnMoreActivity
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.screens.subscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.subscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.FAQActivity
import com.centurylink.biwf.screens.support.SupportActivity
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackActivity
import com.centurylink.biwf.utility.WebLinkUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() : LifecycleObserver {

    private val activity: AppCompatActivity? get() = ActivityObserver.resumedActivity

    fun navigateToForgotPassword() {
        activity?.startActivity(ForgotPasswordActivity.newIntent(activity!!))
    }

    fun navigateToLearnMore() {
        activity?.startActivity(LearnMoreActivity.newIntent(activity!!))
    }

    fun navigateToHomeScreen(userType: Boolean) {
        activity?.startActivity(
            HomeActivity.newIntent(activity!!, bundleOf("EXISTING_USER" to userType))
        )
    }

    fun navigateToSupport() {
        activity?.startActivityForResult(SupportActivity.newIntent(activity!!),SupportActivity.REQUEST_TO_HOME)
    }

    fun navigateToChangeAppointment() {
        activity?.startActivity(ChangeAppointmentActivity.newIntent(activity!!))
    }

    fun navigateToNotificationList() {
        activity?.startActivity(NotificationActivity.newIntent(activity!!))
    }

    fun navigateToNotificationDetails() {
        val bundle = NotificationCoordinator.NotificationCoordinatorDestinations.get()
        activity?.startActivityForResult(
            NotificationDetailsActivity.newIntent(activity!!, bundle),
            NotificationDetailsActivity.REQUEST_TO_DISMISS
        )
    }

    fun navigateToFaq() {
        val bundle = SupportCoordinator.SupportCoordinatorDestinations.get()
        activity?.startActivityForResult(
            FAQActivity.newIntent(activity!!, bundle),
            FAQActivity.REQUEST_TO_HOME
        )
    }

    fun navigateToProfileActivity() {
        activity?.startActivity(ProfileActivity.newIntent(activity!!))
    }

    fun navigateToLiveChat() {}

    fun navigateToMangeSubscription() {
        activity?.startActivityForResult(
            CancelSubscriptionActivity.newIntent(activity!!),
            CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION
        )
    }

    fun navigateToScheduleCallback() {
        activity?.startActivityForResult(
            ScheduleCallbackActivity.newIntent(activity!!),
            ScheduleCallbackActivity.REQUEST_TO_HOME
        )
    }

    fun navigateToPhoneDialler() {
        WebLinkUtil.handleClick(activity!!.getString(R.string.tel), activity!!)
    }

    fun navigateToCancelSubscriptionDetails() {
        activity?.startActivityForResult(
            CancelSubscriptionDetailsActivity.newIntent(activity!!),
            CancelSubscriptionDetailsActivity.REQUEST_TO__CANCEL_SUBSCRIPTION
        )
    }

    class ActivityObserver private constructor(
        private val activity: AppCompatActivity
    ) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroyed() {
            activity.lifecycle.removeObserver(this)
            observers -= this
        }

        companion object {
            private val observers = mutableListOf<ActivityObserver>()
            val resumedActivity: AppCompatActivity?
                get() = observers
                    .firstOrNull { it.activity.lifecycle.currentState == Lifecycle.State.RESUMED }
                    ?.activity

            fun observe(activity: AppCompatActivity) =
                ActivityObserver(activity).also {
                    activity.lifecycle.addObserver(it)
                    observers += it
                }
        }
    }
}
