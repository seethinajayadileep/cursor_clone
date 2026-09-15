package com.seethinajayadileep.cursoragents

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
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

    @Test
    fun trustedMediaOriginUriOverloadUsesSchemeAndHost() {
        assertFalse(AgentsUrls.isTrustedMediaOrigin(null as Uri?))
        assertTrue(AgentsUrls.isTrustedMediaOrigin(Uri.parse("https://cursor.com/agents")))
        assertTrue(AgentsUrls.isTrustedMediaOrigin(Uri.parse("https://www.cursor.com")))
        assertFalse(AgentsUrls.isTrustedMediaOrigin(Uri.parse("http://cursor.com/agents")))
        assertFalse(AgentsUrls.isTrustedMediaOrigin(Uri.parse("https://github.com")))
        assertFalse(AgentsUrls.isTrustedMediaOrigin(Uri.parse("https://cursor.com:4443/agents")))
        assertTrue(AgentsUrls.isTrustedMediaOrigin(Uri.parse("https://cursor.com:443/agents")))
    }

    @Test
    fun resolveIncomingDeepLinkAcceptsOnlyHttpsAgentsPaths() {
        val agents = "https://cursor.com/agents"
        val nested = "https://cursor.com/agents/abc"
        assertEquals(
            agents,
            AgentsUrls.resolveIncomingDeepLink("https", "cursor.com", "/agents", agents)
        )
        assertEquals(
            nested,
            AgentsUrls.resolveIncomingDeepLink("https", "cursor.com", "/agents/abc", nested)
        )
        assertEquals(
            AgentsUrls.HOME,
            AgentsUrls.resolveIncomingDeepLink("http", "cursor.com", "/agents", "http://cursor.com/agents")
        )
        assertEquals(
            AgentsUrls.HOME,
            AgentsUrls.resolveIncomingDeepLink("https", "cursor.com", "/", "https://cursor.com/")
        )
        assertEquals(
            AgentsUrls.HOME,
            AgentsUrls.resolveIncomingDeepLink(
                "https",
                "cursor.com",
                "/agentship",
                "https://cursor.com/agentship"
            )
        )
        assertEquals(
            AgentsUrls.HOME,
            AgentsUrls.resolveIncomingDeepLink("https", "github.com", "/agents", "https://github.com/agents")
        )
        assertEquals(
            AgentsUrls.HOME,
            AgentsUrls.resolveIncomingDeepLink(
                "https",
                "cursor.com",
                "/agents/../account",
                "https://cursor.com/agents/../account"
            )
        )
        assertEquals(
            AgentsUrls.HOME,
            AgentsUrls.resolveIncomingDeepLink(
                scheme = "https",
                host = "cursor.com",
                path = "/agents",
                originalUrl = "https://cursor.com:4443/agents",
                port = 4443
            )
        )
    }
}
