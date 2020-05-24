package com.centurylink.biwf.widgets

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.centurylink.biwf.R

class ChoiceDialogFragment : DialogFragment() {

    private lateinit var callback: BioMetricDialogCallback
    private lateinit var _context: Context
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positiveText: String
    private lateinit var negativeText: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as BioMetricDialogCallback
        _context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = arguments!!.getString(KEY_TITLE, "")
            message = arguments!!.getString(KEY_MESSAGE, "")
            positiveText = arguments!!.getString(KEY_POSITIVE, "")
            negativeText = arguments!!.getString(KEY_NEGATIVE, "")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = AlertDialog.Builder(_context)
            .setTitle(title)
            .setMessage(message)
        builder.setPositiveButton(positiveText) { _, _ ->
            callback.onOkBiometricResponse()
            dismiss()
        }
        builder.setNegativeButton(negativeText) { _, _ ->
            dismiss()
        }
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.blue, null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.font_color_medium_grey, null))
        }

        return dialog
    }

    interface BioMetricDialogCallback {
        fun onOkBiometricResponse()
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_POSITIVE = "positive"
        private const val KEY_NEGATIVE = "negative"

        operator fun invoke(
            title: String,
            message: String,
            positiveText: String,
            negativeText: String
        ): ChoiceDialogFragment {
            return ChoiceDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TITLE, title)
                    putString(KEY_MESSAGE, message)
                    putString(KEY_POSITIVE, positiveText)
                    putString(KEY_NEGATIVE, negativeText)
                }
            }
        }
    }
}