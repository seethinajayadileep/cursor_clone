package com.seethinajayadileep.cursoragents

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class AgentsWebViewClient(
    private val onPageStarted: () -> Unit,
    private val onPageFinished: (url: String?) -> Unit,
    private val onError: (description: String) -> Unit,
    private val shouldOpenExternally: (url: String) -> Boolean
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        val url = uri.toString()
        if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("intent:")) {
            shouldOpenExternally(url)
            return true
        }
        val host = uri.host
        if (!AgentsUrls.isAllowedHost(host) && shouldOpenExternally(url)) {
            return true
        }
        return false
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        onPageStarted()
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        onPageFinished(url)
        super.onPageFinished(view, url)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (request.isForMainFrame) {
            onError(error.description?.toString() ?: "Unable to load Cursor Agents")
        }
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.cancel()
        onError("Secure connection failed")
    }
}
