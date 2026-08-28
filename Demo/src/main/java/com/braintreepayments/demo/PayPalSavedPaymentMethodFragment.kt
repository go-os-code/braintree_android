package com.braintreepayments.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.PayPalSavedPaymentMethodViewStyle

/**
 * Demo screen for `PayPalSavedPaymentMethod`, XML path. Renders [DummyPayPalSavedPaymentMethodView]
 * in place of the real `component.PayPalSavedPaymentMethodView` until the real component is
 * merged into this branch (see feature-paypalsavedpaymentmethod-xml-api-integration). Tapping the
 * dummy view simulates a launch result so the nonce section / Clear button can be demoed.
 *
 * The Edit Style button opens the shared [PayPalSavedPaymentMethodStyleBottomSheet].
 */
class PayPalSavedPaymentMethodFragment : BaseFragment() {

    private lateinit var amountEditText: EditText
    private lateinit var appSwitchToggle: Switch
    private lateinit var savedPaymentMethodView: DummyPayPalSavedPaymentMethodView
    private lateinit var nonceSection: View
    private lateinit var nonceText: TextView
    private var currentStyle: PayPalSavedPaymentMethodViewStyle = PayPalSavedPaymentMethodViewStyle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_paypal_saved_payment_method, container, false)

        amountEditText = view.findViewById(R.id.paypal_saved_payment_method_amount_edit_text)
        appSwitchToggle = view.findViewById(R.id.paypal_saved_payment_method_app_switch)
        savedPaymentMethodView = view.findViewById(R.id.paypal_saved_payment_method_view)
        nonceSection = view.findViewById(R.id.paypal_saved_payment_method_nonce_section)
        nonceText = view.findViewById(R.id.paypal_saved_payment_method_nonce_text)

        childFragmentManager.setFragmentResultListener(
            PayPalSavedPaymentMethodStyleBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            currentStyle = PayPalSavedPaymentMethodStyleBottomSheet.styleFromBundle(bundle)
            savedPaymentMethodView.applyStyle(currentStyle)
        }

        view.findViewById<ImageButton>(R.id.paypal_saved_payment_method_edit_style_button).setOnClickListener {
            openStyleBottomSheet()
        }
        view.findViewById<Button>(R.id.paypal_saved_payment_method_nonce_clear_button).setOnClickListener {
            nonceSection.visibility = View.GONE
        }
        savedPaymentMethodView.setOnSimulateResultClickListener { simulateResult() }

        return view
    }

    private fun openStyleBottomSheet() {
        PayPalSavedPaymentMethodStyleBottomSheet.newInstance(currentStyle).show(
            childFragmentManager,
            PayPalSavedPaymentMethodStyleBottomSheet.TAG
        )
    }

    // TODO: this moves to the real initialize()/handleReturnToApp() flow once the client is merged.
    private fun simulateResult() {
        val amount = amountEditText.text?.toString().orEmpty()
        val appSwitchEnabled = appSwitchToggle.isChecked
        nonceText.text = getString(
            R.string.paypal_saved_payment_method_nonce_placeholder,
            "fake-nonce-$amount-appSwitch=$appSwitchEnabled"
        )
        nonceSection.visibility = View.VISIBLE
    }
}
