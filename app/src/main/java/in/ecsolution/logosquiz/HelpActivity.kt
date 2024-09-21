package `in`.ecsolution.logosquiz

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class HelpActivity : AppCompatActivity() {
    private lateinit var clickSound: MediaPlayer
    private lateinit var btnHome: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        clickSound = MediaPlayer.create(this, R.raw.click_sound)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press handling
                GlobalValues.isActivityTransition=true
                val intent = Intent(this@HelpActivity, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        btnHome = findViewById(R.id.homeButton)
        btnHome.setOnClickListener {
            playClickSound()
            GlobalValues.isActivityTransition=true
            val intent = Intent(this@HelpActivity, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun playClickSound() {
        if (GlobalValues.soundOn == true) {
            clickSound.start()
        }
    }
    override fun onPause() {
        super.onPause()
        if (this::clickSound.isInitialized) {
            clickSound.release()
        }
        if (!GlobalValues.isActivityTransition) {
            stopService(Intent(this, MusicService::class.java))
        }
        GlobalValues.isActivityTransition = false
    }
    override fun onResume() {
        super.onResume()
        if(GlobalValues.musicOn==true){
            startService(Intent(this, MusicService::class.java))
        }
        else{
            stopService(Intent(this, MusicService::class.java))
        }
    }
}