package net.mullvad.mullvadvpn.compose.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.mullvad.mullvadvpn.R
import net.mullvad.mullvadvpn.compose.button.ConnectionButton
import net.mullvad.mullvadvpn.compose.button.SwitchLocationButton
import net.mullvad.mullvadvpn.compose.component.ConnectionStatusText
import net.mullvad.mullvadvpn.compose.component.LocationInfo
import net.mullvad.mullvadvpn.compose.component.Notification
import net.mullvad.mullvadvpn.compose.component.ScaffoldWithTopBar
import net.mullvad.mullvadvpn.compose.state.ConnectUiState
import net.mullvad.mullvadvpn.compose.test.CIRCULAR_PROGRESS_INDICATOR
import net.mullvad.mullvadvpn.compose.test.CONNECT_BUTTON_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.LOCATION_INFO_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.RECONNECT_BUTTON_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.SCROLLABLE_COLUMN_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.SELECT_LOCATION_BUTTON_TEST_TAG
import net.mullvad.mullvadvpn.lib.common.util.openAccountPageInBrowser
import net.mullvad.mullvadvpn.lib.theme.AlphaTopBar
import net.mullvad.mullvadvpn.lib.theme.AppTheme
import net.mullvad.mullvadvpn.lib.theme.Dimens
import net.mullvad.mullvadvpn.model.TunnelState
import net.mullvad.mullvadvpn.viewmodel.ConnectViewModel
import net.mullvad.talpid.tunnel.ActionAfterDisconnect

private const val CONNECT_BUTTON_THROTTLE_MILLIS = 1000

@Preview
@Composable
fun PreviewConnectScreen() {
    val state = ConnectUiState.INITIAL
    AppTheme {
        ConnectScreen(
            uiState = state,
            viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
        )
    }
}

@Composable
fun ConnectScreen(
    uiState: ConnectUiState,
    viewActions: SharedFlow<ConnectViewModel.ViewAction>,
    onDisconnectClick: () -> Unit = {},
    onReconnectClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onSwitchLocationClick: () -> Unit = {},
    onToggleTunnelInfo: () -> Unit = {},
    onUpdateVersionClick: () -> Unit = {},
    onManageAccountClick: () -> Unit = {},
    onOpenOutOfTimeScreen: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        viewActions.collect { viewAction ->
            when (viewAction) {
                is ConnectViewModel.ViewAction.OpenAccountManagementPageInBrowser -> {
                    context.openAccountPageInBrowser(viewAction.token)
                }
                is ConnectViewModel.ViewAction.OpenOutOfTimeView -> {
                    onOpenOutOfTimeScreen()
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    var lastConnectionActionTimestamp by remember { mutableLongStateOf(0L) }

    fun handleThrottledAction(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastConnectionActionTimestamp) > CONNECT_BUTTON_THROTTLE_MILLIS) {
            lastConnectionActionTimestamp = currentTime
            action.invoke()
        }
    }

    ScaffoldWithTopBar(
        topBarColor =
            if (uiState.tunnelUiState.isSecured()) {
                MaterialTheme.colorScheme.inversePrimary
            } else {
                MaterialTheme.colorScheme.error
            },
        statusBarColor =
            if (uiState.tunnelUiState.isSecured()) {
                MaterialTheme.colorScheme.inversePrimary
            } else {
                MaterialTheme.colorScheme.error
            },
        navigationBarColor = MaterialTheme.colorScheme.primary,
        iconTintColor =
            if (uiState.tunnelUiState.isSecured()) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onError
                }
                .copy(alpha = AlphaTopBar),
        onSettingsClicked = onSettingsClick,
        onAccountClicked = onAccountClick
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
            modifier =
                Modifier.padding(it)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(bottom = Dimens.screenVerticalMargin)
                    .testTag(SCROLLABLE_COLUMN_TEST_TAG)
        ) {
            Notification(
                connectNotificationState = uiState.connectNotificationState,
                onClickUpdateVersion = onUpdateVersionClick,
                onClickShowAccount = onManageAccountClick
            )
            Spacer(modifier = Modifier.weight(1f))
            if (
                uiState.tunnelRealState is TunnelState.Connecting ||
                    (uiState.tunnelRealState is TunnelState.Disconnecting &&
                        uiState.tunnelRealState.actionAfterDisconnect ==
                            ActionAfterDisconnect.Reconnect)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier.padding(
                                start = Dimens.sideMargin,
                                end = Dimens.sideMargin,
                                top = Dimens.mediumPadding
                            )
                            .size(
                                width = Dimens.progressIndicatorSize,
                                height = Dimens.progressIndicatorSize
                            )
                            .align(Alignment.CenterHorizontally)
                            .testTag(CIRCULAR_PROGRESS_INDICATOR)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.mediumPadding))
            ConnectionStatusText(
                state = uiState.tunnelRealState,
                modifier = Modifier.padding(horizontal = Dimens.sideMargin)
            )
            Text(
                text = uiState.location?.country ?: "",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = Dimens.sideMargin)
            )
            Text(
                text = uiState.location?.city ?: "",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = Dimens.sideMargin)
            )
            LocationInfo(
                onToggleTunnelInfo = onToggleTunnelInfo,
                isVisible =
                    uiState.tunnelRealState != TunnelState.Disconnected &&
                        uiState.location?.hostname != null,
                isExpanded = uiState.isTunnelInfoExpanded,
                location = uiState.location,
                inAddress = uiState.inAddress,
                outAddress = uiState.outAddress,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = Dimens.sideMargin)
                        .testTag(LOCATION_INFO_TEST_TAG)
            )
            Spacer(modifier = Modifier.height(Dimens.buttonSeparation))
            SwitchLocationButton(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(Dimens.selectLocationButtonHeight)
                        .padding(horizontal = Dimens.sideMargin)
                        .testTag(SELECT_LOCATION_BUTTON_TEST_TAG),
                onClick = onSwitchLocationClick,
                showChevron = uiState.showLocation,
                text =
                    if (uiState.showLocation) {
                        uiState.relayLocation?.locationName ?: ""
                    } else {
                        stringResource(id = R.string.switch_location)
                    }
            )
            Spacer(modifier = Modifier.height(Dimens.buttonSeparation))
            ConnectionButton(
                state = uiState.tunnelUiState,
                modifier =
                    Modifier.fillMaxWidth()
                        .height(Dimens.connectButtonHeight)
                        .padding(horizontal = Dimens.sideMargin)
                        .testTag(CONNECT_BUTTON_TEST_TAG),
                disconnectClick = onDisconnectClick,
                reconnectClick = { handleThrottledAction(onReconnectClick) },
                cancelClick = onCancelClick,
                connectClick = { handleThrottledAction(onConnectClick) },
                reconnectButtonTestTag = RECONNECT_BUTTON_TEST_TAG
            )
        }
    }
}
