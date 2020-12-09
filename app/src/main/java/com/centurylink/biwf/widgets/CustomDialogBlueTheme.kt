package com.centurylink.biwf.widgets

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.centurylink.biwf.BuildConfig
import com.centurylink.biwf.R
import com.centurylink.biwf.utility.AppUtil
import kotlinx.android.synthetic.main.widget_popup.view.popup_cancel_btn
import kotlinx.android.synthetic.main.widget_popup.view.popup_message
import kotlinx.android.synthetic.main.widget_popup.view.popup_neutral_button
import kotlinx.android.synthetic.main.widget_popup.view.popup_positive_button
import kotlinx.android.synthetic.main.widget_popup.view.popup_title

/**
 * Custom dialog blue theme class to create custom dialog
 *
 * @property callback - button type callback
 * @constructor Create empty Custom dialog blue theme
 */
open class CustomDialogBlueTheme(
    private val callback: (buttonType: Int) -> Unit
) : DialogFragment() {

    lateinit var title: String
    lateinit var message: String
    lateinit var buttonText: String
    var isErrorPopup: Boolean = false
    var linkTextToPhone: Boolean = false

    /**
     * On create - called to do initial creation of the fragment.
     *
     * @param savedInstanceState - Bundle: If the fragment is being re-created from a previous
     *                             saved state, this is the state. This value may be null
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = arguments!!.getString(KEY_TITLE, "")
            message = arguments!!.getString(KEY_MESSAGE, "")
            buttonText = arguments!!.getString(KEY_BUTTON_TEXT, "")
            isErrorPopup = arguments!!.getBoolean(KEY_IS_ERROR)
            linkTextToPhone = arguments!!.getBoolean(TEXT_LINK, false)
        }
    }

    /**
     * On create view - Called to have the fragment instantiate its user interface view
     *
     * @param inflater - LayoutInflater: The LayoutInflater object that can be used to inflate
     *                   any views in the fragment
     * @param container - ViewGroup: If non-null, this is the parent view that the fragment's
     *                    UI should be attached to. The fragment should not add the view itself,
     *                    but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed from
     *                             a previous saved state as given here.
     * @return - Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.widget_popup, container, false)
        rootView.popup_title.text = title
        if (linkTextToPhone) {
            val string = SpannableString(message)
            string.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.purple)),
                190,
                202,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            rootView.popup_message.text = string
            rootView.popup_message.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${BuildConfig.MOBILE_NUMBER}")
                startActivity(intent)
            }
        } else {
            rootView.popup_message.text = message
        }
        rootView.popup_cancel_btn.setOnClickListener {
            AppUtil.rebootStatus = false
            AppUtil.rebootOnGoingStatus = false
            dismiss()
            callback(AlertDialog.BUTTON_NEGATIVE)
        }
        if (isErrorPopup) {
            rootView.popup_positive_button.text = buttonText
            rootView.popup_neutral_button.visibility = View.GONE
            rootView.popup_positive_button.setOnClickListener {
                AppUtil.rebootStatus = false
                AppUtil.rebootOnGoingStatus = false
                dismiss()
                callback(AlertDialog.BUTTON_POSITIVE)
            }
        } else {
            rootView.popup_neutral_button.text = buttonText
            rootView.popup_positive_button.visibility = View.GONE
            rootView.popup_neutral_button.setOnClickListener {
                AppUtil.rebootStatus = false
                AppUtil.rebootOnGoingStatus = false
                dismiss()
            }
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        return rootView
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_BUTTON_TEXT = "button-text"
        private const val KEY_IS_ERROR = "is-error"
        private const val TEXT_LINK = "text-link"

        operator fun invoke(
            title: String,
            message: String,
            buttonText: String,
            isErrorPopup: Boolean,
            callback: (buttonType: Int) -> Unit,
            textLink: Boolean = false
        ): CustomDialogBlueTheme {
            return CustomDialogBlueTheme(callback).apply {
                arguments = Bundle().apply {
                    putString(KEY_TITLE, title)
                    putString(KEY_MESSAGE, message)
                    putString(KEY_BUTTON_TEXT, buttonText)
                    putBoolean(KEY_IS_ERROR, isErrorPopup)
                    putBoolean(TEXT_LINK, textLink)
                }
            }
        }
    }
}
