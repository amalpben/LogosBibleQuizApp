package `in`.ecsolution.logosquiz

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class LogosChapterShow : AppCompatActivity() {
    private lateinit var clickSound: MediaPlayer
    private lateinit var btnHome: ImageButton
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private lateinit var dbHelper: QuizDbHelper
    private lateinit var bookName:String
    private lateinit var bookNameTxt:TextView
    private var bookNo:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logos_chapter_show)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        clickSound = MediaPlayer.create(this, R.raw.click_sound)
        //Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@LogosChapterShow, HomePageActivity::class.java)
                GlobalValues.isActivityTransition=true
                startActivity(intent)
                finish()
            }
        })
        btnHome = findViewById(R.id.homeButton)
        btnHome.setOnClickListener {
            playClickSound()
            GlobalValues.isActivityTransition=true
            val intent = Intent(this@LogosChapterShow, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
        val btnBack=findViewById<ImageButton>(R.id.backButton)
        btnBack.setOnClickListener {
            playClickSound()
            GlobalValues.isActivityTransition=true
            val intent = Intent(this@LogosChapterShow, LogosBookShow::class.java)
            startActivity(intent)
            finish()
        }
        //get intent data
        val intent = intent
        bookName = intent.getStringExtra("bookName").toString()
        bookNo = intent.getIntExtra("bookNo",0)
        bookNameTxt=findViewById(R.id.bookNameTxt)
        bookNameTxt.text=bookName
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

        val dataForLogosBook=dbHelper.getDataForLogosChapter(bookNo)
        for (chapter in dataForLogosBook) {
            val chapterNo=chapter[0]
            val mark = chapter[1]
            val totalMarks = chapter[2]
            val viewedQuestions=chapter[3]
            val totalQuestions=chapter[4]
            val star=chapter[5]

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
                setOnClickListener{
                    playClickSound()
//                    val questionAdd=dbHelper.isQuestionAddedInChapter(bookNo,chapterNo.toString().toInt())
                    if(totalQuestions.toString().toInt()>0){
                        val intent = Intent(this@LogosChapterShow, QuizActivity::class.java)
                        intent.putExtra("type","logos")
                        intent.putExtra("bookName",bookName)
                        intent.putExtra("bookNo",bookNo)
                        intent.putExtra("chapterNo",chapterNo.toString().toInt())
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(this@LogosChapterShow,"This chapter is locked",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Create TextView for the book name and chapter range
            val textView = TextView(this).apply {
                text = "അദ്ധ്യായം $chapterNo"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.dp) // Bottom margin
                }
                setTextColor(ContextCompat.getColor(this@LogosChapterShow, R.color.white)) // Text color
                textSize = 17f // Text size in sp
                typeface = ResourcesCompat.getFont(this@LogosChapterShow, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER // Text alignment
            }

            val verticalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER// Center the layout vertically
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
                setImageResource(R.drawable.icon_pen)
            }
            horizontalLayout1.addView(imageView1)

            val textView1 = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dp, 0, 8.dp, 0)
                gravity = Gravity.CENTER
                text = "$mark / $totalMarks"
                textSize = 16f // Text size in sp
                typeface = ResourcesCompat.getFont(this@LogosChapterShow, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColorStateList(this@LogosChapterShow,R.color.secondary))
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
                gravity = Gravity.CENTER
                text = "$viewedQuestions / $totalQuestions"
                textSize = 16f // Text size in sp
                typeface = ResourcesCompat.getFont(this@LogosChapterShow, R.font.mal_semi) // Font
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColorStateList(this@LogosChapterShow,R.color.secondary))
            }
            horizontalLayout2.addView(textView2)
            // Add the two horizontal LinearLayouts to the vertical LinearLayout
            verticalLayout.addView(horizontalLayout1)
            verticalLayout.addView(horizontalLayout2)
            val imageViewStar=ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(150.dp, LinearLayout.LayoutParams.WRAP_CONTENT)
                when (star) {
                    0 -> {
                        setImageResource(R.drawable.zero_star)
                    }
                    1 -> {
                        setImageResource(R.drawable.one_star)
                    }
                    2 -> {
                        setImageResource(R.drawable.two_star)
                    }
                    3 -> {
                        setImageResource(R.drawable.three_star)
                    }
                }
            }
            // Add TextView and ImageView to the card layout
            cardLayout.addView(textView)
            cardLayout.addView(verticalLayout)
            cardLayout.addView(imageViewStar)
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
    override fun attachBaseContext(newBase: Context?) {
        val configuration = Configuration(newBase?.resources?.configuration)
        configuration.fontScale = 1.0f // Set font scale to 1.0 (no scaling)

        // Apply the updated configuration to the context
        val context = newBase?.createConfigurationContext(configuration)

        // Pass the adjusted context to the super method
        super.attachBaseContext(context)
    }
}
