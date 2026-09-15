package com.seethinajayadileep.cursoragents

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentsUrlsTest {
    @Test
    fun deepLinkAllowsCursorAgentsHostOnly() {
        assertTrue(AgentsUrls.isDeepLinkHost("cursor.com"))
        assertTrue(AgentsUrls.isDeepLinkHost("www.cursor.com"))
        assertFalse(AgentsUrls.isDeepLinkHost("github.com"))
        assertFalse(AgentsUrls.isDeepLinkHost("accounts.google.com"))
    }

    @Test
    fun navigationAllowlistStillIncludesOauthHosts() {
        assertTrue(AgentsUrls.isAllowedHost("accounts.google.com"))
        assertTrue(AgentsUrls.isAllowedHost("authenticator.cursor.sh"))
    }
}
