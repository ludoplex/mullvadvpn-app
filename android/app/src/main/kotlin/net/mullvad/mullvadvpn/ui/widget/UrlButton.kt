package net.mullvad.mullvadvpn.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import net.mullvad.mullvadvpn.R

open class UrlButton : Button {
    constructor(context: Context) : super(context)

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    constructor(
        context: Context,
        attributes: AttributeSet,
        defaultStyleAttribute: Int
    ) : super(context, attributes, defaultStyleAttribute)

    init {
        super.setEnabled(false)
        super.detailImage = AppCompatResources.getDrawable(context, R.drawable.icon_extlink)
        super.showSpinner = true
    }
}
