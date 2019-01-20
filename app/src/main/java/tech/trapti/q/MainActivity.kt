package tech.trapti.q

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

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
        webView.settings.databaseEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.setSupportZoom(false)
        webView.settings.userAgentString += " traQ-Android"

        webView.webViewClient = object: WebViewClient() {
        }

        webView.loadUrl("https://traq-dev.tokyotech.org")

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
