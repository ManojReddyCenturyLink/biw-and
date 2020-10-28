package com.centurylink.biwf.screens.home.account.subscription.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.utility.DateUtils
import kotlinx.android.synthetic.main.widget_recyclerview_list.view.*

/**
 * PaymentInvoicesAdapter Adapter used for the purpose of displaying the Payment Invoices list items in the UI
 */
class PaymentInvoicesAdapter(
    private val mContext: Context,
    private val itemClickListener: InvoiceClickListener,
    private var paymentListItems: PaymentList
) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(mContext).inflate(
                R.layout.widget_recyclerview_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return paymentListItems.totalSize
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item: PaymentList = paymentListItems
        val date = item.records.get(position).createdDate
        holder.header.text = DateUtils.formatInvoiceDate(date)
        holder.content.setOnClickListener { itemClickListener.onPaymentListItemClick(item.records[position]) }
    }
}

class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val header: TextView = view.rv_sub_heading
    val content: ConstraintLayout = view.rv_list_content
}

interface InvoiceClickListener {
    /**
     * Handle click event on Faq questions click
     */
    fun onPaymentListItemClick(item: RecordsItem)
}
