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

class LogosBookShow : AppCompatActivity() {

    private lateinit var clickSound: MediaPlayer
    private lateinit var btnHome: ImageButton
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private lateinit var dbHelper: QuizDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logos_book_show)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

//        sharedPreferences = getSharedPreferences("login_data", MODE_PRIVATE)
        clickSound = MediaPlayer.create(this, R.raw.click_sound)
        //Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GlobalValues.isActivityTransition=true
                val intent = Intent(this@LogosBookShow, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        btnHome = findViewById(R.id.homeButton)
        btnHome.setOnClickListener {
            playClickSound()
            GlobalValues.isActivityTransition=true
            val intent = Intent(this@LogosBookShow, HomePageActivity::class.java)
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
        dbHelper = QuizDbHelper.getInstance(this)

        // Get the LinearLayout where content will be added
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutContent)

        val syllabusList = dbHelper.getLogosSyllabus() // Assume this returns List<Pair<String, String>>
        val dataForLogosBook=dbHelper.getDataForLogosBook()
        for (syllabus in syllabusList) {
            val bookNo=syllabus.third
            val bookName = syllabus.first
            val chapters = syllabus.second
            val dataLogosEachBook=dataForLogosBook[bookNo]
            val numbersList = chapters.split(",")
            val firstNumber = numbersList.first()
            val lastNumber = numbersList.last()
            val result = "$firstNumber - $lastNumber"
            val noOfChapters=numbersList.size*3

            // Create LinearLayout for each card
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20.dp, 32.dp, 20.dp, 32.dp)
                setBackgroundResource(R.drawable.bg_semiblack_card) // Set the background

                // Set layout params with margin
                layoutParams = LinearLayout.LayoutParams(220.dp,250.dp
                ).apply {
                    marginStart = 16.dp
                    marginEnd = 16.dp
                }
                gravity = Gravity.CENTER // Set gravity on the entire card layout
                setOnClickListener{
                    playClickSound()
                    GlobalValues.isActivityTransition=true
                    val intent = Intent(this@LogosBookShow, LogosChapterShow::class.java)
                    intent.putExtra("bookName", bookName)
                    intent.putExtra("bookNo",bookNo)
                    startActivity(intent)
                    finish()
                }
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
                setTextColor(ContextCompat.getColor(this@LogosBookShow, R.color.white)) // Text color
                textSize = 17f // Text size in sp
                typeface = ResourcesCompat.getFont(this@LogosBookShow, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER // Text alignment
            }

            val verticalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER // Center the layout vertically
                }
            }

            val horizontalLayout1 = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin=12.dp
                }
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 8.dp)
                setBackgroundResource(R.drawable.bg_white_all_round)
            }
            val imageView1 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(32.dp, 32.dp).apply {
                    gravity = Gravity.START
                }
                setImageResource(R.drawable.icon_star)
            }
            horizontalLayout1.addView(imageView1)

            val textView1 = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dp, 0, 8.dp, 0)
                gravity = Gravity.CENTER
                val starCounts=dataLogosEachBook?.second?:0
                text = "$starCounts / $noOfChapters"
                textSize = 16f // Text size in sp
                typeface = ResourcesCompat.getFont(this@LogosBookShow, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColor(this@LogosBookShow,R.color.secondary))
            }
            horizontalLayout1.addView(textView1)

            val horizontalLayout2 = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin=16.dp
                }
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 8.dp)
                setBackgroundResource(R.drawable.bg_white_all_round)
            }
            val imageView2 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(32.dp, 32.dp).apply {
                    gravity = Gravity.START
                }
                setImageResource(R.drawable.icon_eye)
            }
            horizontalLayout2.addView(imageView2)
            val textView2 = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dp, 0, 8.dp, 0)
                gravity = Gravity.START
                val totalQuestionsInBook=dataLogosEachBook?.first?:0
                val countOfViewStatus1=dataLogosEachBook?.third?:0
                text = "$countOfViewStatus1 / $totalQuestionsInBook"
                textSize = 16f // Text size in sp
                typeface = ResourcesCompat.getFont(this@LogosBookShow, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColor(this@LogosBookShow,R.color.secondary))
            }
            horizontalLayout2.addView(textView2)

            // Add the two horizontal LinearLayouts to the vertical LinearLayout
            verticalLayout.addView(horizontalLayout1)
            verticalLayout.addView(horizontalLayout2)

            // Add TextView and ImageView to the card layout
            cardLayout.addView(textView)
            cardLayout.addView(verticalLayout)

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
