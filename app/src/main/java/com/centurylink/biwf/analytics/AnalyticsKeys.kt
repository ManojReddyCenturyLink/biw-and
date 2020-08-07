package com.centurylink.biwf.analytics

class AnalyticsKeys {
    companion object {

        /*Event Tags*/
        const val EVENT_TYPE_SCREEN_LAUNCH = "screen_launch"
        const val EVENT_TYPE_BUTTON_CLICKED = "button_clicked"
        const val EVENT_TYPE_CARD_SELECTED = "card_selected"
        const val EVENT_TYPE_API_CALL = "api_call"
        const val EVENT_TYPE_TOGGLE_STATE_CHANGED = "toggle_state_changed"
        const val EVENT_BIOMETRICS_LOGIN = "biometrics_login"

        /*Screens*/
        const val SCREEN_CANCEL_SUBSCRIPTION_DETAILS = "Cancel Subscription Details Screen"
        const val SCREEN_CANCEL_SUBSCRIPTION = "Cancel Subscription Screen"
        const val SCREEN_SUBSCRIPTION = "Subscription Screen"
        const val SCREEN_PERSONAL_INFO = "Personal Info Screen"
        const val SCREEN_ACCOUNTS = "Accounts Screen"
        const val SCREEN_SUBSCRIPTION_STATEMENT = "Statement Statement Screen"
        const val SCREEN_EDIT_PAYMENT_DETAILS = "Edit Payment Details Screen"

        /*Buttons*/
        const val BUTTON_CONTINUE_CANCELLATION = "Cancel Subscription Button"
        const val BUTTON_LOG_OUT = "Logout Button"
        const val BUTTON_DONE_PERSONAL_INFO = "Done Button Personal Info"
        const val BUTTON_DONE_SUBSCRIPTION_SCREEN = "Done Button Subscription"
        const val BUTTON_DONE_EDIT_PAYMENT_DETAILS = "Done Button Edit Payment Details"
        const val BUTTON_BACK_EDIT_PAYMENT_DETAILS = "Back Button Edit Payment Details"
        const val BUTTON_DONE_PREVIOUS_STATEMENT = "Done Button Previous Statements"
        const val BUTTON_BACK_PREVIOUS_STATEMENT = "Back Button Previous Statements"
        const val BUTTON_CANCEL_CANCEL_SUBSCRIPTION = "Cancel Button Cancel Subscription"
        const val BUTTON_BACK_CANCEL_SUBSCRIPTION = "Back Button Cancel Subscription"
        const val BUTTON_BACK_CANCEL_SUBSCRIPTION_CONFIRMATION = "Back Button Cancel Subscription Confirmation"
        const val BUTTON_MANAGE_SUBSCRIPTION = "Manage Subscription Button"
        const val BUTTON_PREVIOUS_STATEMENT = "Previous Statements Button"
        const val BUTTON_EDIT_BILLING_INFO = "Edit Billing Info Button"
        const val BUTTON_SUBMIT_CANCEL_SUBSCRIPTION_CONFIRMATION = "Submit Button Cancel Subscription Confirmation"

        /*Alert Dialog*/
        const val ALERT_UPDATE_EMAIL_INFO = "Change Email Info Popup"
        const val ALERT_CANCEL_SUBSCRIPTION_KEEP_SERVICE = "Keep Service Cancel Subscription Confirmation"
        const val ALERT_CANCEL_SUBSCRIPTION_CANCEL_SERVICE = "Cancel Service Cancel Subscription Confirmation"

        /*Card View*/
        const val CARD_SUBSCRIPTION_INFO = "Subscription Info"
        const val CARD_PERSONAL_INFO = "Personal Info"

        /*Toggle*/
        const val TOGGLE_BIOMETRIC = "Biometric"
        const val TOGGLE_MARKETING_CALLS_AND_TEXT = "Marketing calls & texts"
        const val TOGGLE_SERVICE_CALLS_AND_TEXT = "Service calls & texts"
        const val TOGGLE_MARKETING_EMAILS = "Marketing Emails"

        /*API Calls*/
        const val GET_ACCOUNT_DETAILS_SUCCESS = "Account Details Api Success"
        const val GET_ACCOUNT_DETAILS_FAILURE = "Account Details Api Failure"
        const val GET_CONTACT_DETAILS_SUCCESS = "Contact Api Success"
        const val GET_CONTACT_DETAILS_FAILURE = "Contact Api Failure"
        const val GET_INVOICES_LIST_SUCCESS = "Invoices List Api Success"
        const val GET_INVOICES_LIST_FAILURE = "Invoices List  Api Failure"
        const val GET_LIVE_CARD_INFO_SUCCESS = "Live Card Info Api Success"
        const val GET_LIVE_CARD_INFO_FAILURE = "Live Card Info Api Failure"
        const val LOG_OUT_SUCCESS = "Logout Success"
        const val LOG_OUT_FAILURE = "Logout  Failure"
        const val RESET_PASSWORD_SUCCESS = "Reset Password Api Success"
        const val RESET_PASSWORD_FAILURE = "Reset Password Api  Failure"
        const val RECORD_TYPE_ID_SUCCESS = "Record Type Id Api Success"
        const val RECORD_TYPE_ID_FAILURE = "Record Type Id Api  Failure"
        const val GET_USER_DETAILS_SUCCESS = "User Details Api Success"
        const val GET_USER_DETAILS_FAILURE = "User Details Api Failure"
        const val GET_SUBSCRIPTION_DATE_SUCCESS = "Subscription Date Api Success"
        const val GET_SUBSCRIPTION_DATE_FAILURE = "Subscription Date Api Failure"
        const val GET_PAYMENT_INFO_SUCCESS = "Payment Info Api Success"
        const val GET_PAYMENT_INFO_FAILURE = "Payment Info Api Failure"
        const val POST_CASE_FOR_SUBSCRIPTION_SUCCESS = "Submit Case For Subscription Success"
        const val POST_CASE_FOR_SUBSCRIPTION_FAILURE = "Submit Case For Subscription Failure"
    }
}