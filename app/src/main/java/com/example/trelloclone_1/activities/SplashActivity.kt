package com.example.trelloclone_1.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.trelloclone_1.databinding.ActivitySplashBinding
import com.example.trelloclone_1.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val typeFace: Typeface = Typeface.createFromAsset(assets, "OleoScriptSwashCaps-Bold.ttf")
        binding.tvAppName.typeface = typeFace

        Handler().postDelayed({
            var currentUserId = FirestoreClass().getCurrentUserId()
            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java) )
                finish()
            }
            else{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        }, 2000)
    }

}