package com.braintreepayments.demo

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.ContainerStyle
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.PayPalSavedPaymentMethodViewStyle

/**
 * Demo-module-only stand-in for `component.PayPalSavedPaymentMethodView`. Renders a fake "Visa
 * ••1234" funding instrument row so the Enter Amount / App Switch / Edit Style scaffolding in
 * [SavedPaymentMethodFragment] can be wired and demoed before the real XML component (from
 * feature-paypalsavedpaymentmethod-xml-api-integration) is merged into this branch.
 *
 * TODO: this view is replaced by the real component.PayPalSavedPaymentMethodView.
 */
class DummyPayPalSavedPaymentMethodView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val logoView: ImageView
    private val labelView: TextView
    private val fundingInstrumentView: TextView
    private val editIconView: ImageView
    private val creditMessagingView: TextView

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.dummy_paypal_saved_payment_method_view, this, true)
        logoView = findViewById(R.id.dummy_paypal_saved_payment_method_logo)
        labelView = findViewById(R.id.dummy_paypal_saved_payment_method_label)
        fundingInstrumentView = findViewById(R.id.dummy_paypal_saved_payment_method_funding_instrument)
        editIconView = findViewById(R.id.dummy_paypal_saved_payment_method_edit_icon)
        creditMessagingView = findViewById(R.id.dummy_paypal_saved_payment_method_credit_messaging)
        applyStyle(PayPalSavedPaymentMethodViewStyle())
    }

    fun setOnSimulateResultClickListener(listener: () -> Unit) {
        fundingInstrumentView.setOnClickListener { listener() }
        editIconView.setOnClickListener { listener() }
    }

    fun applyStyle(style: PayPalSavedPaymentMethodViewStyle) {
        applyVisibility(style)
        applyTextAppearance(style)
        applyIconSizes(style.container)
        applyBackground(style)
        applyPaddingAndSize(style.container)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun applyVisibility(style: PayPalSavedPaymentMethodViewStyle) {
        logoView.visibility = if (style.showPayPalLogo) VISIBLE else GONE
        labelView.visibility = if (style.showPayPalLabel) VISIBLE else GONE
        creditMessagingView.visibility = if (style.showPayPalCreditMessaging) VISIBLE else GONE
    }

    private fun applyTextAppearance(style: PayPalSavedPaymentMethodViewStyle) {
        val appearance = style.componentAppearance
        val container = style.container

        val textColor = appearance?.textColor ?: DEFAULT_TEXT_COLOR
        labelView.setTextColor(textColor)
        fundingInstrumentView.setTextColor(textColor)
        creditMessagingView.setTextColor(container?.creditMessaging?.linkColor ?: textColor)

        val labelFontSizeSp = container?.label?.fontSizeSp ?: appearance?.baseFontSizeSp
        val fundingFontSizeSp = container?.fundingInstrument?.textFontSizeSp ?: appearance?.baseFontSizeSp
        val creditFontSizeSp = container?.creditMessaging?.fontSizeSp ?: appearance?.baseFontSizeSp
        labelFontSizeSp?.let { labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        fundingFontSizeSp?.let { fundingInstrumentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        creditFontSizeSp?.let { creditMessagingView.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
    }

    private fun applyIconSizes(container: ContainerStyle?) {
        container?.logo?.widthDp?.let { widthDp ->
            logoView.layoutParams = logoView.layoutParams.apply {
                width = widthDp.dpToPx(context)
            }
        }
        container?.fundingInstrument?.editIconSizeDp?.let { sizeDp ->
            editIconView.layoutParams = editIconView.layoutParams.apply {
                width = sizeDp.dpToPx(context)
                height = sizeDp.dpToPx(context)
            }
        }
    }

    private fun applyBackground(style: PayPalSavedPaymentMethodViewStyle) {
        val appearance = style.componentAppearance
        val container = style.container
        background = GradientDrawable().apply {
            setColor(appearance?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR)
            cornerRadius = (container?.cornerRadiusDp ?: DEFAULT_CORNER_RADIUS_DP).dpToPx(context).toFloat()
            container?.borderWidthDp?.let { borderWidthDp ->
                setStroke(borderWidthDp.dpToPx(context), container.borderColor ?: DEFAULT_BORDER_COLOR)
            }
        }
    }

    private fun applyPaddingAndSize(container: ContainerStyle?) {
        val horizontalPadding = (container?.horizontalPaddingDp ?: DEFAULT_HORIZONTAL_PADDING_DP).dpToPx(context)
        val verticalPadding = (container?.verticalPaddingDp ?: DEFAULT_VERTICAL_PADDING_DP).dpToPx(context)
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        container?.heightDp?.let {
            layoutParams = layoutParams.apply { height = it.dpToPx(context) }
        }
    }

    private fun Float.dpToPx(context: Context): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics).toInt()

    companion object {
        private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#F5F5F5")
        private val DEFAULT_TEXT_COLOR = Color.parseColor("#1A1A1A")
        private val DEFAULT_BORDER_COLOR = Color.parseColor("#DDDDDD")
        private const val DEFAULT_CORNER_RADIUS_DP = 8f
        private const val DEFAULT_HORIZONTAL_PADDING_DP = 16f
        private const val DEFAULT_VERTICAL_PADDING_DP = 12f
    }
}
