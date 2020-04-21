package com.centurylink.biwf.coordinators

import android.app.Activity
import androidx.core.os.bundleOf
import com.centurylink.biwf.screens.changeappointment.ChangeAppointmentActivity
import com.centurylink.biwf.screens.emptydesitination.ProfileActivity
import com.centurylink.biwf.screens.forgotpassword.ForgotPasswordActivity
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.screens.learnmore.LearnMoreActivity
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.screens.subscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.support.FAQActivity
import com.centurylink.biwf.screens.support.SupportActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() {

    var activity: Activity? = null

    fun navigateToForgotPassword() {
        activity?.startActivity(ForgotPasswordActivity.newIntent(activity!!))
    }

    fun navigateToLearnMore() {
        activity?.startActivity(LearnMoreActivity.newIntent(activity!!))
    }

    fun navigateToHomeScreen(userType: Boolean) {
        activity?.startActivity(HomeActivity.newIntent(activity!!, bundleOf("EXISTING_USER" to userType)))
    }

    fun navigateToSupport() {
        activity?.startActivity(SupportActivity.newIntent(activity!!))
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
}
