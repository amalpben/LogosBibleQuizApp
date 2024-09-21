package `in`.ecsolution.logosquiz

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {
    private var isSoundOn: Boolean? = true
    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        //total questions and total score from QuizDBHelper
        val dbHelper = QuizDbHelper.getInstance(this@ProfileActivity)
        val totalQuestions = dbHelper.totalNumberOfQuestions()
        val totalScore = dbHelper.getCorrectCount()
        val viewCount = dbHelper.getCount()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferences = getSharedPreferences("login_data", MODE_PRIVATE)
        val isSoundOn = sharedPreferences?.getBoolean("sound_on", true)
        if(isSoundOn==true){
//            playMusic()
            MusicManager.onActivityCreated(true, this)
        }
        //get username from shared preferences
        val sharedPreferences = getSharedPreferences("login_data", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        //set username
        val textView = findViewById<TextView>(R.id.name)
        val scoreVal=findViewById<TextView>(R.id.scoreVal)
        val totQuestVal=findViewById<TextView>(R.id.totQuestVal)
        val totQuestViewVal=findViewById<TextView>(R.id.totQuestViewVal)
        textView.text = "Hi $username,"
        scoreVal.text=totalScore.toString()
        totQuestVal.text=totalQuestions.toString()
        totQuestViewVal.text=viewCount.toString()
        //Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press handling
                val intent = Intent(this@ProfileActivity, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }
    override fun onPause() {
        super.onPause()
        MusicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        MusicManager.resumeMusic()
    }
    override fun onDestroy() {
        super.onDestroy()
        isSoundOn?.let { MusicManager.onActivityDestroyed(it) }
    }

}