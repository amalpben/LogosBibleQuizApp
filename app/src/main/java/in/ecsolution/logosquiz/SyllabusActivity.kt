package `in`.ecsolution.logosquiz

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SyllabusActivity : AppCompatActivity() {
    private lateinit var clickSound: MediaPlayer
    private lateinit var btnHome: ImageButton
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_syllabus)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        clickSound = MediaPlayer.create(this, R.raw.click_sound)
        //Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GlobalValues.isActivityTransition=true
                val intent = Intent(this@SyllabusActivity, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        btnHome = findViewById(R.id.homeButton)
        btnHome.setOnClickListener {
            playClickSound()
            GlobalValues.isActivityTransition=true
            val intent = Intent(this@SyllabusActivity, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
        generateCards()
    }
    private fun playClickSound() {
        if (GlobalValues.soundOn == true) {
            clickSound.start()
        }
    }
    private fun generateCards(){
        val dbHelper = QuizDbHelper.getInstance(this@SyllabusActivity)

        // Get the LinearLayout where content will be added
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutContent)

        val syllabusList = dbHelper.getLogosSyllabus() // Assume this returns List<Pair<String, String>>

        for (syllabus in syllabusList) {
            val bookName = syllabus.first
            val chapters = syllabus.second

            val numbersList = chapters.split(",")
            val firstNumber = numbersList.first()
            val lastNumber = numbersList.last()
            val result = "$firstNumber - $lastNumber"

            // Create LinearLayout for each card
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20.dp, 32.dp, 20.dp, 32.dp)
                setBackgroundResource(R.drawable.bg_semiblack_card) // Set the background

                // Set layout params with margin
                layoutParams = LinearLayout.LayoutParams(220.dp,250.dp
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 16.dp
                    marginEnd = 16.dp
                }
                gravity = Gravity.CENTER // Set gravity on the entire card layout
            }

            // Create TextView for the book name and chapter range
            val textView = TextView(this).apply {
                text = "$bookName\nഅദ്ധ്യായം $result"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.dp) // Bottom margin
                }
                setTextColor(ContextCompat.getColor(this@SyllabusActivity, R.color.white)) // Text color
                textSize = 16f // Text size in sp
                typeface = ResourcesCompat.getFont(this@SyllabusActivity, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER // Text alignment
            }
            // Create ImageView
            val imageView = ImageView(this).apply {
                setImageResource(R.drawable.big_book) // Image source
                layoutParams = LinearLayout.LayoutParams(130.dp, 83.dp)
                // The gravity is already handled by the parent LinearLayout, so no need to set it again here
            }
            // Add TextView and ImageView to the card layout
            cardLayout.addView(textView)
            cardLayout.addView(imageView)

            // Add the card layout to the main LinearLayout
            linearLayout.addView(cardLayout)
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