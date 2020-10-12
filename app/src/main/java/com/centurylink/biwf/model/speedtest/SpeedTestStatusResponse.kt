package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName


data class SpeedTestStatusResponse(@SerializedName("statusResponse")
                                   val statusResponse: SpeedTestStatus,
                                   @SerializedName("callBackUrl")
                                   val callBackUrl: String = "",
                                   @SerializedName("createErrorRecord")
                                   val createErrorRecord: Boolean = false,
                                   @SerializedName("requestId")
                                   val requestId: String = "",
                                   @SerializedName("success")
                                   val success: Boolean = false,
                                   @SerializedName("uploadSpeedSummary")
                                   val uploadSpeedSummary: UploadSpeedSummary,
                                   @SerializedName("downloadSpeedSummary")
                                   val downloadSpeedSummary: DownloadSpeedSummary,
                                   @SerializedName("assiaId")
                                   val assiaId: String = "",
                                   @SerializedName("message")
                                   val message: String = "",
                                   @SerializedName("uniqueErrorCode")
                                   val uniqueErrorCode: Int = 0,
                                   @SerializedName("status")
                                   val status: String = "")

data class SpeedTestNestedResults(@SerializedName("name")
                val name: String = "",
                                  @SerializedName("list")
                val list: List<SpeedTestResult>?)


data class SpeedTestResult(@SerializedName("average")
                    val average: Int = 0,
                    @SerializedName("std")
                    val std: Int = 0,
                    @SerializedName("detection")
                    val detection: Int = 0,
                    @SerializedName("numErrorFreeSamples")
                    val numErrorFreeSamples: Int = 0,
                    @SerializedName("sampleMaxPercentile")
                    val sampleMaxPercentile: Int = 0,
                    @SerializedName("latestSampleTimestamp")
                    val latestSampleTimestamp: Long = 0,
                    @SerializedName("sampleMax")
                    val sampleMax: Int = 0,
                    @SerializedName("videoQuality")
                    val videoQuality: Int = 0,
                    @SerializedName("serviceDetection")
                    val serviceDetection: Int = 0,
                    @SerializedName("percentile")
                    val percentile: List<Integer>?,
                    @SerializedName("latestSample")
                    val latestSample: Int = 0,
                    @SerializedName("primaryIp")
                    val primaryIp: String = "",
                    @SerializedName("timestamp")
                    val timestamp: String = "")

data class DownloadSpeedSummary(@SerializedName("code")
                                val code: Int = 0,
                                @SerializedName("data")
                                val speedTestNestedResults: SpeedTestNestedResults,
                                @SerializedName("message")
                                val message: String = "")

data class UploadSpeedSummary(@SerializedName("code")
                              val code: Int = 0,
                              @SerializedName("data")
                              val speedTestNestedResults: SpeedTestNestedResults,
                              @SerializedName("message")
                              val message: String = "")