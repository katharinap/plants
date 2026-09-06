package com.katharina.plants

import org.junit.Assert.assertNotNull
import org.junit.Test

class ConfigTest {
    @Test
    fun `PLANTNET_API_KEY is defined`() {
        assertNotNull(BuildConfig.PLANTNET_API_KEY)
    }
}
