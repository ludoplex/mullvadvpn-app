package net.mullvad.mullvadvpn.ui

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CompletableDeferred
import net.mullvad.mullvadvpn.R

class ConfirmDnsDialogFragment @JvmOverloads constructor(
    private val message: Int = R.string.confirm_local_dns,
    private var confirmation: CompletableDeferred<Boolean>? = null
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.confirm_dns, container, false)

        view.findViewById<TextView>(R.id.message).setText(message)

        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            activity?.onBackPressed()
        }

        view.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            confirmation?.complete(true)
            confirmation = null
            dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.window?.setBackgroundDrawable(ColorDrawable(android.R.color.transparent))

        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        if (confirmation == null) {
            dismiss()
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        confirmation?.complete(false)
    }

    override fun onDestroy() {
        confirmation?.cancel()

        super.onDestroy()
    }
}
