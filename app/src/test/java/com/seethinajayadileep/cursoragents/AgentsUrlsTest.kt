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

    @Test
    fun trustedMediaOriginAllowsHttpsCursorHostsOnly() {
        assertTrue(AgentsUrls.isTrustedMediaOrigin("https", "cursor.com"))
        assertTrue(AgentsUrls.isTrustedMediaOrigin("HTTPS", "www.cursor.com"))
        assertTrue(AgentsUrls.isTrustedMediaOrigin("https", "agents.cursor.com"))
        assertFalse(AgentsUrls.isTrustedMediaOrigin("http", "cursor.com"))
        assertFalse(AgentsUrls.isTrustedMediaOrigin("https", "cursor.com.evil.example"))
        assertFalse(AgentsUrls.isTrustedMediaOrigin("https", "notcursor.com"))
        assertFalse(AgentsUrls.isTrustedMediaOrigin("https", "github.com"))
        assertFalse(AgentsUrls.isTrustedMediaOrigin(null, "cursor.com"))
        assertFalse(AgentsUrls.isTrustedMediaOrigin("https", null))
    }
}
