package com.like.likecard.webViewProject

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.like.likecard.webViewProject.databinding.ActivityIntroBinding
import com.like.likecard.webViewProject.databinding.ActivityMainBinding

class IntroActivity : AppCompatActivity() {
    lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etUrl.setText(SharedPrefManager.getUrl(this))
        binding.btnGo.setOnClickListener {
            SharedPrefManager.setUrl(binding.etUrl.text.toString(), this)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("url", binding.etUrl.text.toString())
            startActivity(intent)
        }

    }
}