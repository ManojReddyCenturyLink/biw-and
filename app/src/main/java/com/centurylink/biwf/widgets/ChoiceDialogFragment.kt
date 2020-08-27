package com.centurylink.biwf.widgets

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.centurylink.biwf.R

class ChoiceDialogFragment : DialogFragment() {

    private lateinit var callback: BioMetricDialogCallback
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positiveText: String
    private lateinit var negativeText: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as BioMetricDialogCallback
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
        val builder = AlertDialog.Builder(requireActivity(), R.style.choiceDialog)
            .setTitle(title)
            .setMessage(message)
        builder.setPositiveButton(positiveText) { _, _ ->
            callback.onOkBiometricResponse()
            dismiss()
        }
        builder.setNegativeButton(negativeText) { _, _ ->
            callback.onCancelBiometricResponse()
            dismiss() }
        return builder.create()
    }

    interface BioMetricDialogCallback {
        fun onOkBiometricResponse()
        fun onCancelBiometricResponse()
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