package com.seethinajayadileep.cursoragents

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.seethinajayadileep.cursoragents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null
    private var pendingPermissionRequest: PermissionRequest? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = filePathCallback
        filePathCallback = null
        if (callback == null) return@registerForActivityResult

        val uris = mutableListOf<Uri>()
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val clip = data?.clipData
            when {
                clip != null -> {
                    for (index in 0 until clip.itemCount) {
                        clip.getItemAt(index).uri?.let(uris::add)
                    }
                }
                data?.data != null -> uris.add(data.data!!)
                cameraImageUri != null -> uris.add(cameraImageUri!!)
            }
        }
        cameraImageUri = null
        callback.onReceiveValue(if (uris.isEmpty()) null else uris.toTypedArray())
    }

    private val runtimePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val request = pendingPermissionRequest ?: return@registerForActivityResult
        pendingPermissionRequest = null
        val allowed = request.resources.filter { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                    granted[Manifest.permission.CAMERA] == true
                PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                    granted[Manifest.permission.RECORD_AUDIO] == true
                else -> true
            }
        }
        if (allowed.isEmpty()) {
            request.deny()
        } else {
            request.grant(allowed.toTypedArray())
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        configureWebView()
        binding.swipeRefresh.setOnRefreshListener {
            binding.webView.reload()
        }
        binding.retryButton.setOnClickListener {
            showError(false)
            binding.webView.reload()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            binding.webView.loadUrl(deepLinkOrHome())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = deepLinkOrHome()
        if (url != AgentsUrls.HOME || binding.webView.url.isNullOrBlank()) {
            binding.webView.loadUrl(url)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onPause() {
        CookieManager.getInstance().flush()
        super.onPause()
    }

    private fun deepLinkOrHome(): String {
        val incoming = intent?.data?.toString()
        return if (!incoming.isNullOrBlank() && AgentsUrls.isAllowedHost(intent?.data?.host)) {
            incoming
        } else {
            AgentsUrls.HOME
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = chromeUserAgent(userAgentString)
        }

        binding.webView.webViewClient = AgentsWebViewClient(
            onPageStarted = {
                binding.progressBar.isVisible = true
            },
            onPageFinished = {
                binding.progressBar.isVisible = false
                binding.swipeRefresh.isRefreshing = false
                showError(false)
            },
            onError = { message ->
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.isVisible = false
                binding.errorMessage.text = message
                showError(true)
            },
            shouldOpenExternally = { url ->
                openExternally(url)
                true
            }
        )

        binding.webView.webChromeClient = AgentsChromeClient(
            onProgress = { progress ->
                binding.progressBar.progress = progress
                binding.progressBar.isVisible = progress in 1..99
            },
            onPermissionRequest = ::handlePermissionRequest,
            onFileChooser = { callback, params ->
                launchFileChooser(callback, params)
            },
            onCreateWindow = { _, resultMsg ->
                handlePopup(resultMsg)
            }
        )
        binding.webView.setDownloadListener(AgentsDownloadListener(this))
        binding.webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.swipeRefresh.isEnabled = scrollY == 0
        }
    }

    private fun chromeUserAgent(defaultAgent: String): String {
        // WebView adds "; wv" which some OAuth providers reject. Keep a Chrome UA.
        return defaultAgent.replace("; wv", "")
    }

    private fun handlePermissionRequest(request: PermissionRequest) {
        val needed = mutableListOf<String>()
        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in request.resources) {
            needed += Manifest.permission.CAMERA
        }
        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in request.resources) {
            needed += Manifest.permission.RECORD_AUDIO
        }
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            request.grant(request.resources)
            return
        }
        pendingPermissionRequest = request
        runtimePermissionLauncher.launch(missing.toTypedArray())
    }

    private fun launchFileChooser(
        callback: ValueCallback<Array<Uri>>?,
        params: WebChromeClient.FileChooserParams
    ): Boolean {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = callback

        val contentIntent = params.createIntent()
        val chooserIntents = mutableListOf<Intent>()

        if (params.acceptTypes.any { it.contains("image") || it == "*/*" || it.isBlank() } ||
            params.acceptTypes.isEmpty()
        ) {
            cameraImageUri = cacheImageUri(this)
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            chooserIntents += cameraIntent
        }

        val chooser = Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, contentIntent)
            putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_file))
            if (chooserIntents.isNotEmpty()) {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, chooserIntents.toTypedArray())
            }
        }
        return try {
            fileChooserLauncher.launch(chooser)
            true
        } catch (_: ActivityNotFoundException) {
            filePathCallback = null
            false
        }
    }

    private fun handlePopup(resultMsg: Message?): Boolean {
        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
        transport.webView = binding.webView
        resultMsg.sendToTarget()
        return true
    }

    private fun openExternally(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(show: Boolean) {
        binding.errorContainer.visibility = if (show) View.VISIBLE else View.GONE
        binding.webView.isVisible = !show
    }
}
