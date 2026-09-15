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
}
