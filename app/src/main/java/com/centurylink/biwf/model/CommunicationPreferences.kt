package com.centurylink.biwf.model

data class CommunicationPreferences(var biometricStatus: Boolean = false,
                                    var serviceCallsStatus: Boolean = false,
                                    var marketingEmailsStatus: Boolean = false,
                                    var marketingCallsAndTextStatus: Boolean = false)