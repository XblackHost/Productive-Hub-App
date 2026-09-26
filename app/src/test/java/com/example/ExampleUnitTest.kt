package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun verifyRestrictedPackageNames() {
        // Must strictly be YouTube and Instagram only
        assertEquals("com.google.android.youtube", ShortsReelsBlockerService.PACKAGE_YOUTUBE)
        assertEquals("com.instagram.android", ShortsReelsBlockerService.PACKAGE_INSTAGRAM)
    }

    @Test
    fun verifyDefaultState() {
        val initialStats = FocusPreferences.statsFlow.value
        assertTrue(initialStats.isBlockingEnabled)
        assertEquals(0, initialStats.youtubeLimitMinutes)
        assertEquals(0, initialStats.instagramLimitMinutes)
        assertEquals(0, initialStats.todayBlocks)
        assertEquals(0, initialStats.totalBlocks)
        assertFalse(initialStats.isAdminLockEnabled)
    }

    @Test
    fun verifyRecoveryKeyFormat() {
        val key = FocusPreferences.generateNewRecoveryKey()
        assertTrue("Key should start with FH- prefix", key.startsWith("FH-"))
        assertEquals("Key length should be 12 (FH-XXXX-XXXX)", 12, key.length)
        assertEquals('-', key[2])
        assertEquals('-', key[7])
    }

    @Test
    fun verifyHatifSecurityQuestionAnswer() {
        // Exact Question: "What does Hatif Wants?"
        // Correct Answer: "hope"
        assertEquals("What does Hatif Wants?", HatifSecurityManager.DEFAULT_SECURITY_QUESTION)
        assertTrue(HatifSecurityManager.verifySecurityQuestion("hope"))
        assertTrue(HatifSecurityManager.verifySecurityQuestion("  HOPE  "))
        assertFalse(HatifSecurityManager.verifySecurityQuestion("freedom"))
    }

    @Test
    fun verifyAiReflectionExportFormatting() {
        val exportText = ReflectionRepository.generateAiExportText()
        assertTrue(exportText.contains("HATIF WORKSPACE: 30-DAY SELF-REFLECTION REPORT"))
        assertTrue(exportText.contains("[SYSTEM PROMPT FOR AI EVALUATION]"))
        assertTrue(exportText.contains("ChatGPT, Claude, or Gemini"))
    }
}
