package tech.trapti.q

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById(R.id.webView) as WebView
        webView.settings.databaseEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.setSupportZoom(false)
        webView.loadUrl("https://traq-dev.tokyotech.org")
    }
}
