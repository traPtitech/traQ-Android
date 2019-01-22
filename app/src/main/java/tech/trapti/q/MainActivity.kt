package tech.trapti.q

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.customtabs.CustomTabsIntent
import android.util.Log
import android.webkit.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(NotificationService.CHANNEL_ID_NORMAL, "普通のチャンネル", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel1)
        }



        val token = PreferenceManager.getDefaultSharedPreferences(this.applicationContext).getString("FCMToken", "No Token")
        Log.d("traq-debug", token)

        WebView.setWebContentsDebuggingEnabled(true)

        val webView = findViewById(R.id.webView) as WebView

        webView.loadUrl("https://traq-dev.tokyotech.org")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val host = request!!.url.host
                Log.d("traq-debug", request.toString())
                if (host.contains("traq-dev.tokyotech.org")) {
                    return false
                } else {
                    val builder = CustomTabsIntent.Builder()
                    builder.addDefaultShareMenuItem()

                    val customTabsIntent = builder.build()

                    customTabsIntent.launchUrl(applicationContext, request.url)
                    return true
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d("traq-debug", url)
                if (Uri.parse(url).host.contains("traq-dev.tokyotech.org")) {
                    return false
                } else {
                    val builder = CustomTabsIntent.Builder()
                    builder.addDefaultShareMenuItem()

                    val customTabsIntent = builder.build()

                    customTabsIntent.launchUrl(applicationContext, Uri.parse(url))
                    return true
                }
            }
        }

        webView.settings.databaseEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.setSupportZoom(false)

        webView.settings.setAppCacheEnabled(true)
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        webView.settings.userAgentString += " traQ-Android"


        webView.addJavascriptInterface(Bridge(token), "Bridge")
    }

    override fun onResume() {
        super.onResume()

        val webView = findViewById(R.id.webView) as WebView

        val path = intent.getStringExtra("path")
        if (path != null) {
            webView.loadUrl("https://traq-dev.tokyotech.org$path")
        }

        val channelID = intent.getStringExtra("tag")
        if (channelID != null) {
            NotificationService.notificationIDMap.remove(channelID)
            NotificationService.notificationMap.remove(channelID)
        }

    }
}

class Bridge {
    var token:String
    constructor(token :String) {
        this.token = token
    }
    @JavascriptInterface
    fun getFCMToken() : String {
        return token
    }
}
