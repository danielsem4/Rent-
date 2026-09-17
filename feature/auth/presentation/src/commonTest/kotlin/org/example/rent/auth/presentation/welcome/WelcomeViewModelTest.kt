package org.example.rent.auth.presentation.welcome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resend on the phone flow sends the phone identifier only`() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = WelcomeViewModel(repo)

        viewModel.onAction(WelcomeAction.OnPhoneNumberChange("0501234567"))
        viewModel.onAction(WelcomeAction.OnResendCode)

        assertEquals(1, repo.resendCallCount)
        assertEquals("0501234567", repo.lastResendPhone)
        assertNull(repo.lastResendQrToken)
    }

    @Test
    fun `resend on the qr flow sends the qrToken identifier only`() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = WelcomeViewModel(repo)

        val token = "a1b2c3d4e5f60718a1b2c3d4e5f60718"
        viewModel.onAction(WelcomeAction.OnQrTokenReceived(token))
        viewModel.onAction(WelcomeAction.OnResendCode)

        assertEquals(1, repo.resendCallCount)
        assertEquals(token, repo.lastResendQrToken)
        assertNull(repo.lastResendPhone)
    }
}
