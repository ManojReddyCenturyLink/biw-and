package com.centurylink.biwf.screens.support

import android.content.Context
import android.util.AttributeSet
import android.widget.ExpandableListView

class CustomExpandableListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ExpandableListView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST
        )
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom)
        val params = layoutParams
        params.height = measuredHeight
    }
}