package com.example.nativemicrosite

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MicrositeWebView(
    micrositeUrl: String,
    token: String,
    onTokenExpired: () -> Unit,
    onCloseWebView: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onCloseWebView()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.loadsImagesAutomatically = true
                settings.domStorageEnabled = true
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

//                WebView.setWebContentsDebuggingEnabled(true)

                addJavascriptInterface(TokenJsInterface(
                    onTokenExpired,
                    onCloseWebView
                ), "Android")

//                webViewClient = WebViewClient()

                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.grant(request.resources)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                        val headers = mapOf(
                            "USER-TOKEN" to token
                        )
                        if (url != null) {
                            view?.loadUrl(url, headers)
                        }
                        return true
                    }
                }

                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                loadMicrosite(this, micrositeUrl, token)
                webViewRef = this
            }
        },
        update = { webView ->
            // No need to reload microsite on every recomposition which resets webView history
        }
    )
}

fun loadMicrosite(webView: WebView, url: String, token: String) {
    val headers = mapOf(
        "USER-TOKEN" to token
    )
    // for Banglalink
    webView.loadUrl(url, headers)
    println("url: $url")
    //for bkash
//    webView.loadUrl("$url/$token",)
}

class TokenJsInterface(
    private val onTokenExpired: () -> Unit,
    private val onCloseWebView: () -> Unit
) {
    @JavascriptInterface
    fun tokenStatus(status: String, message: String) {
        // Optional: log message
        println("WebView tokenStatus: $status | $message")

        if (status == "Y") {
            // for Banlalink. comment the line for bkash
            onTokenExpired.invoke()
        }

    }

    @JavascriptInterface
    fun closeWebView() {

        Log.d(
            "Native Microsite",
            "JavaScript requested to close WebView"
        )
        onCloseWebView.invoke()
    }
}
