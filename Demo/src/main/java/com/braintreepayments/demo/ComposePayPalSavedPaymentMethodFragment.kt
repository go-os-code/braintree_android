package com.braintreepayments.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.PayPalSavedPaymentMethodViewStyle

/**
 * Compose entry point for `PayPalSavedPaymentMethod`, mirroring [PayPalSavedPaymentMethodFragment]
 * (XML path) in shape: Enter Amount, App Switch, Edit Style, a Compose-native dummy placeholder
 * standing in for `compose.PayPalSavedPaymentMethodView`, and a nonce result section with Clear.
 *
 * The Edit Style button opens the shared [PayPalSavedPaymentMethodStyleBottomSheet].
 */
class ComposePayPalSavedPaymentMethodFragment : BaseFragment() {

    private val styleState = mutableStateOf(PayPalSavedPaymentMethodViewStyle())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            PayPalSavedPaymentMethodStyleBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            styleState.value = PayPalSavedPaymentMethodStyleBottomSheet.styleFromBundle(bundle)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                var amount by remember { mutableStateOf("10.00") }
                var appSwitchEnabled by remember { mutableStateOf(false) }
                var nonce by remember { mutableStateOf<String?>(null) }
                val style by styleState

                val simulateResult: () -> Unit = {
                    nonce = "fake-nonce-${amount}-appSwitch=$appSwitchEnabled"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.paypal_saved_payment_method_screen_background))
                        .padding(16.dp)
                ) {
                    PayPalSavedPaymentMethodRow {
                        Text(
                            text = stringResource(R.string.paypal_saved_payment_method_amount_label),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = colorResource(R.color.paypal_saved_payment_method_pill_background),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    PayPalSavedPaymentMethodRow {
                        Text(
                            text = stringResource(R.string.paypal_saved_payment_method_app_switch_label),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = appSwitchEnabled, onCheckedChange = { appSwitchEnabled = it })
                    }

                    PayPalSavedPaymentMethodRow {
                        Text(
                            text = stringResource(R.string.paypal_saved_payment_method_label),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = colorResource(R.color.paypal_saved_payment_method_pill_background),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    PayPalSavedPaymentMethodStyleBottomSheet.newInstance(style).show(
                                        childFragmentManager,
                                        PayPalSavedPaymentMethodStyleBottomSheet.TAG
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_edit_pencil),
                                    contentDescription = stringResource(
                                        R.string.paypal_saved_payment_method_edit_style_description
                                    )
                                )
                            }
                        }
                    }

                    DummyComposePayPalSavedPaymentMethodView(style = style, onClick = simulateResult)

                    if (nonce != null) {
                        PayPalSavedPaymentMethodRow(horizontal = false) {
                            Text(stringResource(R.string.paypal_saved_payment_method_nonce_placeholder, nonce!!))
                            Button(onClick = { nonce = null }) {
                                Text(stringResource(R.string.paypal_saved_payment_method_nonce_clear_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayPalSavedPaymentMethodRow(
    horizontal: Boolean = true,
    content: @Composable () -> Unit
) {
    Surface(
        color = colorResource(R.color.paypal_saved_payment_method_card_background),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        if (horizontal) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                content()
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

// Compose-only visual defaults, mirroring DummyPayPalSavedPaymentMethodView's XML defaults.
// TODO: this whole composable is replaced by the real compose.PayPalSavedPaymentMethodView.
private object DummyComposeDefaults {
    val BackgroundColor = Color(0xFFF5F5F5)
    val TextColor = Color(0xFF1A1A1A)
    val BorderColor = Color(0xFFDDDDDD)
    const val CORNER_RADIUS_DP = 8f
    const val HORIZONTAL_PADDING_DP = 16f
    const val VERTICAL_PADDING_DP = 12f
    const val LOGO_WIDTH_DP = 40f
    const val EDIT_ICON_SIZE_DP = 20f
    const val LABEL_MARGIN_START_DP = 8f
    const val FUNDING_MARGIN_START_DP = 8f
}

@Composable
private fun DummyComposePayPalSavedPaymentMethodView(
    style: PayPalSavedPaymentMethodViewStyle,
    onClick: () -> Unit
) {
    val appearance = style.componentAppearance
    val container = style.container

    val backgroundColor = appearance?.backgroundColor?.let { Color(it) } ?: DummyComposeDefaults.BackgroundColor
    val textColor = appearance?.textColor?.let { Color(it) } ?: DummyComposeDefaults.TextColor
    val borderColor = container?.borderColor?.let { Color(it) } ?: DummyComposeDefaults.BorderColor
    val cornerRadius = (container?.cornerRadiusDp ?: DummyComposeDefaults.CORNER_RADIUS_DP).dp
    val borderWidth = (container?.borderWidthDp ?: 0f).dp
    val horizontalPadding = (container?.horizontalPaddingDp ?: DummyComposeDefaults.HORIZONTAL_PADDING_DP).dp
    val verticalPadding = (container?.verticalPaddingDp ?: DummyComposeDefaults.VERTICAL_PADDING_DP).dp
    val baseFontSize = appearance?.baseFontSizeSp
    val labelFontSize = (container?.label?.fontSizeSp ?: baseFontSize)?.sp ?: TextUnit.Unspecified
    val fundingFontSize = (container?.fundingInstrument?.textFontSizeSp ?: baseFontSize)?.sp ?: TextUnit.Unspecified
    val creditFontSize = (container?.creditMessaging?.fontSizeSp ?: baseFontSize)?.sp ?: TextUnit.Unspecified
    val creditLinkColor = container?.creditMessaging?.linkColor?.let { Color(it) } ?: textColor
    val logoWidth = (container?.logo?.widthDp ?: DummyComposeDefaults.LOGO_WIDTH_DP).dp
    val editIconSize = (container?.fundingInstrument?.editIconSizeDp ?: DummyComposeDefaults.EDIT_ICON_SIZE_DP).dp
    val labelMarginStart = (container?.label?.marginStartDp ?: DummyComposeDefaults.LABEL_MARGIN_START_DP).dp
    val fundingMarginStart =
        (container?.fundingInstrument?.marginStartDp ?: DummyComposeDefaults.FUNDING_MARGIN_START_DP).dp
    val heightDp = container?.heightDp

    var rowModifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp)
        .background(backgroundColor, RoundedCornerShape(cornerRadius))
    if (borderWidth > 0.dp) {
        rowModifier = rowModifier.border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
    }
    if (heightDp != null) {
        rowModifier = rowModifier.height(heightDp.dp)
    }

    Column(modifier = rowModifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (style.showPayPalLogo) {
                Image(
                    painter = painterResource(R.drawable.ic_paypal_brand_logo),
                    contentDescription = stringResource(R.string.paypal_saved_payment_method_label),
                    modifier = Modifier.size(width = logoWidth, height = 24.dp)
                )
            }
            if (style.showPayPalLabel) {
                Text(
                    text = stringResource(R.string.paypal_saved_payment_method_label),
                    color = textColor,
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = labelMarginStart)
                )
            }
            Text(
                text = stringResource(R.string.dummy_paypal_saved_payment_method_funding_instrument),
                color = textColor,
                fontSize = fundingFontSize,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = fundingMarginStart)
                    .clickable(onClick = onClick)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit_pencil),
                contentDescription = stringResource(R.string.paypal_saved_payment_method_edit_style_description),
                tint = textColor,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(editIconSize)
                    .clickable(onClick = onClick)
            )
        }
        if (style.showPayPalCreditMessaging) {
            Text(
                text = stringResource(R.string.dummy_paypal_saved_payment_method_credit_messaging),
                color = creditLinkColor,
                fontSize = creditFontSize,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
