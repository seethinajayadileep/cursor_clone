# Keep WebView / JS interfaces intact if minify is enabled later.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
