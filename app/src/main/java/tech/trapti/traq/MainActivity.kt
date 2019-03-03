package tech.trapti.traq

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.webkit.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var host : String = "q.trap.jp"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(NotificationService.CHANNEL_ID_NORMAL, "普通のチャンネル", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel1)
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        10)
            }
        }



        val token = PreferenceManager.getDefaultSharedPreferences(this.applicationContext).getString("FCMToken", "No Token")
        Log.d("traq-debug", token)

        WebView.setWebContentsDebuggingEnabled(true)

        val webView = findViewById(R.id.webView) as WebView


        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.d("traq-debug", request.toString())
                if (request!!.url.host!!.contains(host)) {
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
                if (Uri.parse(url).host.contains(host)) {
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

        webView.webChromeClient = object: WebChromeClient() {
            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback
                if (fileChooserParams == null) return false
                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, 10)
                } catch (e: Exception) {
                    uploadMessage = null
                    return false
                }
                return true
            }
        }

        webView.settings.databaseEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(false)

        webView.settings.setAppCacheEnabled(true)

        webView.settings.userAgentString += " traQ-Android"


        webView.addJavascriptInterface(Bridge(token), "Bridge")
        webView.loadUrl("https://" + host)
    }

    override fun onResume() {
        super.onResume()

        val webView = findViewById(R.id.webView) as WebView

        val path = intent.getStringExtra("path")
        if (path != null) {
            webView.loadUrl("https://$host$path")
        }

        val channelID = intent.getStringExtra("tag")
        if (channelID != null) {
            NotificationService.notificationIDMap.remove(channelID)
            NotificationService.notificationMap.remove(channelID)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 10) {
            if (uploadMessage == null) {
                return
            }
            uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            )
            uploadMessage = null
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

class Bridge {
    companion object {
      public var token:String = ""
    }
    constructor(_token :String) {
        token = _token
    }
    @JavascriptInterface
    fun getFCMToken() : String {
        return token
    }
}
