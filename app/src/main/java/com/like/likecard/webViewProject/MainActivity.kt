package com.like.likecard.webViewProject

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.like.likecard.webViewProject.databinding.ActivityMainBinding
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val chromeClient = AppChromeClient(WeakReference(this))
    private var contentLauncher: ActivityResultLauncher<String> = getMultipleContentLauncher()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root )
        binding.web.settings.javaScriptEnabled = true
        binding.web.setInitialScale(1);
        binding.web.getSettings().setLoadWithOverviewMode(true);
        binding.web.getSettings().setUseWideViewPort(true);
        binding.web.getSettings().setDomStorageEnabled(true);
        binding.web.getSettings().setAllowFileAccess(true);
        binding.web.getSettings().setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            binding.web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //FOR WEBPAGE SLOW UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            binding.web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            binding.web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        binding.web.webChromeClient = chromeClient
        binding.web.loadUrl("https://challengexspace.fra1.digitaloceanspaces.com/ChallengeX/index.html")

        binding.web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: String): Boolean {
                return false
            }
            override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String) {
                view.apply {
                    File(context.cacheDir, "org.chromium.android_webview").let {
                        if (!it.exists() || it.listFiles()?.size ?: 0 < 5) {
                            // not cached
                        }
                    }
                }
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView, req: WebResourceRequest, rerr: WebResourceError) {
                onReceivedError(view, rerr.errorCode, rerr.description?.toString(), req.url.toString())
            }
        }

    }

    override fun onStart() {
        super.onStart()


    }

    override fun onDestroy() {
       // binding.web.destroy()

        super.onDestroy()
    }
    override fun onBackPressed() {
        if (binding.web.canGoBack()) {
            binding.web.goBack()
        } else {
            binding
            super.onBackPressed()
        }
    }
    private fun getMultipleContentLauncher(): ActivityResultLauncher<String> {
        return this.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { list ->
            if (list.isEmpty()) {
            }
            chromeClient.receiveFileCallback(list.toTypedArray())
        }
    }


    fun launchGetMultipleContents(type: String) {
        contentLauncher.launch(type)
    }
}
class AppChromeClient(private val fragmentWeakReference: WeakReference<MainActivity>) :
    WebChromeClient() {
    private var openFileCallback: ValueCallback<Array<Uri>>? = null

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (filePathCallback == null) {
            return (super.onShowFileChooser(webView, filePathCallback, fileChooserParams))
        }
        openFileCallback = filePathCallback
        val webViewFragment = fragmentWeakReference.get() ?: return false
        if (fileChooserParams?.acceptTypes?.contains(".jpg")==true ||
            fileChooserParams?.acceptTypes?.contains(".jpeg") ==true ||
            fileChooserParams?.acceptTypes?.contains(".png")== true)
        {
            webViewFragment.launchGetMultipleContents("image/*")
        }else{
            webViewFragment.launchGetMultipleContents("video/*")

        }

        return true
    }

    fun receiveFileCallback(result: Array<Uri>) {
        openFileCallback?.onReceiveValue(result)
        openFileCallback = null
    }
}