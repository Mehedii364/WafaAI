package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.security.KeySecurityHelper
import com.example.domain.model.ApiKeyConfig
import com.example.domain.model.KeyStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Wafa AI", appName)
    }

    @Test
    fun `test key masking logic`() {
        val rawKey = "sk-or-v1-abcdef1234567890abcdef1234567890"
        val masked = KeySecurityHelper.maskKey(rawKey)
        assertTrue(masked.startsWith("sk-or-v1-••••••••"))
        assertTrue(masked.endsWith("7890"))
    }

    @Test
    fun `test ApiKeyConfig ready status check`() {
        val emptyConfig = ApiKeyConfig(slotIndex = 1, label = "API Key 01", hasKey = false)
        assertFalse(emptyConfig.isReadyToUse)

        val readyConfig = ApiKeyConfig(
            slotIndex = 1,
            label = "API Key 01",
            hasKey = true,
            status = KeyStatus.AVAILABLE
        )
        assertTrue(readyConfig.isReadyToUse)

        val invalidConfig = ApiKeyConfig(
            slotIndex = 1,
            label = "API Key 01",
            hasKey = true,
            status = KeyStatus.INVALID
        )
        assertFalse(invalidConfig.isReadyToUse)
    }
}
