package com.centurylink.biwf.screens.support.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.centurylink.biwf.R
import com.centurylink.biwf.utility.WebLinkUtil

class ExpandableContentAdapter(private val answerList: HashMap<String, String>) :
    BaseExpandableListAdapter() {

    val questionList = ArrayList(answerList.keys)

    override fun getChild(listPosition: Int, expandedListPosition: Int): String? {
        val key = questionList[listPosition]
        return this.answerList[key]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return questionList.size
    }

    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        if (convertView == null) {
            val layoutInflater =
                parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.faq_item_answers, null)
        }
        val expandedListWebView = convertView!!.findViewById<WebView>(R.id.faq_answers)
        WebLinkUtil.setupWebView(false, expandedListWebView, true)
        expandedListWebView.loadDataWithBaseURL(null, expandedListText, "text/html", "UTF-8", null)
        return convertView
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition)
        if (convertView == null) {
            val layoutInflater =
                parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.faq_header_questions, null)
        }
        val questionTextView = convertView!!.findViewById<TextView>(R.id.faq_header_group_title)
        val questionIcon = convertView.findViewById<ImageView>(R.id.faq_header_arrow)
        val dividerView = convertView.findViewById<View>(R.id.faq_dividers_view)
        questionTextView.text = listTitle
        if (isExpanded) {
            questionIcon.setImageResource(R.drawable.ic_faq_down)
            dividerView.visibility = View.INVISIBLE
        } else {
            questionIcon.setImageResource(R.drawable.ic_icon_right)
            dividerView.visibility = View.VISIBLE
        }
        DrawableCompat.setTint(questionIcon.drawable,
            ContextCompat.getColor(parent?.context!!, R.color.purple))
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroup(groupPosition: Int): String {
        return questionList[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }
}
