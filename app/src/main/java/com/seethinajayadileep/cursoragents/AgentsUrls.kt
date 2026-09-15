package com.seethinajayadileep.cursoragents

object AgentsUrls {
    const val HOME = "https://cursor.com/agents"

    private val allowedHosts = setOf(
        "cursor.com",
        "www.cursor.com",
        "cursor.sh",
        "google.com",
        "apple.com",
        "github.com",
        "gitlab.com",
        "microsoftonline.com",
        "microsoft.com",
        "live.com",
        "auth0.com",
        "workos.com"
    )

    fun isAllowedHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val normalized = host.lowercase()
        return allowedHosts.any { normalized == it || normalized.endsWith(".$it") }
    }

    fun isDeepLinkHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val normalized = host.lowercase()
        return normalized == "cursor.com" || normalized == "www.cursor.com"
    }

    fun isTrustedMediaOrigin(origin: android.net.Uri?): Boolean {
        if (origin == null) return false
        return isTrustedMediaOrigin(origin.scheme, origin.host)
    }

    fun isTrustedMediaOrigin(scheme: String?, host: String?): Boolean {
        if (!scheme.equals("https", ignoreCase = true)) return false
        if (host.isNullOrBlank()) return false
        val normalized = host.lowercase()
        return normalized == "cursor.com" || normalized.endsWith(".cursor.com")
    }

    fun resolveIncomingDeepLink(
        scheme: String?,
        host: String?,
        path: String?,
        originalUrl: String?
    ): String {
        val normalizedPath = path.orEmpty()
        val isAgentsPath = normalizedPath == "/agents" || normalizedPath.startsWith("/agents/")
        return if (
            !originalUrl.isNullOrBlank() &&
            scheme.equals("https", ignoreCase = true) &&
            isDeepLinkHost(host) &&
            isAgentsPath
        ) {
            originalUrl
        } else {
            HOME
        }
    }
}
