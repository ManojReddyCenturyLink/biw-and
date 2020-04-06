package com.centurylink.biwf.coordinators

import android.app.Activity
import com.centurylink.biwf.screens.changeappointment.ChangeAppointmentActivity
import com.centurylink.biwf.screens.forgotpassword.ForgotPasswordActivity
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.screens.learnmore.LearnMoreActivity
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
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

    fun navigateToHomeScreen() {
        activity?.startActivity(HomeActivity.newIntent(activity!!))
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
         val bundle  = NotificationCoordinator.NotificationCoordinatorDestinations.get()
         activity?.startActivityForResult(NotificationDetailsActivity.newIntent(activity!!,bundle),
             NotificationDetailsActivity.requesttodismiss)
    }
}
