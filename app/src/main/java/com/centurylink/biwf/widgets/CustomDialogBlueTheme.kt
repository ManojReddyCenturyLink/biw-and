package com.centurylink.biwf.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.centurylink.biwf.R
import kotlinx.android.synthetic.main.widget_popup.view.*

class CustomDialogBlueTheme : DialogFragment() {

    private lateinit var callback: ErrorDialogCallback
    lateinit var title: String
    lateinit var message: String
    lateinit var buttonText: String
    var isErrorPopup: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as ErrorDialogCallback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = arguments!!.getString(KEY_TITLE, "")
            message = arguments!!.getString(KEY_MESSAGE, "")
            buttonText = arguments!!.getString(KEY_BUTTON_TEXT, "")
            isErrorPopup = arguments!!.getBoolean(KEY_IS_ERROR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.widget_popup, container, false)
        rootView.popup_title.text = title
        rootView.popup_message.text = message
        rootView.popup_ok_button.text = buttonText
        rootView.popup_ok_button.setOnClickListener {
            dismiss()
            callback.onErrorDialogCallback(AlertDialog.BUTTON_POSITIVE)
        }
        rootView.popup_cancel_btn.setOnClickListener {
            dismiss()
            callback.onErrorDialogCallback(AlertDialog.BUTTON_NEGATIVE)
        }
        if (isErrorPopup) {
            rootView.popup_cancel_btn.visibility = View.GONE
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        return rootView
    }

    interface ErrorDialogCallback {
        fun onErrorDialogCallback(buttonType: Int)
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_BUTTON_TEXT = "button-text"
        private const val KEY_IS_ERROR = "is-error"

        operator fun invoke(
            title: String,
            message: String,
            buttonText: String,
            isErrorPopup: Boolean
        ): CustomDialogBlueTheme {
            return CustomDialogBlueTheme().apply {
                arguments = Bundle().apply {
                    putString(KEY_TITLE, title)
                    putString(KEY_MESSAGE, message)
                    putString(KEY_BUTTON_TEXT, buttonText)
                    putBoolean(KEY_IS_ERROR, isErrorPopup)
                }
            }
        }
    }
}
