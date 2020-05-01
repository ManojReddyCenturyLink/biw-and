package com.centurylink.biwf.coordinators

import androidx.appcompat.app.AppCompatActivity
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

    fun observe(activity: AppCompatActivity) {
        ActivityObserver.observe(activity)
    }

    fun navigateToForgotPassword() {
        activity?.also {
            it.startActivity(ForgotPasswordActivity.newIntent(it))
        }
    }

    fun navigateToLearnMore() {
        activity?.also {
            it.startActivity(LearnMoreActivity.newIntent(it))
        }
    }

    fun navigateToHomeScreen(userType: Boolean) {
        activity?.also {
            it.startActivity(HomeActivity.newIntent(it, userType))
        }
    }

    fun navigateToSupport() {
        activity?.also {
            it.startActivityForResult(
                SupportActivity.newIntent(it),
                SupportActivity.REQUEST_TO_HOME
            )
        }
    }

    fun navigateToChangeAppointment() {
        activity?.also {
            it.startActivity(ChangeAppointmentActivity.newIntent(it))
        }
    }

    fun navigateToNotificationList() {
        activity?.also {
            it.startActivity(NotificationActivity.newIntent(it))
        }
    }

    fun navigateToNotificationDetails() {
        activity?.also {
            it.startActivityForResult(
                NotificationDetailsActivity.newIntent(
                    it,
                    NotificationCoordinator.NotificationCoordinatorDestinations.bundle
                ),
                NotificationDetailsActivity.REQUEST_TO_DISMISS
            )
        }
    }

    fun navigateToFaq() {
        activity?.also {
            it.startActivityForResult(
                FAQActivity.newIntent(it, SupportCoordinator.SupportCoordinatorDestinations.bundle),
                FAQActivity.REQUEST_TO_HOME
            )
        }
    }

    fun navigateToProfileActivity() {
        activity?.also {
            it.startActivity(ProfileActivity.newIntent(it))
        }
    }

    fun navigateToLiveChat() {}

    fun navigateToMangeSubscription() {
        activity?.also {
            it.startActivityForResult(
                CancelSubscriptionActivity.newIntent(it),
                CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION
            )
        }
    }

    fun navigateToScheduleCallback() {
        activity?.also {
            it.startActivityForResult(
                ScheduleCallbackActivity.newIntent(it),
                ScheduleCallbackActivity.REQUEST_TO_HOME
            )
        }
    }

    fun navigateToPhoneDialler() {
        WebLinkUtil.handleClick(activity!!.getString(R.string.tel), activity!!)
    }

    fun navigateToCancelSubscriptionDetails() {
        activity?.also {
            it.startActivityForResult(
                CancelSubscriptionActivity.newIntent(it),
                CancelSubscriptionDetailsActivity.REQUEST_TO__CANCEL_SUBSCRIPTION
            )
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
