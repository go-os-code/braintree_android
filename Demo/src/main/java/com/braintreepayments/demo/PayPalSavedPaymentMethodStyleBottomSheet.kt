package com.braintreepayments.demo

import android.app.Dialog
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.setFragmentResult
import java.util.Locale
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.ComponentAppearance
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.ContainerStyle
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.CreditMessagingStyle
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.FundingInstrumentStyle
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.PayPalLabelStyle
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.PayPalLogoStyle
import com.braintreepayments.api.paypalsavedpaymentmethod.styling.PayPalSavedPaymentMethodViewStyle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.roundToInt

/**
 * Shared "Edit Style" bottom sheet for `PayPalSavedPaymentMethod`, invokable from both the XML
 * screen ([PayPalSavedPaymentMethodFragment]) and the Compose screen
 * ([ComposePayPalSavedPaymentMethodFragment]). Exposes the full [PayPalSavedPaymentMethodViewStyle]
 * surface and returns the chosen values to the caller via the Fragment Result API, since the style
 * classes aren't `Parcelable`.
 *
 * TODO: font resource selection ([ComponentAppearance.fontResId]) isn't exposed here — it needs a
 * font-resource picker UI, out of scope for this demo.
 */
class PayPalSavedPaymentMethodStyleBottomSheet : BottomSheetDialogFragment() {

    // Not stored in savedInstanceState/arguments since PayPalSavedPaymentMethodViewStyle isn't
    // Parcelable — fine for this demo, which doesn't need to survive process death.
    private var initialStyle: PayPalSavedPaymentMethodViewStyle = PayPalSavedPaymentMethodViewStyle()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                // The sheet is always full-height, so there's nothing to drag it to — disabling
                // drag stops it from intercepting downward scroll gestures inside the form as a
                // dismiss swipe. It can still be dismissed via the scrim tap or back press.
                behavior.isDraggable = false
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                StyleForm(
                    initialValues = formValuesFromStyle(initialStyle),
                    onApply = { bundle ->
                        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundle)
                        dismiss()
                    }
                )
            }
        }
    }

    companion object {
        const val TAG = "PayPalSavedPaymentMethodStyleBottomSheet"
        const val REQUEST_KEY = "PayPalSavedPaymentMethodStyleBottomSheet.result"

        private const val FLOAT_UNSET = Float.NaN
        private const val COLOR_UNSET = Int.MIN_VALUE

        private const val KEY_SHOW_LOGO = "showPayPalLogo"
        private const val KEY_SHOW_LABEL = "showPayPalLabel"
        private const val KEY_SHOW_CREDIT_MESSAGING = "showPayPalCreditMessaging"
        private const val KEY_BACKGROUND_COLOR = "backgroundColor"
        private const val KEY_TEXT_COLOR = "textColor"
        private const val KEY_BASE_FONT_SIZE_SP = "baseFontSizeSp"
        private const val KEY_HEIGHT_DP = "heightDp"
        private const val KEY_HORIZONTAL_PADDING_DP = "horizontalPaddingDp"
        private const val KEY_VERTICAL_PADDING_DP = "verticalPaddingDp"
        private const val KEY_CORNER_RADIUS_DP = "cornerRadiusDp"
        private const val KEY_BORDER_COLOR = "borderColor"
        private const val KEY_BORDER_WIDTH_DP = "borderWidthDp"
        private const val KEY_LOGO_WIDTH_DP = "logoWidthDp"
        private const val KEY_LABEL_FONT_SIZE_SP = "labelFontSizeSp"
        private const val KEY_LABEL_MARGIN_START_DP = "labelMarginStartDp"
        private const val KEY_FUNDING_TEXT_FONT_SIZE_SP = "fundingInstrumentTextFontSizeSp"
        private const val KEY_FUNDING_EDIT_ICON_SIZE_DP = "fundingInstrumentEditIconSizeDp"
        private const val KEY_FUNDING_MARGIN_START_DP = "fundingInstrumentMarginStartDp"
        private const val KEY_CREDIT_FONT_SIZE_SP = "creditMessagingFontSizeSp"
        private const val KEY_CREDIT_LINK_COLOR = "creditMessagingLinkColor"

        /**
         * Creates the bottom sheet pre-filled with [style]'s current values, so reopening "Edit
         * Style" shows what's already applied instead of resetting to SDK defaults.
         */
        fun newInstance(style: PayPalSavedPaymentMethodViewStyle) =
            PayPalSavedPaymentMethodStyleBottomSheet().apply { initialStyle = style }

        /**
         * Shared reconstruction helper used by both the XML and Compose callers to turn a Fragment
         * Result bundle from this bottom sheet back into a [PayPalSavedPaymentMethodViewStyle].
         */
        fun styleFromBundle(bundle: Bundle): PayPalSavedPaymentMethodViewStyle {
            fun floatOrNull(key: String): Float? {
                val value = bundle.getFloat(key, FLOAT_UNSET)
                return if (value.isNaN()) null else value
            }
            fun colorOrNull(key: String): Int? {
                val value = bundle.getInt(key, COLOR_UNSET)
                return if (value == COLOR_UNSET) null else value
            }

            fun buildLogo(): PayPalLogoStyle? =
                floatOrNull(KEY_LOGO_WIDTH_DP)?.let { PayPalLogoStyle(widthDp = it) }

            fun buildLabel(): PayPalLabelStyle? {
                val fontSizeSp = floatOrNull(KEY_LABEL_FONT_SIZE_SP)
                val marginStartDp = floatOrNull(KEY_LABEL_MARGIN_START_DP)
                return if (fontSizeSp != null || marginStartDp != null) {
                    PayPalLabelStyle(fontSizeSp = fontSizeSp, marginStartDp = marginStartDp)
                } else {
                    null
                }
            }

            fun buildFundingInstrument(): FundingInstrumentStyle? {
                val textFontSizeSp = floatOrNull(KEY_FUNDING_TEXT_FONT_SIZE_SP)
                val editIconSizeDp = floatOrNull(KEY_FUNDING_EDIT_ICON_SIZE_DP)
                val marginStartDp = floatOrNull(KEY_FUNDING_MARGIN_START_DP)
                return if (textFontSizeSp != null || editIconSizeDp != null || marginStartDp != null) {
                    FundingInstrumentStyle(
                        textFontSizeSp = textFontSizeSp,
                        editIconSizeDp = editIconSizeDp,
                        marginStartDp = marginStartDp
                    )
                } else {
                    null
                }
            }

            fun buildCreditMessaging(): CreditMessagingStyle? {
                val fontSizeSp = floatOrNull(KEY_CREDIT_FONT_SIZE_SP)
                val linkColor = colorOrNull(KEY_CREDIT_LINK_COLOR)
                return if (fontSizeSp != null || linkColor != null) {
                    CreditMessagingStyle(fontSizeSp = fontSizeSp, linkColor = linkColor)
                } else {
                    null
                }
            }

            fun buildContainer(
                logo: PayPalLogoStyle?,
                label: PayPalLabelStyle?,
                fundingInstrument: FundingInstrumentStyle?,
                creditMessaging: CreditMessagingStyle?
            ): ContainerStyle? {
                val heightDp = floatOrNull(KEY_HEIGHT_DP)
                val horizontalPaddingDp = floatOrNull(KEY_HORIZONTAL_PADDING_DP)
                val verticalPaddingDp = floatOrNull(KEY_VERTICAL_PADDING_DP)
                val cornerRadiusDp = floatOrNull(KEY_CORNER_RADIUS_DP)
                val borderColor = colorOrNull(KEY_BORDER_COLOR)
                val borderWidthDp = floatOrNull(KEY_BORDER_WIDTH_DP)
                val hasOverride = listOf(
                    heightDp, horizontalPaddingDp, verticalPaddingDp, cornerRadiusDp, borderColor, borderWidthDp,
                    logo, label, fundingInstrument, creditMessaging
                ).any { it != null }
                return if (hasOverride) {
                    ContainerStyle(
                        heightDp = heightDp,
                        horizontalPaddingDp = horizontalPaddingDp,
                        verticalPaddingDp = verticalPaddingDp,
                        cornerRadiusDp = cornerRadiusDp,
                        borderColor = borderColor,
                        borderWidthDp = borderWidthDp,
                        logo = logo,
                        label = label,
                        fundingInstrument = fundingInstrument,
                        creditMessaging = creditMessaging
                    )
                } else {
                    null
                }
            }

            fun buildComponentAppearance(): ComponentAppearance? {
                val backgroundColor = colorOrNull(KEY_BACKGROUND_COLOR)
                val textColor = colorOrNull(KEY_TEXT_COLOR)
                val baseFontSizeSp = floatOrNull(KEY_BASE_FONT_SIZE_SP)
                return if (backgroundColor != null || textColor != null || baseFontSizeSp != null) {
                    ComponentAppearance(
                        backgroundColor = backgroundColor,
                        textColor = textColor,
                        baseFontSizeSp = baseFontSizeSp
                    )
                } else {
                    null
                }
            }

            val logo = buildLogo()
            val label = buildLabel()
            val fundingInstrument = buildFundingInstrument()
            val creditMessaging = buildCreditMessaging()

            return PayPalSavedPaymentMethodViewStyle(
                showPayPalLogo = bundle.getBoolean(KEY_SHOW_LOGO, true),
                showPayPalLabel = bundle.getBoolean(KEY_SHOW_LABEL, true),
                showPayPalCreditMessaging = bundle.getBoolean(KEY_SHOW_CREDIT_MESSAGING, true),
                componentAppearance = buildComponentAppearance(),
                container = buildContainer(logo, label, fundingInstrument, creditMessaging)
            )
        }
    }
}

/** Plain mirror of [PayPalSavedPaymentMethodViewStyle], used to seed and reset [StyleForm]. */
private data class StyleFormValues(
    val showLogo: Boolean = true,
    val showLabel: Boolean = true,
    val showCreditMessaging: Boolean = true,
    val backgroundColorText: String = "",
    val textColorText: String = "",
    val baseFontSizeText: String = "",
    val heightDp: Float? = null,
    val horizontalPaddingDp: Float? = null,
    val verticalPaddingDp: Float? = null,
    val cornerRadiusDp: Float? = null,
    val borderColorText: String = "",
    val borderWidthDp: Float? = null,
    val logoWidthDp: Float? = null,
    val labelFontSizeText: String = "",
    val labelMarginStartDp: Float? = null,
    val fundingTextFontSizeText: String = "",
    val fundingEditIconSizeDp: Float? = null,
    val fundingMarginStartDp: Float? = null,
    val creditFontSizeText: String = "",
    val creditLinkColorText: String = ""
)

private const val COLOR_MASK = 0xFFFFFF
private const val HSV_COMPONENT_COUNT = 3

private fun colorToText(color: Int?): String =
    color?.let { String.format(Locale.US, "#%06X", COLOR_MASK and it) } ?: ""

private fun floatToText(value: Float?): String = value?.let {
    if (it == it.toLong().toFloat()) it.toLong().toString() else it.toString()
} ?: ""

private fun formValuesFromStyle(style: PayPalSavedPaymentMethodViewStyle): StyleFormValues {
    val appearance = style.componentAppearance
    val container = style.container
    return StyleFormValues(
        showLogo = style.showPayPalLogo,
        showLabel = style.showPayPalLabel,
        showCreditMessaging = style.showPayPalCreditMessaging,
        backgroundColorText = colorToText(appearance?.backgroundColor),
        textColorText = colorToText(appearance?.textColor),
        baseFontSizeText = floatToText(appearance?.baseFontSizeSp),
        heightDp = container?.heightDp,
        horizontalPaddingDp = container?.horizontalPaddingDp,
        verticalPaddingDp = container?.verticalPaddingDp,
        cornerRadiusDp = container?.cornerRadiusDp,
        borderColorText = colorToText(container?.borderColor),
        borderWidthDp = container?.borderWidthDp,
        logoWidthDp = container?.logo?.widthDp,
        labelFontSizeText = floatToText(container?.label?.fontSizeSp),
        labelMarginStartDp = container?.label?.marginStartDp,
        fundingTextFontSizeText = floatToText(container?.fundingInstrument?.textFontSizeSp),
        fundingEditIconSizeDp = container?.fundingInstrument?.editIconSizeDp,
        fundingMarginStartDp = container?.fundingInstrument?.marginStartDp,
        creditFontSizeText = floatToText(container?.creditMessaging?.fontSizeSp),
        creditLinkColorText = colorToText(container?.creditMessaging?.linkColor)
    )
}

@Composable
private fun StyleForm(initialValues: StyleFormValues, onApply: (Bundle) -> Unit) {
    fun initialOrDefault(value: Float?, default: Float) = value ?: default
    fun sliderResult(enabled: Boolean, value: Float) = if (enabled) value else Float.NaN

    var showLogo by remember { mutableStateOf(initialValues.showLogo) }
    var showLabel by remember { mutableStateOf(initialValues.showLabel) }
    var showCreditMessaging by remember { mutableStateOf(initialValues.showCreditMessaging) }

    var backgroundColorText by remember { mutableStateOf(initialValues.backgroundColorText) }
    var textColorText by remember { mutableStateOf(initialValues.textColorText) }
    var baseFontSizeText by remember { mutableStateOf(initialValues.baseFontSizeText) }

    var heightEnabled by remember { mutableStateOf(initialValues.heightDp != null) }
    var heightValue by remember { mutableStateOf(initialOrDefault(initialValues.heightDp, SliderDefaults.HEIGHT_DP)) }
    var horizontalPaddingEnabled by remember { mutableStateOf(initialValues.horizontalPaddingDp != null) }
    var horizontalPaddingValue by remember {
        mutableStateOf(initialOrDefault(initialValues.horizontalPaddingDp, SliderDefaults.HORIZONTAL_PADDING_DP))
    }
    var verticalPaddingEnabled by remember { mutableStateOf(initialValues.verticalPaddingDp != null) }
    var verticalPaddingValue by remember {
        mutableStateOf(initialOrDefault(initialValues.verticalPaddingDp, SliderDefaults.VERTICAL_PADDING_DP))
    }
    var cornerRadiusEnabled by remember { mutableStateOf(initialValues.cornerRadiusDp != null) }
    var cornerRadiusValue by remember {
        mutableStateOf(initialOrDefault(initialValues.cornerRadiusDp, SliderDefaults.CORNER_RADIUS_DP))
    }
    var borderColorText by remember { mutableStateOf(initialValues.borderColorText) }
    var borderWidthEnabled by remember { mutableStateOf(initialValues.borderWidthDp != null) }
    var borderWidthValue by remember {
        mutableStateOf(initialOrDefault(initialValues.borderWidthDp, SliderDefaults.BORDER_WIDTH_DP))
    }

    var logoWidthEnabled by remember { mutableStateOf(initialValues.logoWidthDp != null) }
    var logoWidthValue by remember {
        mutableStateOf(initialOrDefault(initialValues.logoWidthDp, SliderDefaults.LOGO_WIDTH_DP))
    }

    var labelFontSizeText by remember { mutableStateOf(initialValues.labelFontSizeText) }
    var labelMarginStartEnabled by remember { mutableStateOf(initialValues.labelMarginStartDp != null) }
    var labelMarginStartValue by remember {
        mutableStateOf(initialOrDefault(initialValues.labelMarginStartDp, SliderDefaults.LABEL_MARGIN_START_DP))
    }

    var fundingTextFontSizeText by remember { mutableStateOf(initialValues.fundingTextFontSizeText) }
    var fundingEditIconSizeEnabled by remember { mutableStateOf(initialValues.fundingEditIconSizeDp != null) }
    var fundingEditIconSizeValue by remember {
        mutableStateOf(initialOrDefault(initialValues.fundingEditIconSizeDp, SliderDefaults.FUNDING_EDIT_ICON_SIZE_DP))
    }
    var fundingMarginStartEnabled by remember { mutableStateOf(initialValues.fundingMarginStartDp != null) }
    var fundingMarginStartValue by remember {
        mutableStateOf(initialOrDefault(initialValues.fundingMarginStartDp, SliderDefaults.FUNDING_MARGIN_START_DP))
    }

    var creditFontSizeText by remember { mutableStateOf(initialValues.creditFontSizeText) }
    var creditLinkColorText by remember { mutableStateOf(initialValues.creditLinkColorText) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text("Edit Style", style = MaterialTheme.typography.headlineSmall)

            SwitchRow("Show PayPal Logo", showLogo) { showLogo = it }
            SwitchRow("Show PayPal Label", showLabel) { showLabel = it }
            SwitchRow("Show Credit Messaging", showCreditMessaging) { showCreditMessaging = it }

            SectionHeader("Component Appearance")
            ColorPickerField("Background Color", backgroundColorText) { backgroundColorText = it }
            ColorPickerField("Text Color", textColorText) { textColorText = it }
            StyleTextField("Base Font Size (sp)", baseFontSizeText, KeyboardType.Decimal) { baseFontSizeText = it }

            SectionHeader("Container")
            SliderRow(
                "Height (dp)", heightEnabled, heightValue,
                SliderDefaults.HEIGHT_MIN_DP..SliderDefaults.HEIGHT_MAX_DP,
                { heightEnabled = it }
            ) { heightValue = it }
            SliderRow(
                "Horizontal Padding (dp)", horizontalPaddingEnabled, horizontalPaddingValue,
                SliderDefaults.SPACING_MIN_DP..SliderDefaults.SPACING_MAX_DP,
                { horizontalPaddingEnabled = it }
            ) { horizontalPaddingValue = it }
            SliderRow(
                "Vertical Padding (dp)", verticalPaddingEnabled, verticalPaddingValue,
                SliderDefaults.SPACING_MIN_DP..SliderDefaults.SPACING_MAX_DP,
                { verticalPaddingEnabled = it }
            ) { verticalPaddingValue = it }
            SliderRow(
                "Corner Radius (dp)", cornerRadiusEnabled, cornerRadiusValue,
                SliderDefaults.SPACING_MIN_DP..SliderDefaults.SPACING_MAX_DP,
                { cornerRadiusEnabled = it }
            ) { cornerRadiusValue = it }
            ColorPickerField("Border Color", borderColorText) { borderColorText = it }
            SliderRow(
                "Border Width (dp)", borderWidthEnabled, borderWidthValue,
                SliderDefaults.BORDER_WIDTH_MIN_DP..SliderDefaults.BORDER_WIDTH_MAX_DP,
                { borderWidthEnabled = it }
            ) { borderWidthValue = it }

            SectionHeader("Logo")
            SliderRow(
                "Width (dp)", logoWidthEnabled, logoWidthValue,
                SliderDefaults.LOGO_WIDTH_MIN_DP..SliderDefaults.LOGO_WIDTH_MAX_DP,
                { logoWidthEnabled = it }
            ) { logoWidthValue = it }

            SectionHeader("Label")
            StyleTextField("Font Size (sp)", labelFontSizeText, KeyboardType.Decimal) { labelFontSizeText = it }
            SliderRow(
                "Margin Start (dp)", labelMarginStartEnabled, labelMarginStartValue,
                SliderDefaults.MARGIN_MIN_DP..SliderDefaults.MARGIN_MAX_DP,
                { labelMarginStartEnabled = it }
            ) { labelMarginStartValue = it }

            SectionHeader("Funding Instrument")
            StyleTextField("Text Font Size (sp)", fundingTextFontSizeText, KeyboardType.Decimal) {
                fundingTextFontSizeText = it
            }
            SliderRow(
                "Edit Icon Size (dp)", fundingEditIconSizeEnabled, fundingEditIconSizeValue,
                SliderDefaults.SPACING_MIN_DP..SliderDefaults.SPACING_MAX_DP,
                { fundingEditIconSizeEnabled = it }
            ) { fundingEditIconSizeValue = it }
            SliderRow(
                "Margin Start (dp)", fundingMarginStartEnabled, fundingMarginStartValue,
                SliderDefaults.MARGIN_MIN_DP..SliderDefaults.MARGIN_MAX_DP,
                { fundingMarginStartEnabled = it }
            ) { fundingMarginStartValue = it }

            SectionHeader("Credit Messaging")
            StyleTextField("Font Size (sp)", creditFontSizeText, KeyboardType.Decimal) { creditFontSizeText = it }
            ColorPickerField("Link Color", creditLinkColorText) { creditLinkColorText = it }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val defaults = StyleFormValues()
                    showLogo = defaults.showLogo
                    showLabel = defaults.showLabel
                    showCreditMessaging = defaults.showCreditMessaging
                    backgroundColorText = defaults.backgroundColorText
                    textColorText = defaults.textColorText
                    baseFontSizeText = defaults.baseFontSizeText
                    heightEnabled = false
                    heightValue = SliderDefaults.HEIGHT_DP
                    horizontalPaddingEnabled = false
                    horizontalPaddingValue = SliderDefaults.HORIZONTAL_PADDING_DP
                    verticalPaddingEnabled = false
                    verticalPaddingValue = SliderDefaults.VERTICAL_PADDING_DP
                    cornerRadiusEnabled = false
                    cornerRadiusValue = SliderDefaults.CORNER_RADIUS_DP
                    borderColorText = defaults.borderColorText
                    borderWidthEnabled = false
                    borderWidthValue = SliderDefaults.BORDER_WIDTH_DP
                    logoWidthEnabled = false
                    logoWidthValue = SliderDefaults.LOGO_WIDTH_DP
                    labelFontSizeText = defaults.labelFontSizeText
                    labelMarginStartEnabled = false
                    labelMarginStartValue = SliderDefaults.LABEL_MARGIN_START_DP
                    fundingTextFontSizeText = defaults.fundingTextFontSizeText
                    fundingEditIconSizeEnabled = false
                    fundingEditIconSizeValue = SliderDefaults.FUNDING_EDIT_ICON_SIZE_DP
                    fundingMarginStartEnabled = false
                    fundingMarginStartValue = SliderDefaults.FUNDING_MARGIN_START_DP
                    creditFontSizeText = defaults.creditFontSizeText
                    creditLinkColorText = defaults.creditLinkColorText
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset to Default")
            }
            Button(
                onClick = {
                    onApply(
                        Bundle().apply {
                            putBoolean("showPayPalLogo", showLogo)
                            putBoolean("showPayPalLabel", showLabel)
                            putBoolean("showPayPalCreditMessaging", showCreditMessaging)
                            putInt("backgroundColor", parseColorForBundle(backgroundColorText))
                            putInt("textColor", parseColorForBundle(textColorText))
                            putFloat("baseFontSizeSp", parseFloatForBundle(baseFontSizeText))
                            putFloat("heightDp", if (heightEnabled) heightValue else Float.NaN)
                            putFloat(
                                "horizontalPaddingDp",
                                if (horizontalPaddingEnabled) horizontalPaddingValue else Float.NaN
                            )
                            putFloat(
                                "verticalPaddingDp",
                                if (verticalPaddingEnabled) verticalPaddingValue else Float.NaN
                            )
                            putFloat("cornerRadiusDp", if (cornerRadiusEnabled) cornerRadiusValue else Float.NaN)
                            putInt("borderColor", parseColorForBundle(borderColorText))
                            putFloat("borderWidthDp", if (borderWidthEnabled) borderWidthValue else Float.NaN)
                            putFloat("logoWidthDp", if (logoWidthEnabled) logoWidthValue else Float.NaN)
                            putFloat("labelFontSizeSp", parseFloatForBundle(labelFontSizeText))
                            putFloat(
                                "labelMarginStartDp",
                                if (labelMarginStartEnabled) labelMarginStartValue else Float.NaN
                            )
                            putFloat("fundingInstrumentTextFontSizeSp", parseFloatForBundle(fundingTextFontSizeText))
                            putFloat(
                                "fundingInstrumentEditIconSizeDp",
                                if (fundingEditIconSizeEnabled) fundingEditIconSizeValue else Float.NaN
                            )
                            putFloat(
                                "fundingInstrumentMarginStartDp",
                                if (fundingMarginStartEnabled) fundingMarginStartValue else Float.NaN
                            )
                            putFloat("creditMessagingFontSizeSp", parseFloatForBundle(creditFontSizeText))
                            putInt("creditMessagingLinkColor", parseColorForBundle(creditLinkColorText))
                        }
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }
    }
}

private object SliderDefaults {
    const val HEIGHT_DP = 56f
    const val HEIGHT_MIN_DP = 0f
    const val HEIGHT_MAX_DP = 160f

    const val HORIZONTAL_PADDING_DP = 16f
    const val VERTICAL_PADDING_DP = 12f
    const val CORNER_RADIUS_DP = 8f
    const val SPACING_MIN_DP = 0f
    const val SPACING_MAX_DP = 48f

    const val BORDER_WIDTH_DP = 1f
    const val BORDER_WIDTH_MIN_DP = 0f
    const val BORDER_WIDTH_MAX_DP = 8f

    const val LOGO_WIDTH_DP = 40f
    const val LOGO_WIDTH_MIN_DP = 0f
    const val LOGO_WIDTH_MAX_DP = 80f

    const val LABEL_MARGIN_START_DP = 8f
    const val MARGIN_MIN_DP = 0f
    const val MARGIN_MAX_DP = 32f

    const val FUNDING_EDIT_ICON_SIZE_DP = 20f
    const val FUNDING_MARGIN_START_DP = 8f
}

private fun parseColorForBundle(text: String): Int =
    if (text.isBlank()) Int.MIN_VALUE else runCatching { AndroidColor.parseColor(text.trim()) }.getOrDefault(Int.MIN_VALUE)

private fun parseFloatForBundle(text: String): Float = text.trim().toFloatOrNull() ?: Float.NaN

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionHeader(title: String) {
    HorizontalDivider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
    Text(title, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun SliderRow(
    label: String,
    enabled: Boolean,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onEnabledChange: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, modifier = Modifier.weight(1f))
                Text("${value.roundToInt()}", modifier = Modifier.width(32.dp))
            }
        }
    }
}

@Composable
private fun StyleTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

/**
 * Hex text field + swatch button that expands into an HSV picker (saturation/value square + hue
 * slider). Still just reads/writes [value] as a `#RRGGBB` hex string, so Apply's bundle logic is
 * unaffected by whether the color came from typing or from the picker.
 */
@Composable
private fun ColorPickerField(label: String, value: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }
    var brightness by remember { mutableFloatStateOf(1f) }

    // Keeps the picker's sliders in sync if the hex text changes from outside the picker
    // (typed by hand, or "Reset to Default").
    LaunchedEffect(value) {
        val parsed = if (value.isBlank()) null else runCatching { AndroidColor.parseColor(value.trim()) }.getOrNull()
        if (parsed != null) {
            val hsv = FloatArray(HSV_COMPONENT_COUNT)
            AndroidColor.colorToHSV(parsed, hsv)
            hue = hsv[0]
            saturation = hsv[1]
            brightness = hsv[2]
        }
    }

    fun applyHsv() {
        onValueChange(colorToText(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness))))
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("$label (#RRGGBB)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(36.dp)
                    .background(Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness))), CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
                    .clickable { expanded = !expanded }
            )
        }
        if (expanded) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 8.dp)
                    .pointerInput(hue) {
                        fun update(offset: Offset) {
                            saturation = (offset.x / size.width).coerceIn(0f, 1f)
                            brightness = (1f - offset.y / size.height).coerceIn(0f, 1f)
                            applyHsv()
                        }
                        detectDragGestures(
                            onDragStart = { update(it) },
                            onDrag = { change, _ -> change.consume(); update(change.position) }
                        )
                    }
            ) {
                val hueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))
                drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(saturation * size.width, (1f - brightness) * size.height),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            Slider(
                value = hue,
                onValueChange = { hue = it; applyHsv() },
                valueRange = 0f..360f,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
