package com.centurylink.biwf.widgets

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.centurylink.biwf.R

/**
 * Choice dialog fragment -  fragment class to handle biometric login
 *
 * @constructor Create empty Choice dialog fragment
 */
class ChoiceDialogFragment : DialogFragment() {

    private lateinit var callback: BioMetricDialogCallback
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positiveText: String
    private lateinit var negativeText: String

    /**
     * On attach - called once the fragment is associated with its activity.
     *
     * @param context - instance activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as BioMetricDialogCallback
    }

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
            positiveText = arguments!!.getString(KEY_POSITIVE, "")
            negativeText = arguments!!.getString(KEY_NEGATIVE, "")
        }
    }

    /**
     * On create dialog - Override to build your own custom Dialog container.
     *
     * @param savedInstanceState - Bundle: The last saved instance state of the Fragment, or null
     *                             if this is a freshly created Fragment.
     * @return - Return a new Dialog instance to be displayed by the Fragment.
     */
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

    /**
     * Bio metric dialog callback interface
     *
     * @constructor Create empty Bio metric dialog callback
     */
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
