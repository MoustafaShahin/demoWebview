package com.like.likecard.webViewProject

import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.like.likecard.webViewProject.databinding.ActivityMainBinding
import com.like.likecard.webViewProject.databinding.DialogConfirmExitBinding
import java.io.File
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val chromeClient = AppChromeClient(WeakReference(this))
    private var contentLauncher: ActivityResultLauncher<String> = getMultipleContentLauncher()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val receivedString = intent.getStringExtra("url")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        /*     @Suppress("DEPRECATION")
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                 window.insetsController?.hide(WindowInsets.Type.statusBars())
             } else {
       window.setFlags(
                 WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN)
             }*/

        binding.web.settings.javaScriptEnabled = true
        binding.web.evaluateJavascript(
            "javascript:navigator.clipboard.writeText = (msg) => { return Android.writeToClipboard(msg); }",
            null
        )
        binding.web.addJavascriptInterface(WebAppInterface(this), "NativeAndroid");

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

        binding.web.loadUrl(receivedString.toString())

        binding.web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: String): Boolean {
                if (request.contains("exit")) {
                    binding.web.destroy()
                    finish()
                }
                return false
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String
            ) {
                view.apply {
                    File(context.cacheDir, "org.chromium.android_webview").let {
                        if (!it.exists() || it.listFiles()?.size ?: 0 < 5) {
                            // not cached
                        }
                    }
                }
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView,
                req: WebResourceRequest,
                rerr: WebResourceError
            ) {
                onReceivedError(
                    view,
                    rerr.errorCode,
                    rerr.description?.toString(),
                    req.url.toString()
                )
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
            openConfirmExitDialog()
            //super.onBackPressed()
        }
    }

    var alertDialog: AlertDialog? = null
    private fun openConfirmExitDialog() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (alertDialog != null) if (alertDialog!!.isShowing()) return


        alertDialog = AlertDialog.Builder(this).create()

        alertDialog!!.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_btn_primary)
        val confirmBinding: DialogConfirmExitBinding = DialogConfirmExitBinding.inflate(inflater)

        alertDialog!!.setView(confirmBinding.root)

        alertDialog!!.setCancelable(false)
        alertDialog!!.show()

        confirmBinding.title.setText("Exit App")
        confirmBinding.contant.setText("Are You Sure You Want To Exit?")
        confirmBinding.confirmButton.setText("Ok")
        confirmBinding.confirmButton.setOnClickListener { view1 ->
            binding.web.destroy()
            finish()
        }


        confirmBinding.cancelButton.setOnClickListener { view1 -> alertDialog!!.dismiss() }

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

class WebAppInterface(val context: Context) {
    @JavascriptInterface
    fun copyToClipboard(text: String?) {
        val clipboard: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("demo", text)
        clipboard?.setPrimaryClip(clip)
    }

    @JavascriptInterface
    fun endWebView() {
        (context as MainActivity).binding.web.destroy()
        (context as MainActivity).finish()

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
        if (fileChooserParams?.acceptTypes?.contains(".jpg") == true ||
            fileChooserParams?.acceptTypes?.contains(".jpeg") == true ||
            fileChooserParams?.acceptTypes?.contains(".png") == true
        ) {
            webViewFragment.launchGetMultipleContents("image/*")
        } else {
            webViewFragment.launchGetMultipleContents("video/*")

        }

        return true
    }

    fun receiveFileCallback(result: Array<Uri>) {
        openFileCallback?.onReceiveValue(result)
        openFileCallback = null
    }
}
