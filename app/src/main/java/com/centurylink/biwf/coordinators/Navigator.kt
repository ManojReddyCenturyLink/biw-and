package com.centurylink.biwf.coordinators
import android.app.Activity
import com.centurylink.biwf.screens.forgotpassword.ForgotPasswordActivity
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.screens.learnmore.LearnMoreActivity
import com.centurylink.biwf.screens.notification.NotificationActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() {

    var activity: Activity? = null

    fun navigateToForgotPassword() {
        activity?.startActivity(ForgotPasswordActivity.newIntent(activity!!))
    }

    fun navigateToLearnMore() {
        activity?.startActivity(NotificationActivity.newIntent(activity!!))
    }

    fun navigateToHomeScreen() {
        activity?.startActivity(HomeActivity.newIntent(activity!!))
    }

    fun navigateToNotificationDetails() {
        //activity?.startActivity(NotificationDetailsActivity.newIntent(activity!!))
    }

    fun navigateToNotificationList() {
        activity?.startActivity(NotificationActivity.newIntent(activity!!))
    }

    fun closeActivity(){
        activity?.finish()
    }
}
