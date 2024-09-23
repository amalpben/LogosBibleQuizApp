package `in`.ecsolution.logosquiz

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeneralQuizActivity : AppCompatActivity() {
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var btnDailyQuiz:LinearLayout
    private lateinit var btnWeeklyQuiz:LinearLayout
    private lateinit var btnMonthlyQuiz:LinearLayout
    private lateinit var weeklyQuizTimer:TextView
    private lateinit var monthlyQuizTimer:TextView
    private lateinit var clickSound: MediaPlayer
    private lateinit var btnHome: ImageButton
    private lateinit var timerTxtDaily:TextView
    private lateinit var dbHelper:QuizDbHelper

    private var currentDate=-1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_general_quiz)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GlobalValues.isActivityTransition=true
                val intent = Intent(this@GeneralQuizActivity, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        dbHelper = QuizDbHelper.getInstance(this)
        timerTxtDaily=findViewById(R.id.timerTextDaily)
        weeklyQuizTimer=findViewById(R.id.timerTextWeekly)
        monthlyQuizTimer=findViewById(R.id.timerTextMonthly)

        btnHome = findViewById(R.id.homeButton)

        btnHome.setOnClickListener {
            playClickSound()
            GlobalValues.isActivityTransition=true
            val intent = Intent(this@GeneralQuizActivity, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }

        currentDate = TimeUnit.MILLISECONDS.toDays(Date().time).toInt()
        clickSound = MediaPlayer.create(this, R.raw.click_sound)
        sharedPreferences=getSharedPreferences("login_data", MODE_PRIVATE)
        GlobalValues.soundOn=sharedPreferences.getBoolean("sound_on", true)
        GlobalValues.musicOn=sharedPreferences.getBoolean("music_on", true)

        dailyQuiz()


        btnWeeklyQuiz=findViewById(R.id.weeklyQuizBtn)
        weeklyQuiz()
        btnMonthlyQuiz=findViewById(R.id.monthlyQuizBtn)
        monthlyQuiz()
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
    private fun startCountdownTimer() {
        val currentTime = Calendar.getInstance()
        // Set end time to 11:59:59 PM
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
        }
        // Calculate time difference in milliseconds
        val timeDifference = endTime.timeInMillis - currentTime.timeInMillis
        // Start the countdown timer
        object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Convert milliseconds into hours, minutes, and seconds
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                // Format the text in HH:MM:SS
                val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                // Set the text to the TextView
                timerTxtDaily.text = timeFormatted
            }
            override fun onFinish() {
                // When the timer finishes, set text to 00:00:00
                timerTxtDaily.text = "00:00:00"
            }
        }.start()
    }

    private fun startCountdownTimerWeekly() {
        // Get the current time
        val currentTime = Calendar.getInstance()

        // Set end time to 11:59:59 PM on Monday
        val endTime = Calendar.getInstance().apply {
            // Set the day to Monday
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
        }
        // Calculate the time difference in milliseconds
        val timeDifference = endTime.timeInMillis - currentTime.timeInMillis

        // Start the countdown timer
        object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Convert milliseconds into hours, minutes, and seconds
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                // Format the text in HH:MM:SS
                val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                // Set the text to the TextView
                weeklyQuizTimer.text = timeFormatted
            }

            override fun onFinish() {
                // When the timer finishes, set text to 00:00:00
                weeklyQuizTimer.text = "00:00:00"
            }
        }.start()
    }

    private fun startMonthlyTimer() {
        val calendar = Calendar.getInstance()

        // Set to last Sunday of the current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        // Add 4 days from the last Sunday
        calendar.add(Calendar.DAY_OF_MONTH, 4)

        val timeLeftInMillis = calendar.timeInMillis - System.currentTimeMillis()

        object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                monthlyQuizTimer.text = timeFormatted
            }

            override fun onFinish() {
                monthlyQuizTimer.text = "00:00:00"
            }
        }.start()
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun dailyQuiz(){
        btnDailyQuiz=findViewById(R.id.dailyQuizBtn)
        val dailyQuizDateTxt=findViewById<TextView>(R.id.dailyQuizDate)
        val dailyQuizScoreTxt=findViewById<TextView>(R.id.dailyQuizScore)
        val dailyQuizStreakTxt=findViewById<TextView>(R.id.dailyQuizStreak)
        val dailyQuizSyllabusTxt=findViewById<TextView>(R.id.dailyQuizSyl)
        val dailyQuizDetails=dbHelper.getDailyQuizDetails()
        val date=dailyQuizDetails[0].toString().toLong()
        val dateFormated=SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(TimeUnit.DAYS.toMillis(date)))
        val syllabus=dailyQuizDetails[1].toString()
        val score=dailyQuizDetails[2].toString().toInt()
        val total=dailyQuizDetails[3].toString().toInt()
        val isPlayed=dailyQuizDetails[4].toString().toInt()
        val streak=GlobalValues.dStreak

        dailyQuizStreakTxt.text="$streak Days"
        dailyQuizSyllabusTxt.text=syllabus
        if(date.toInt()==currentDate){
            dailyQuizDateTxt.text = dateFormated
            dailyQuizScoreTxt.text="$score / $total"
            if(isPlayed==0){
                startCountdownTimer()
                btnDailyQuiz.setOnClickListener{
                    if(dbHelper.getDailyQuizQuestCount()){
                        GlobalValues.isActivityTransition=false
                        val intent = Intent(this, QuizActivity::class.java)
                        intent.putExtra("type","daily")
                        intent.putExtra("date", date)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        if (isInternetAvailable()) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val dataUpdater = DataUpdater(this@GeneralQuizActivity)
                                val isDownloadSuccessful = dataUpdater.dailyQuizDownload(date.toInt())
                                if (isDownloadSuccessful) {
                                    if(dbHelper.getDailyQuizQuestCount()){
                                        GlobalValues.isActivityTransition=false
                                        val intent = Intent(this@GeneralQuizActivity, QuizActivity::class.java)
                                        intent.putExtra("type","daily")
                                        intent.putExtra("date", date)
                                        withContext(Dispatchers.Main) {
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                    else{
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@GeneralQuizActivity, "No questions found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@GeneralQuizActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else{
                            Toast.makeText(this@GeneralQuizActivity,"Please connect to internet",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                timerTxtDaily.text="completed"
            }
        }
    }
    private fun weeklyQuiz(){
        btnWeeklyQuiz=findViewById(R.id.weeklyQuizBtn)
        val weeklyQuizDateTxt=findViewById<TextView>(R.id.weeklyQuizDate)
        val weeklyQuizScoreTxt=findViewById<TextView>(R.id.weeklyQuizScore)
        val weeklyQuizStreakTxt=findViewById<TextView>(R.id.weeklyQuizStreak)
        val weeklyQuizSyllabusTxt=findViewById<TextView>(R.id.weeklyQuizSyl)
        val weeklyQuizDetails=dbHelper.getWeeklyQuizDetails()
        val date=weeklyQuizDetails[0].toString().toLong()
        val dateFormated=SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(TimeUnit.DAYS.toMillis(date)))
        val syllabus=weeklyQuizDetails[1].toString()
        val score=weeklyQuizDetails[2].toString().toInt()
        val total=weeklyQuizDetails[3].toString().toInt()
        val isPlayed=weeklyQuizDetails[4].toString().toInt()
        val streak=GlobalValues.wStreak

        weeklyQuizStreakTxt.text="$streak Weeks"
        weeklyQuizSyllabusTxt.text=syllabus
        weeklyQuizDateTxt.text = dateFormated
        if(date.toInt()==currentDate||date.toInt()+1==currentDate){
            weeklyQuizScoreTxt.text="$score / $total"
            if(isPlayed==0){
                startCountdownTimerWeekly()
                btnWeeklyQuiz.setOnClickListener{
                    if(dbHelper.getWeeklyQuizQuestCount(date)){
                        GlobalValues.isActivityTransition=false
                        val intent = Intent(this, QuizActivity::class.java)
                        intent.putExtra("type","weekly")
                        intent.putExtra("date", date)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        if (isInternetAvailable()) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val dataUpdater = DataUpdater(this@GeneralQuizActivity)
                                val isDownloadSuccessful = dataUpdater.weeklyQuizDownload(date)
                                if (isDownloadSuccessful) {
                                    if(dbHelper.getWeeklyQuizQuestCount(date)){
                                        GlobalValues.isActivityTransition=false
                                        val intent = Intent(this@GeneralQuizActivity, QuizActivity::class.java)
                                        intent.putExtra("type","weekly")
                                        intent.putExtra("date", date)
                                        withContext(Dispatchers.Main) {
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                    else{
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@GeneralQuizActivity, "No questions found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@GeneralQuizActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else{
                            Toast.makeText(this@GeneralQuizActivity,"Please connect to internet",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                weeklyQuizTimer.text="completed"
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun monthlyQuiz(){
        btnMonthlyQuiz=findViewById(R.id.monthlyQuizBtn)
        val monthlyQuizDateTxt=findViewById<TextView>(R.id.monthlyQuizDate)
        val monthlyQuizScoreTxt=findViewById<TextView>(R.id.monthlyQuizScore)
        val monthlyQuizStreakTxt=findViewById<TextView>(R.id.monthlyQuizStreak)
        val monthlyQuizSyllabusTxt=findViewById<TextView>(R.id.monthlyQuizSyl)
        val monthlyQuizDetails=dbHelper.getMonthlyQuizDetails()
        val date=monthlyQuizDetails[0].toString().toLong()
        val dateFormated=SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(TimeUnit.DAYS.toMillis(date)))
        val syllabus=monthlyQuizDetails[1].toString()
        val score=monthlyQuizDetails[2].toString().toInt()
        val total=monthlyQuizDetails[3].toString().toInt()
        val isPlayed=monthlyQuizDetails[4].toString().toInt()
        val streak=GlobalValues.mStreak

        monthlyQuizStreakTxt.text="$streak Months"
        monthlyQuizSyllabusTxt.text=syllabus
        monthlyQuizDateTxt.text = dateFormated
        if(date.toInt()==currentDate||date.toInt()+1==currentDate||date.toInt()+2==currentDate||date.toInt()+3==currentDate||date.toInt()+4==currentDate){
            monthlyQuizScoreTxt.text="$score / $total"
            if(isPlayed==0){
                startMonthlyTimer()
                btnMonthlyQuiz.setOnClickListener{
                    if(dbHelper.getMonthlyQuizQuestCount(date)){
                        GlobalValues.isActivityTransition=false
                        val intent = Intent(this, QuizActivity::class.java)
                        intent.putExtra("type","weekly")
                        intent.putExtra("date", date)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        if (isInternetAvailable()) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val dataUpdater = DataUpdater(this@GeneralQuizActivity)
                                val isDownloadSuccessful = dataUpdater.monthlyQuizDownload(date)
                                if (isDownloadSuccessful) {
                                    if(dbHelper.getMonthlyQuizQuestCount(date)){
                                        GlobalValues.isActivityTransition=false
                                        val intent = Intent(this@GeneralQuizActivity, QuizActivity::class.java)
                                        intent.putExtra("type","weekly")
                                        intent.putExtra("date", date)
                                        withContext(Dispatchers.Main) {
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                    else{
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@GeneralQuizActivity, "No questions found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@GeneralQuizActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else{
                            Toast.makeText(this@GeneralQuizActivity,"Please connect to internet",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                monthlyQuizTimer.text="completed"
            }
        }
    }


}