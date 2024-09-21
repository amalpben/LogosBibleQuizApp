package `in`.ecsolution.logosquiz

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ShowScoreActivity : AppCompatActivity() {
    private var score = 0
    private var totalQuestions = 0
    private lateinit var type: String
    private lateinit var btnHome: ImageButton
    private lateinit var scoreTxt: TextView
    private var bgmm: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_score)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        if(GlobalValues.musicOn==true){
            bgmm= MediaPlayer.create(this, R.raw.score_music)
            bgmm!!.isLooping=false
            bgmm!!.start()
        }
        //get intent data
        type= intent.getStringExtra("type").toString()
        score = intent.getIntExtra("score", 0)
        totalQuestions = intent.getIntExtra("totalQuestions", 0)
        val backBtn = findViewById<ImageView>(R.id.backButton)
        if(type=="logos"){
            val bookName = intent.getStringExtra("bookName")
            val bookNo = intent.getIntExtra("bookNo", 0)
            backBtn.setOnClickListener {
                val intent = Intent(this@ShowScoreActivity, LogosChapterShow::class.java)
                intent.putExtra("bookName", bookName)
                intent.putExtra("bookNo", bookNo)
                startActivity(intent)
                finish()
            }
        }
        else{
            backBtn.setOnClickListener {
                val intent = Intent(this@ShowScoreActivity, GeneralQuizActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


        //set score
        scoreTxt = findViewById(R.id.scoreBoard)
        scoreTxt.text = "$score  / $totalQuestions"
        //set star
        val percent=(score.toFloat()/totalQuestions.toFloat())*100
        val starImage=findViewById<ImageView>(R.id.starImage)
        if(percent>=90){
            starImage.setImageResource(R.drawable.three_star)
        }
        else if(percent>=60){
            starImage.setImageResource(R.drawable.two_star)
        }
        else if(percent>=40){
            starImage.setImageResource(R.drawable.one_star)
        }
        else{
            starImage.setImageResource(R.drawable.zero_star)
        }
        //set home button
        btnHome = findViewById(R.id.homeButton)
        btnHome.setOnClickListener {
            val intent = Intent(this@ShowScoreActivity, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ShowScoreActivity, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }
}