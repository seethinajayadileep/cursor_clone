package com.seethinajayadileep.cursoragents

import android.net.Uri
import android.os.Message
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class AgentsChromeClient(
    private val onProgress: (Int) -> Unit,
    private val onPermissionRequest: (PermissionRequest) -> Unit,
    private val onFileChooser: (
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams
    ) -> Boolean,
    private val onCreateWindow: (WebView, Message?) -> Boolean
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        onProgress(newProgress)
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        onPermissionRequest.invoke(request)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams
    ): Boolean {
        return onFileChooser(filePathCallback, fileChooserParams)
    }

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        return onCreateWindow(view, resultMsg)
    }
}
