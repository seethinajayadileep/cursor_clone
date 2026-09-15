package com.seethinajayadileep.cursoragents

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class AgentsDownloadListener(
    private val activity: MainActivity
) : DownloadListener {
    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        try {
            val request = android.app.DownloadManager.Request(Uri.parse(url))
            val cookies = CookieManager.getInstance().getCookie(url)
            if (!cookies.isNullOrBlank()) {
                request.addRequestHeader("Cookie", cookies)
            }
            request.addRequestHeader("User-Agent", userAgent)
            request.setMimeType(mimeType)
            request.setNotificationVisibility(
                android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            val manager = activity.getSystemService(android.app.DownloadManager::class.java)
            manager.enqueue(request)
            Toast.makeText(activity, R.string.download_started, Toast.LENGTH_SHORT).show()
        } catch (error: Exception) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(activity, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun cacheImageUri(activity: MainActivity): Uri {
    val dir = File(activity.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        activity,
        "${activity.packageName}.fileprovider",
        file
    )
}
