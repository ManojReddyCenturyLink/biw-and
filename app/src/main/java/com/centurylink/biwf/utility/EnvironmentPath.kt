package com.centurylink.biwf.utility

import com.centurylink.biwf.BuildConfig

class EnvironmentPath {
    companion object {

        fun getSalesForceBaseURl(): String {
            return "https://" + BuildConfig.SALESFORCE_URL
        }

        fun getBaseSubscriptionUrl(): String {
            return "https://" + BuildConfig.SALESFORCE_URL + "/" + getCommunityName() + "/apex/vf_fiberBuyFlowPaymentMobile?userId="
        }

        private fun getCommunityName(): String {
            return BuildConfig.COMMUNITY_NAME
        }

        private fun getSalesForceURl(): String {
            return getSalesForceBaseURl() + "/" + getCommunityName() + "/services/data"
        }

        fun getSalesForceVersionURl(): String {
            return getSalesForceURl() + "/" + BuildConfig.SALES_FORCE_VERSION + "/"
        }

        private fun getApigeeBaseUrl(): String {
            return "https://" + BuildConfig.APIGEE_URL
        }

        fun getApigeeVersionUrl(): String {
            return "https://" + BuildConfig.APIGEE_URL + "/" + "v1/"
        }

        fun geApigeeCloudCheckURl(): String {
            return getApigeeVersionUrl() + "cloudcheck/"
        }

        fun getClientId(): String {
            return BuildConfig.CLIENT_ID
        }

        fun getAuthorizationEndpoint(): String {
            return getApigeeBaseUrl() + "/v1/mobile/auth"
        }

        fun getTokenEndpoint(): String {
            return getApigeeBaseUrl() + "/v1/mobile/salesforce-access-token"
        }

        fun getRevokeTokenEndpoint(): String {
            return getApigeeBaseUrl() + "/v1/mobile/revoke"
        }

        /**
         * OAuth 2 redirection URI for the client.
         */
        const val REDIRECT_URI = "myapp://com.force.lightning.ctl-fiber--qa"
        const val API_UPDATE_PASSWORD_PATH = "sobjects/User/{user-id}/password"
        const val API_USER_DETAILS_PATH = "sobjects/User"
        const val API_USER_ID_PATH = "sobjects/User/{user-id}"
        const val API_CONTACT_INFORMATION_PATH = "sobjects/Contact/{contact-id}"
        const val INVOICE_LIST_QUERY =
            " SELECT Id, Zuora__Invoice__c, CreatedDate FROM Zuora__Payment__c WHERE Zuora__Account__c ='%s'"
        const val SUBSCRIPTION_DATES_QUERY =
            "SELECT Id, Name, Zuora__SubscriptionStartDate__c, Zuora__SubscriptionEndDate__c, Zuora__NextRenewalDate__c, Zuora__NextChargeDate__c FROM Zuora__Subscription__c WHERE Zuora__Account__c='%s'"
        const val RECORD_TYPE_ID_QUERY =
            "SELECT Id FROM RecordType WHERE SobjectType = 'Case' AND DeveloperName ='Fiber'"
        const val KNOWLEDGE_RECORD_TYPE_ID_QUERY =
            "SELECT Id FROM RecordType WHERE SobjectType = 'Knowledge__kav' AND DeveloperName ='Fiber'"
        const val FAQ_QUESTION_DETAILS_QUERY =
            "SELECT ArticleNumber, ArticleTotalViewCount, Article_Content__c, Article_Url__c, Id, Language, Section__c, Title FROM Knowledge__kav WHERE IsDeleted=false AND PublishStatus='Online' AND ValidationStatus='Validated'AND RecordTypeId='%s'"
        const val LIVE_CARD_DETAILS_QUERY =
            "SELECT Credit_Card_Summary__c,Id,Name,Next_Renewal_Date__c,Zuora__BillCycleDay__c FROM Zuora__CustomerAccount__c WHERE Zuora__Account__c = '%s'"

        // Appointment
        const val APPOINTMENT_INFO_QUERY =
            "SELECT Id, ArrivalWindowEndTime, ArrivalWindowStartTime, Status, Job_Type__c, WorkTypeId, Latitude, Longitude,CreatedDate, ServiceTerritory.OperatingHours.TimeZone,Appointment_Number_Text__c,(SELECT ServiceResource.Id, ServiceResource.Name FROM ServiceAppointment.ServiceResources) FROM ServiceAppointment WHERE AccountId = '%s' ORDER BY CreatedDate DESC"

        const val API_APPOINTMENT_SLOT_PATH =
            "/services/apexrest/AppointmentSlotsMobile/"
        const val API_RESCHEDULE_APPOINTMENT_PATH = "/services/apexrest/AppointmentSlotsMobile/"
        const val API_CANCEL_APPOINTMENT_PATH =
            "/" + BuildConfig.COMMUNITY_NAME + "/services/apexrest/CancelServiceAppointmentMobile"

        // Assia
        const val API_ASIA_ACCESSTOKEN_PATH =
            "oauth/token?username=biwftest&password=BiwfTest1&client_id=spapi&client_secret=oBj2xZc&grant_type=password"
        const val ASSIA_BASE_URL = "https://ctlink-biwf-staging.cloudcheck.net:443/cloudcheck-sp/"
        const val API_LINE_INFO_PATH = "wifi-line-info"
        const val API_WIFI_OPERATIONS_ENABLE = "wifi-operations-enable"
        const val API_WIFI_OPERATIONS_DISABLE = "wifi-operations-disable"
        const val API_ACCOUNT_DETAILS_PATH = "sobjects/Account/{account-id}"
        const val ACCOUNT_ID = "account-id"
        const val CONTACT_ID = "contact-id"
        const val USER_ID = "user-id"
        const val INVOICE_ID = "invoice-id"
        const val GENERIC_ID = "genericId"
        const val FORCE_PING = "forcePing"
        const val WIFI_DEVICE_ID = "wifiDeviceId"
        const val INTERFACE_VALUE = "interface"
        const val SERVICE_APPOINTMENT_ID = "ServiceAppointmentId"
        const val EARLIEST_PERMITTED_DATE = "EarliestPermittedDate"
        const val ASSIA_ID = "assiaId"
        const val LINE_ID = "lineId"
        const val STATION_MAC_ADDRESS = "stationMacAddress"
        const val SALES_FORCE_QUERY = "query"
        const val SALES_FORCE_QUERY_VALUE = "q"
        const val AASIA_ID_TRAFFIC = "assiaIdTraffic"
        const val START_DATE = "startDateTraffic"
        const val STAT_MAC = "staMacTraffic"
        const val STATION_INFO = "station-info"
        const val RECORD_TYPE_ID = "recordTypeId"
        const val SALES_FORCE_QUERY_SLASH = "query" + "/"
        const val API_PAYMENT_DETAILS_PATH = "sobjects/Zuora__Payment__c/{invoice-id}"
        const val API_SUBSCRIPTION_DETAILS_PATH = "sobjects/Zuora__Subscription__c/{account-id}"
        const val API_SERVICE_APPOINTMENTS_PATH = "sobjects/ServiceAppointment/{account-id}"
        const val API_MODEM_INFO_PATH = "api/v3/wifi/line/info"
        const val API_DEVICE_LIST_PATH = "api/v2/wifi/diags/stationinfo"
        const val API_USAGE_INFO_PATH = "station-traffic"
        const val API_SPEED_TEST_PATH = "speed-test-request"
        const val API_SPEED_TEST_STATUS = "speed-test-status"
        const val API_REBOOT_MODEM_PATH = "reboot"
        const val API_BLOCK_UNBLOCK_DEVICE_PATH =
            "api/v2/wifi/operations/station/{assiaId}/{stationMacAddress}/block"
        const val API_BLOCK_DEVICE_PATH = "block"
        const val API_CASE_FOR_SUBSCRIPTION_PATH = "sobjects/Case"
        const val API_SUPPORT_SERVICES_PATH =
            "phish/services/apexrest/ServiceAndSupportAPI"
        const val API_SCHEDULE_CALLBACK_PATH =
            "phish/services/data/v42.0/ui-api/object-info/Case/picklist-values/{recordTypeId}/What_kind_of_customer_care_do_you_need__c"

        // McAfee
        const val API_GET_NETWORK_ACCESS_PATH = "mcafee/get-network-access"
        const val API_UPDATE_NETWORK_ACCESS_PATH = "mcafee/network-access"
        const val API_DEVICES_MAPPING_PATH = "mcafee/macaddress/mapping"
        const val API_UPDATE_DEVICE_INFO_PATH = "mcafee/update-device"
        const val API_GET_DEVICE_INFO_PATH = "mcafee/get-device"

        // Regular and Guest Wifi network
        const val API_ENABLE_REGULAR_GUEST_WIFI_PATH =
            "api/v2/wifi/operations/enableintf/{wifiDeviceId}/{interface}"
        const val API_DISABLE_REGULAR_GUEST_WIFI_PATH =
            "api/v2/wifi/operations/disableintf/{wifiDeviceId}/{interface}"

        // Get and post/change SSID //same url
        const val API_GET_POST_SSID_PATH = "api/v2/wifi/operations/ssid/{wifiDeviceId}/{interface}"

        const val API_CHANGE_SSID = "change-ssid"
        const val API_CHANGE_PASSWORD = "change-wifi-pwd?"
        const val API_GET_PASSWORD = "get-wifi-pwd?"

        // Get and post/change network password //same url
        const val API_GET_CHANGE_NETWORK_PASSWORD_PATH =
            "api/v2/wifi/operations/wifipwd/{wifiDeviceId}/{interface}"
        const val SCOPE = "web api refresh_token"
        const val AWS_BASE_URL = "https://bucketforapi.s3-eu-west-1.amazonaws.com/"
        const val BILLING_DETAILS = "invoice1.json"
        const val CALL_BACK_URL = "ctl-fiber--qa.my.salesforce.com/services/apexrest/SpeedTest/*"
        const val MODEM_ID_QUERY =
            "SELECT Modem_Number__c FROM WorkOrder WHERE AccountId='%s' AND Job_Type__c='Fiber Install - For Installations'"

        const val APIGEE_MOBILE_HEADER = "From: mobile"
    }
}
