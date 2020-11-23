package com.centurylink.biwf.screens.home.dashboard.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.centurylink.biwf.R
import com.centurylink.biwf.databinding.LayoutScancodeItemBinding
import com.centurylink.biwf.databinding.LayoutScancodeItemNoSpeedTestBinding
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.screens.home.SpeedTestUtils
import com.centurylink.biwf.screens.qrcode.QrScanActivity
import com.google.zxing.EncodeHintType
import kotlinx.android.synthetic.main.layout_scancode_item.view.*
import kotlinx.android.synthetic.main.layout_scancode_item_no_speed_test.view.*
import net.glxn.qrgen.android.QRCode

class WifiDevicesAdapter(
    var wifiListItems: MutableList<WifiInfo>,
    private val wifiDeviceClickListener: WifiDeviceClickListener,
    var onlineStatus: Boolean
) : RecyclerView.Adapter<WifiDevicesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) {
            val itemBinding =
                LayoutScancodeItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ViewHolder(itemBinding)
        } else {
            val itemBinding =
                LayoutScancodeItemNoSpeedTestBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ViewHolder(itemBinding)
        }
    }

    override fun getItemCount(): Int {
        if (!wifiListItems.isNullOrEmpty()) {
            return wifiListItems.size
        }
        return 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (onlineStatus) { if (SpeedTestUtils.isSpeedTestAvailable()) 0 else 1 } else 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wifiDetails: WifiInfo = wifiListItems[position]
        holder.bindItems(wifiDetails, wifiDeviceClickListener, position)
    }

    inner class ViewHolder(bindView: ViewBinding) :
        RecyclerView.ViewHolder(bindView.root) {
        fun bindItems(
            wifiDetails: WifiInfo,
            wifiDeviceClickListener: WifiDeviceClickListener,
            pos: Int
        ) {
            if (onlineStatus) if (SpeedTestUtils.isSpeedTestAvailable()) {
                displayQRSpeedTestView(wifiDetails, pos, wifiDeviceClickListener)
            } else {
                displayNoQRSpeedTestView(wifiDetails, pos, wifiDeviceClickListener)
            } else displayNoQRSpeedTestView(wifiDetails, pos, wifiDeviceClickListener)
        }

        private fun displayQRSpeedTestView(
            wifiDetails: WifiInfo,
            pos: Int,
            wifiDeviceClickListener: WifiDeviceClickListener
        ) {
            itemView.devicename.text = getSpannableContent(
                getPrefixString(wifiDetails, itemView.context),
                wifiDetails.name!!
            )
            itemView.qrScan.setImageBitmap(getQRBitmap(wifiDetails, itemView.context))
            itemView.viewdivider.visibility =
                if (pos == wifiListItems.size - 1) View.INVISIBLE else View.VISIBLE

            if (wifiDetails.enabled!!) {
                itemView.iv_network_type.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_strong_signal))
            } else {
                itemView.iv_network_type.setImageDrawable(itemView.context.getDrawable(R.drawable.wifi_image_selector))
            }
            itemView.qrScan.setOnClickListener {
                wifiDeviceClickListener.onWifiQRScanImageClicked(wifiDetails)
            }
            itemView.devicename.setOnClickListener {
                wifiDeviceClickListener.onWifiNameClicked(wifiDetails.name ?: "")
            }

            itemView.view_full_screen.setOnClickListener {
                wifiDeviceClickListener.onWifiNameClicked(wifiDetails.name ?: "")
            }

            itemView.iv_network_type.setOnClickListener {
                wifiDeviceClickListener.onWifiNetworkStatusImageClicked(wifiDetails)
            }
        }

        private fun displayNoQRSpeedTestView(
            wifiDetails: WifiInfo,
            pos: Int,
            wifiDeviceClickListener: WifiDeviceClickListener
        ) {
            itemView.deviceName.text = getSpannableContent(
                itemView.context.getString(R.string.scan_to_join),
                wifiDetails.name!!
            )
            itemView.qrScanImg.setImageBitmap(getQRBitmap(wifiDetails, itemView.context))

            if (wifiDetails.enabled!!) {
                itemView.ivNetworkType.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_strong_signal))
            } else {
                itemView.ivNetworkType.setImageDrawable(itemView.context.getDrawable(R.drawable.wifi_image_selector))
            }
            if (pos == wifiListItems.size - 1) {
                itemView.tvNetworkInfo.text = itemView.context.getString(R.string.guest_network_information)
            } else {
                itemView.tvNetworkInfo.text = itemView.context.getString(R.string.network_information)
            }
            itemView.qrScanImg.setOnClickListener {
                wifiDeviceClickListener.onWifiQRScanImageClicked(wifiDetails)
            }
            itemView.deviceName.setOnClickListener {
                wifiDeviceClickListener.onWifiNameClicked(wifiDetails.name ?: "")
            }
            itemView.viewFullScreenQr.setOnClickListener {
                wifiDeviceClickListener.onWifiNameClicked(wifiDetails.name ?: "")
            }
            itemView.noSpeedTestQRView.setOnClickListener {
                wifiDeviceClickListener.onNavigateToNetworkInfo()
            }
            itemView.ivNetworkType.setOnClickListener {
                wifiDeviceClickListener.onWifiNetworkStatusImageClicked(wifiDetails)
            }
        }

        private fun getPrefixString(wifiDetails: WifiInfo, context: Context): String {
            return when (wifiDetails.type) {
                "guest" -> context.getString(R.string.scan_to_join_guest)
                else -> context.getString(R.string.scan_network)
            }
        }

        private fun getQRBitmap(wifiInfo: WifiInfo, context: Context): Bitmap {
            val wifi =
                context.resources.getString(R.string.wifi_code, wifiInfo.name, wifiInfo.password)
            return QRCode.from(wifi)
                .withColor(QrScanActivity.ON_COLOR_QR, QrScanActivity.OFF_COLOR_QR)
                .withHint(EncodeHintType.MARGIN, 0).bitmap()
        }

        private fun getSpannableContent(
            prefixContent: String,
            name: String
        ): SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            val prefixSpannable = SpannableString(prefixContent)
            val nameSpannable = SpannableString(name)
            val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            prefixSpannable.setSpan(StyleSpan(Typeface.NORMAL), 0, prefixContent.length, flag)
            nameSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, flag)
            builder.append(prefixSpannable)
            builder.append(" ")
            builder.append(nameSpannable)
            return builder
        }
    }

    fun QRCode.withColor(onColor: Long, offColor: Long): QRCode =
        this.withColor(onColor.toInt(), offColor.toInt())

    fun updateList(b: Boolean) {
        wifiListItems.forEach { it.enabled = b }
        onlineStatus = b
        notifyDataSetChanged()
    }
    interface WifiDeviceClickListener {
        fun onWifiQRScanImageClicked(wifidetails: WifiInfo)
        fun onWifiNameClicked(NetworkName: String)
        fun onWifiNetworkStatusImageClicked(wifidetails: WifiInfo)
        fun onNavigateToNetworkInfo()
    }
}
