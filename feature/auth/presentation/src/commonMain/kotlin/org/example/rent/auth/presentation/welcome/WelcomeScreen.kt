package org.example.rent.auth.presentation.welcome

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.rent.core.designsystem.components.buttons.AppButton
import org.example.rent.core.designsystem.components.buttons.AppButtonStyle
import org.example.rent.core.designsystem.components.textFields.AppTextField
import org.example.rent.core.designsystem.theme.AppDimens
import org.example.rent.core.designsystem.theme.AppSpacing
import org.example.rent.core.designsystem.theme.AppTheme
import org.example.rent.core.designsystem.theme.extended
import org.example.rent.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rent.feature.auth.presentation.generated.resources.Res
import rent.feature.auth.presentation.generated.resources.rent_logo_trans

/**
 * Entry point of the auth flow: the app logo, a greeting, and the login options. The phone sign-in
 * flow (phone number → verification code) plays out in place here — the hero stays fixed while the
 * action area below animates between the options, the phone field, and the code field.
 */
@Composable
fun WelcomeRoot(
    onScanQrClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    pendingQrToken: String? = null,
    onQrTokenConsumed: () -> Unit = {},
    viewModel: WelcomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            WelcomeEvent.LoginSuccess -> onLoginSuccess()
            WelcomeEvent.CodeResent -> scope.launch {
                snackbarHostState.showSnackbar("A new code has been sent.")
            }
        }
    }

    // The scanner hands the qrToken back via the nav back-stack; feed it in once, then clear it so
    // it isn't re-applied on recomposition.
    LaunchedEffect(pendingQrToken) {
        if (pendingQrToken != null) {
            viewModel.onAction(WelcomeAction.OnQrTokenReceived(pendingQrToken))
            onQrTokenConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WelcomeScreen(
            state = state,
            onAction = viewModel::onAction,
            onScanQrClick = onScanQrClick,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun WelcomeScreen(
    state: WelcomeState,
    onAction: (WelcomeAction) -> Unit,
    onScanQrClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(Res.drawable.rent_logo_trans),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
        )

        Spacer(Modifier.size(AppSpacing.xl))

        Text(
            text = "Welcome to Rent+",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Sign in to manage your rentals",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )

        Spacer(Modifier.weight(1f))

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                (slideIntoContainer(direction) + fadeIn()) togetherWith
                    (slideOutOfContainer(direction) + fadeOut())
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = AppDimens.formMaxWidth),
            label = "welcomeStep",
        ) { step ->
            when (step) {
                WelcomeStep.OPTIONS -> OptionsContent(
                    onScanQrClick = onScanQrClick,
                    onAction = onAction,
                )

                WelcomeStep.PHONE -> PhoneContent(state = state, onAction = onAction)

                WelcomeStep.CODE -> CodeContent(state = state, onAction = onAction)
            }
        }
    }
}

@Composable
private fun OptionsContent(
    onScanQrClick: () -> Unit,
    onAction: (WelcomeAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        AppButton(
            text = "Scan QR to log in",
            onClick = onScanQrClick,
            style = AppButtonStyle.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
        )

        AppButton(
            text = "Continue with phone",
            onClick = { onAction(WelcomeAction.OnContinueWithPhone) },
            style = AppButtonStyle.OUTLINED,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PhoneContent(
    state: WelcomeState,
    onAction: (WelcomeAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = "Enter your phone number and we'll send you a verification code.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
        )
        AppTextField(
            value = state.phoneNumber,
            onValueChange = { onAction(WelcomeAction.OnPhoneNumberChange(it)) },
            label = "Phone number",
            placeholder = "+1 555 000 1234",
            errorText = state.error?.asString(),
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Send code",
            onClick = { onAction(WelcomeAction.OnSubmitPhoneNumber) },
            isLoading = state.isLoading,
            enabled = state.phoneNumber.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            onClick = { onAction(WelcomeAction.OnBack) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun CodeContent(
    state: WelcomeState,
    onAction: (WelcomeAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = if (state.qrToken != null) {
                "We sent a code to your WhatsApp. Enter it below to sign in."
            } else {
                "We sent a code to ${state.phoneNumber}. Enter it below to sign in."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
        )
        AppTextField(
            value = state.code,
            onValueChange = { onAction(WelcomeAction.OnCodeChange(it)) },
            label = "Verification code",
            placeholder = "123456",
            errorText = state.error?.asString(),
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Verify",
            onClick = { onAction(WelcomeAction.OnSubmitCode) },
            isLoading = state.isLoading,
            enabled = state.code.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            onClick = { onAction(WelcomeAction.OnResendCode) },
            enabled = !state.isLoading && !state.isResending,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isResending) "Sending…" else "Resend code")
        }
        TextButton(
            onClick = { onAction(WelcomeAction.OnBack) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back")
        }
    }
}

@Preview
@Composable
private fun WelcomeOptionsPreview() {
    AppTheme {
        WelcomeScreen(
            state = WelcomeState(step = WelcomeStep.OPTIONS),
            onAction = {},
            onScanQrClick = {},
        )
    }
}

@Preview
@Composable
private fun WelcomePhoneStepDarkPreview() {
    AppTheme(darkTheme = true) {
        WelcomeScreen(
            state = WelcomeState(step = WelcomeStep.PHONE, phoneNumber = "+1 555 000 1234"),
            onAction = {},
            onScanQrClick = {},
        )
    }
}
