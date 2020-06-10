package com.centurylink.biwf.widgets

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.centurylink.biwf.R
import kotlinx.android.synthetic.main.widget_popup.view.*

class CustomDialogBlueTheme : DialogFragment() {

    lateinit var title: String
    lateinit var message: String
    var isErrorPopup: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = arguments!!.getString(KEY_TITLE, "")
            message = arguments!!.getString(KEY_MESSAGE, "")
            isErrorPopup = arguments!!.getBoolean(KEY_IS_ERROR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.widget_popup, container, false)
        rootView.popup_ok_button.setOnClickListener { dismiss() }
        rootView.popup_cancel_btn.setOnClickListener { dismiss() }
        rootView.popup_title.text = title
        rootView.popup_message.text = message
        if (isErrorPopup) {
            rootView.popup_cancel_btn.visibility = View.GONE
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        return rootView
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_IS_ERROR = "is-error"

        operator fun invoke(title: String, message: String, isErrorPopup: Boolean): CustomDialogBlueTheme {
            return CustomDialogBlueTheme().apply {
                arguments = Bundle().apply {
                    putString(KEY_TITLE, title)
                    putString(KEY_MESSAGE, message)
                    putBoolean(KEY_IS_ERROR, isErrorPopup)
                }
            }
        }
    }
}
