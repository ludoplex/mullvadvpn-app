package net.mullvad.mullvadvpn.compose.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.mullvad.mullvadvpn.compose.state.ConnectNotificationState
import net.mullvad.mullvadvpn.compose.state.ConnectUiState
import net.mullvad.mullvadvpn.compose.test.CIRCULAR_PROGRESS_INDICATOR
import net.mullvad.mullvadvpn.compose.test.CONNECT_BUTTON_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.LOCATION_INFO_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.NOTIFICATION_BANNER
import net.mullvad.mullvadvpn.compose.test.RECONNECT_BUTTON_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.SCROLLABLE_COLUMN_TEST_TAG
import net.mullvad.mullvadvpn.compose.test.SELECT_LOCATION_BUTTON_TEST_TAG
import net.mullvad.mullvadvpn.model.GeoIpLocation
import net.mullvad.mullvadvpn.model.TunnelState
import net.mullvad.mullvadvpn.relaylist.RelayItem
import net.mullvad.mullvadvpn.ui.VersionInfo
import net.mullvad.mullvadvpn.viewmodel.ConnectViewModel
import net.mullvad.talpid.net.TransportProtocol
import net.mullvad.talpid.net.TunnelEndpoint
import net.mullvad.talpid.tunnel.ActionAfterDisconnect
import net.mullvad.talpid.tunnel.ErrorState
import net.mullvad.talpid.tunnel.ErrorStateCause
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConnectScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testDefaultState() {
        // Arrange
        composeTestRule.setContent {
            ConnectScreen(
                uiState = ConnectUiState.INITIAL,
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithTag(SCROLLABLE_COLUMN_TEST_TAG).assertExists()
            onNodeWithText("UNSECURED CONNECTION").assertExists()
            onNodeWithText("Secure my connection").assertExists()
        }
    }

    @Test
    fun testConnectingState() {
        // Arrange
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowTunnelStateNotificationBlocked
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithTag(CIRCULAR_PROGRESS_INDICATOR).assertExists()
            onNodeWithText("CREATING SECURE CONNECTION").assertExists()
            onNodeWithText("Switch location").assertExists()
            onNodeWithText("Cancel").assertExists()
            onNodeWithText("BLOCKING INTERNET").assertExists()
        }
    }

    @Test
    fun testConnectingStateQuantumSecured() {
        // Arrange
        val mockTunnelEndpoint: TunnelEndpoint = mockk(relaxed = true)
        every { mockTunnelEndpoint.quantumResistant } returns true
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(endpoint = mockTunnelEndpoint, null),
                        tunnelRealState =
                            TunnelState.Connecting(endpoint = mockTunnelEndpoint, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowTunnelStateNotificationBlocked
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithTag(CIRCULAR_PROGRESS_INDICATOR).assertExists()
            onNodeWithText("CREATING QUANTUM SECURE CONNECTION").assertExists()
            onNodeWithText("Switch location").assertExists()
            onNodeWithText("Cancel").assertExists()
            onNodeWithText("BLOCKING INTERNET").assertExists()
        }
    }

    @Test
    fun testConnectedState() {
        // Arrange
        val mockTunnelEndpoint: TunnelEndpoint = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connected(mockTunnelEndpoint, null),
                        tunnelRealState = TunnelState.Connected(mockTunnelEndpoint, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("SECURE CONNECTION").assertExists()
            onNodeWithText("Switch location").assertExists()
            onNodeWithText("Disconnect").assertExists()
        }
    }

    @Test
    fun testConnectedStateQuantumSecured() {
        // Arrange
        val mockTunnelEndpoint: TunnelEndpoint = mockk(relaxed = true)
        every { mockTunnelEndpoint.quantumResistant } returns true
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connected(mockTunnelEndpoint, null),
                        tunnelRealState = TunnelState.Connected(mockTunnelEndpoint, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("QUANTUM SECURE CONNECTION").assertExists()
            onNodeWithText("Switch location").assertExists()
            onNodeWithText("Disconnect").assertExists()
        }
    }

    @Test
    fun testDisconnectingState() {
        // Arrange
        val mockRelayLocation: RelayItem = mockk(relaxed = true)
        val mockLocationName = "Home"
        every { mockRelayLocation.locationName } returns mockLocationName
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = mockRelayLocation,
                        tunnelUiState = TunnelState.Disconnecting(ActionAfterDisconnect.Nothing),
                        tunnelRealState = TunnelState.Disconnecting(ActionAfterDisconnect.Nothing),
                        inAddress = null,
                        outAddress = "",
                        showLocation = true,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("UNSECURED CONNECTION").assertExists()
            onNodeWithText(mockLocationName).assertExists()
            onNodeWithText("Disconnect").assertExists()
        }
    }

    @Test
    fun testDisconnectedState() {
        // Arrange
        val mockRelayLocation: RelayItem = mockk(relaxed = true)
        val mockLocationName = "Home"
        every { mockRelayLocation.locationName } returns mockLocationName
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = mockRelayLocation,
                        tunnelUiState = TunnelState.Disconnected,
                        tunnelRealState = TunnelState.Disconnected,
                        inAddress = null,
                        outAddress = "",
                        showLocation = true,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("UNSECURED CONNECTION").assertExists()
            onNodeWithText(mockLocationName).assertExists()
            onNodeWithText("Secure my connection").assertExists()
        }
    }

    @Test
    fun testErrorStateBlocked() {
        // Arrange
        val mockRelayLocation: RelayItem = mockk(relaxed = true)
        val mockLocationName = "Home"
        every { mockRelayLocation.locationName } returns mockLocationName
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = mockRelayLocation,
                        tunnelUiState =
                            TunnelState.Error(ErrorState(ErrorStateCause.StartTunnelError, true)),
                        tunnelRealState =
                            TunnelState.Error(ErrorState(ErrorStateCause.StartTunnelError, true)),
                        inAddress = null,
                        outAddress = "",
                        showLocation = true,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowTunnelStateNotificationError(
                                ErrorState(ErrorStateCause.StartTunnelError, true)
                            )
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("BLOCKED CONNECTION").assertExists()
            onNodeWithText(mockLocationName).assertExists()
            onNodeWithText("Disconnect").assertExists()
            onNodeWithText("BLOCKING INTERNET").assertExists()
        }
    }

    @Test
    fun testErrorStateNotBlocked() {
        // Arrange
        val mockRelayLocation: RelayItem = mockk(relaxed = true)
        val mockLocationName = "Home"
        every { mockRelayLocation.locationName } returns mockLocationName
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = mockRelayLocation,
                        tunnelUiState =
                            TunnelState.Error(ErrorState(ErrorStateCause.StartTunnelError, false)),
                        tunnelRealState =
                            TunnelState.Error(ErrorState(ErrorStateCause.StartTunnelError, false)),
                        inAddress = null,
                        outAddress = "",
                        showLocation = true,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowTunnelStateNotificationError(
                                ErrorState(ErrorStateCause.StartTunnelError, false)
                            )
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("FAILED TO SECURE CONNECTION").assertExists()
            onNodeWithText(mockLocationName).assertExists()
            onNodeWithText("Dismiss").assertExists()
            onNodeWithText(text = "Critical error (your attention is required)", ignoreCase = true)
                .assertExists()
        }
    }

    @Test
    fun testReconnectingState() {
        // Arrange
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Disconnecting(ActionAfterDisconnect.Reconnect),
                        tunnelRealState =
                            TunnelState.Disconnecting(ActionAfterDisconnect.Reconnect),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowTunnelStateNotificationBlocked
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithTag(CIRCULAR_PROGRESS_INDICATOR).assertExists()
            onNodeWithText("CREATING SECURE CONNECTION").assertExists()
            onNodeWithText("Switch location").assertExists()
            onNodeWithText("Disconnect").assertExists()
            onNodeWithText("BLOCKING INTERNET").assertExists()
        }
    }

    @Test
    fun testDisconnectingBlockState() {
        // Arrange
        val mockRelayLocation: RelayItem = mockk(relaxed = true)
        val mockLocationName = "Home"
        every { mockRelayLocation.locationName } returns mockLocationName
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = mockRelayLocation,
                        tunnelUiState = TunnelState.Disconnecting(ActionAfterDisconnect.Block),
                        tunnelRealState = TunnelState.Disconnecting(ActionAfterDisconnect.Block),
                        inAddress = null,
                        outAddress = "",
                        showLocation = true,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowTunnelStateNotificationBlocked
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("SECURE CONNECTION").assertExists()
            onNodeWithText(mockLocationName).assertExists()
            onNodeWithText("Disconnect").assertExists()
            onNodeWithText("BLOCKING INTERNET").assertExists()
        }
    }

    @Test
    fun testClickSelectLocationButton() {
        // Arrange
        val mockRelayLocation: RelayItem = mockk(relaxed = true)
        val mockLocationName = "Home"
        every { mockRelayLocation.locationName } returns mockLocationName
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = mockRelayLocation,
                        tunnelUiState = TunnelState.Disconnected,
                        tunnelRealState = TunnelState.Disconnected,
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow(),
                onSwitchLocationClick = mockedClickHandler
            )
        }

        // Act
        composeTestRule.onNodeWithTag(SELECT_LOCATION_BUTTON_TEST_TAG).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testOnDisconnectClick() {
        // Arrange
        val mockTunnelEndpoint: TunnelEndpoint = mockk(relaxed = true)
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connected(mockTunnelEndpoint, null),
                        tunnelRealState = TunnelState.Connected(mockTunnelEndpoint, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow(),
                onDisconnectClick = mockedClickHandler
            )
        }

        // Act
        composeTestRule.onNodeWithTag(CONNECT_BUTTON_TEST_TAG).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testOnReconnectClick() {
        // Arrange
        val mockTunnelEndpoint: TunnelEndpoint = mockk(relaxed = true)
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connected(mockTunnelEndpoint, null),
                        tunnelRealState = TunnelState.Connected(mockTunnelEndpoint, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow(),
                onReconnectClick = mockedClickHandler
            )
        }

        // Act
        composeTestRule.onNodeWithTag(RECONNECT_BUTTON_TEST_TAG).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testOnConnectClick() {
        // Arrange
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Disconnected,
                        tunnelRealState = TunnelState.Disconnected,
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow(),
                onConnectClick = mockedClickHandler
            )
        }

        // Act
        composeTestRule.onNodeWithTag(CONNECT_BUTTON_TEST_TAG).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testOnCancelClick() {
        // Arrange
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow(),
                onCancelClick = mockedClickHandler
            )
        }

        // Act
        composeTestRule.onNodeWithTag(CONNECT_BUTTON_TEST_TAG).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testToggleTunnelInfo() {
        // Arrange
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        val dummyLocation = GeoIpLocation(null, null, "dummy country", null, "dummy hostname")
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = dummyLocation,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow(),
                onToggleTunnelInfo = mockedClickHandler
            )
        }

        // Act
        composeTestRule.onNodeWithTag(LOCATION_INFO_TEST_TAG).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun showLocationInfo() {
        // Arrange
        val mockLocation: GeoIpLocation = mockk(relaxed = true)
        val mockTunnelEndpoint: TunnelEndpoint = mockk(relaxed = true)
        val mockHostName = "Host-Name"
        val mockPort = 99
        val mockHost = "Host"
        val mockProtocol = TransportProtocol.Udp
        val mockInAddress = Triple(mockHost, mockPort, mockProtocol)
        val mockOutAddress = "HostAddressV4 / HostAddressV4"
        every { mockLocation.hostname } returns mockHostName
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = mockLocation,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connected(mockTunnelEndpoint, null),
                        tunnelRealState = TunnelState.Connected(mockTunnelEndpoint, null),
                        inAddress = mockInAddress,
                        outAddress = mockOutAddress,
                        showLocation = false,
                        isTunnelInfoExpanded = true,
                        connectNotificationState = ConnectNotificationState.HideNotification
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText(mockHostName).assertExists()
            onNodeWithText("WireGuard").assertExists()
            onNodeWithText("In $mockHost:$mockPort UDP").assertExists()
            onNodeWithText("Out $mockOutAddress").assertExists()
        }
    }

    @Test
    fun testOutdatedVersionNotification() {
        // Arrange
        val versionInfo =
            VersionInfo(
                currentVersion = "1.0",
                upgradeVersion = "1.1",
                isOutdated = true,
                isSupported = true
            )
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowVersionInfoNotification(versionInfo)
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("UPDATE AVAILABLE").assertExists()
            onNodeWithText("Install Mullvad VPN (1.1) to stay up to date").assertExists()
        }
    }

    @Test
    fun testUnsupportedVersionNotification() {
        // Arrange
        val versionInfo =
            VersionInfo(
                currentVersion = "1.0",
                upgradeVersion = "1.1",
                isOutdated = true,
                isSupported = false
            )
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowVersionInfoNotification(versionInfo)
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("UNSUPPORTED VERSION").assertExists()
            onNodeWithText(
                    "Your privacy might be at risk with this unsupported app version. Please update now."
                )
                .assertExists()
        }
    }

    @Test
    fun testAccountExpiredNotification() {
        // Arrange
        val expiryDate = DateTime(2020, 11, 11, 10, 10)
        composeTestRule.setContent {
            ConnectScreen(
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowAccountExpiryNotification(expiryDate)
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Assert
        composeTestRule.apply {
            onNodeWithText("ACCOUNT CREDIT EXPIRES SOON").assertExists()
            onNodeWithText("Out of time").assertExists()
        }
    }

    @Test
    fun testOnUpdateVersionClick() {
        // Arrange
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        val versionInfo =
            VersionInfo(
                currentVersion = "1.0",
                upgradeVersion = "1.1",
                isOutdated = true,
                isSupported = false
            )
        composeTestRule.setContent {
            ConnectScreen(
                onUpdateVersionClick = mockedClickHandler,
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowVersionInfoNotification(versionInfo)
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Act
        composeTestRule.onNodeWithTag(NOTIFICATION_BANNER).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testOnShowAccountClick() {
        // Arrange
        val mockedClickHandler: () -> Unit = mockk(relaxed = true)
        val expiryDate = DateTime(2020, 11, 11, 10, 10)
        composeTestRule.setContent {
            ConnectScreen(
                onManageAccountClick = mockedClickHandler,
                uiState =
                    ConnectUiState(
                        location = null,
                        relayLocation = null,
                        tunnelUiState = TunnelState.Connecting(null, null),
                        tunnelRealState = TunnelState.Connecting(null, null),
                        inAddress = null,
                        outAddress = "",
                        showLocation = false,
                        isTunnelInfoExpanded = false,
                        connectNotificationState =
                            ConnectNotificationState.ShowAccountExpiryNotification(expiryDate)
                    ),
                viewActions = MutableSharedFlow<ConnectViewModel.ViewAction>().asSharedFlow()
            )
        }

        // Act
        composeTestRule.onNodeWithTag(NOTIFICATION_BANNER).performClick()

        // Assert
        verify { mockedClickHandler.invoke() }
    }

    @Test
    fun testOpenAccountView() {
        // Arrange
        composeTestRule.setContent {
            ConnectScreen(
                uiState = ConnectUiState.INITIAL,
                viewActions =
                    MutableStateFlow(
                        ConnectViewModel.ViewAction.OpenAccountManagementPageInBrowser("222")
                    )
            )
        }

        // Assert
        composeTestRule.apply { onNodeWithTag(SCROLLABLE_COLUMN_TEST_TAG).assertDoesNotExist() }
    }

    @Test
    fun testOpenOutOfTimeScreen() {
        // Arrange
        val mockedOpenScreenHandler: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            ConnectScreen(
                uiState = ConnectUiState.INITIAL,
                viewActions = MutableStateFlow(ConnectViewModel.ViewAction.OpenOutOfTimeView),
                onOpenOutOfTimeScreen = mockedOpenScreenHandler
            )
        }

        // Assert
        verify { mockedOpenScreenHandler.invoke() }
    }
}
