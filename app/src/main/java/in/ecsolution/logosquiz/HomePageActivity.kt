package `in`.ecsolution.logosquiz

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class HomePageActivity : AppCompatActivity() {
    private lateinit var btnPlayLogos: TextView
    private lateinit var btnSyllabusLogos: TextView
    private lateinit var btnPlayGeneral: TextView

    private lateinit var btnSettings: ImageButton
    private lateinit var btnInfo: ImageButton
    private lateinit var btnExit: ImageButton
    private lateinit var btnHelp: ImageButton
    private lateinit var btnCloseInfo: ImageButton
    private lateinit var infoLayout: ConstraintLayout

    private lateinit var btnSound: SwitchCompat
    private lateinit var btnMusic: SwitchCompat
    private lateinit var btnCloseSettings: ImageButton

    private lateinit var txtDailyStreak: TextView
    private lateinit var txtWeeklyStreak: TextView
    private lateinit var txtMonthlyStreak: TextView
    private lateinit var txtTotalStars: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clickSound: MediaPlayer
    private lateinit var quizDbHelper: QuizDbHelper

    private var currentDate:Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        //initialize views
        btnPlayLogos = findViewById(R.id.playLogos)
        btnSyllabusLogos = findViewById(R.id.syllabusLogos)
        btnPlayGeneral = findViewById(R.id.playGeneral)
        btnSettings = findViewById(R.id.settings)
        btnInfo = findViewById(R.id.info)
        btnHelp=findViewById(R.id.help)
        btnExit = findViewById(R.id.exit)
        btnSound = findViewById(R.id.switchSound)
        btnMusic = findViewById(R.id.switchMusic)
        btnCloseSettings = findViewById(R.id.closeSettings)
        txtDailyStreak=findViewById(R.id.dailyStreak)
        txtWeeklyStreak=findViewById(R.id.weeklyStreak)
        txtMonthlyStreak=findViewById(R.id.mothlyStreak)
        txtTotalStars=findViewById(R.id.totalStars)
        btnCloseInfo=findViewById(R.id.closeInfo)
        currentDate = TimeUnit.MILLISECONDS.toDays(Date().time).toInt()

        clickSound=MediaPlayer.create(this, R.raw.click_sound)
        quizDbHelper = QuizDbHelper.getInstance(this)
        sharedPreferences = getSharedPreferences("login_data", MODE_PRIVATE)
        GlobalValues.soundOn = sharedPreferences.getBoolean("sound_on", true)
        GlobalValues.musicOn = sharedPreferences.getBoolean("music_on", true)

        if(GlobalValues.dStreak==-1){
            quizDbHelper.calculateDailyStreak()
        }
        if(GlobalValues.wStreak==-1){
            quizDbHelper.calculateWeeklyStreak()
        }
        if(GlobalValues.mStreak==-1){
            quizDbHelper.calculateMonthlyStreak()
        }
        txtDailyStreak.text=GlobalValues.dStreak.toString() + " Days"
        txtWeeklyStreak.text=GlobalValues.wStreak.toString() + " Weeks"
        txtMonthlyStreak.text=GlobalValues.mStreak.toString() + " Months"

        val ifDownloadRequired=sharedPreferences.getBoolean("download_required", true)
        val todayInDays = TimeUnit.MILLISECONDS.toDays(Date().time).toInt()
        val lastDownloadDate=sharedPreferences.getInt("last_download_date", todayInDays)
        if(ifDownloadRequired){
            if(lastDownloadDate<(todayInDays)){
                if(quizDbHelper.ifSomeChapterHasNoQuestions()){
                    sharedPreferences.edit()?.putInt("last_download_date", todayInDays)?.apply()
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED) // Requires an active internet connection
                        .build()
                    val workRequest = OneTimeWorkRequestBuilder<WorkerDownloadQuestionsDaily>().setConstraints(constraints).build()
                    WorkManager.getInstance(this).enqueue(workRequest)
                } else{
                    sharedPreferences.edit().putBoolean("download_required", false).apply()
                }
            }
        }

        val starCountInAllChapter=quizDbHelper.totalStarsInLogos()
        txtTotalStars.text=starCountInAllChapter.toString()

        if(GlobalValues.soundOn==true){
            btnSound.setChecked(true)
        }
        else{
            btnSound.setChecked(false)
        }
        //set click listeners
        btnPlayLogos.setOnClickListener {
            playClickSound()
            playGame()
        }
        btnSyllabusLogos.setOnClickListener {
            playClickSound()
            syllabus()
        }
        btnPlayGeneral.setOnClickListener{
            playClickSound()
            playGameGeneral()
        }
        btnHelp.setOnClickListener {
            playClickSound()
            appHelp()
        }
        btnInfo.setOnClickListener {
            playClickSound()
            infoLayout=findViewById(R.id.infoLayout)
            infoLayout.visibility = View.VISIBLE
        }
        btnCloseInfo.setOnClickListener {
            playClickSound()
            infoLayout.visibility = View.GONE
        }
        btnSettings.setOnClickListener{
            playClickSound()
            showSettings()
        }
        btnCloseSettings.setOnClickListener {
            playClickSound()
            val settingsLayout = findViewById<RelativeLayout>(R.id.settingsLayout)
            settingsLayout.visibility = View.GONE
        }
        btnExit.setOnClickListener {
            playClickSound()
            val builder = AlertDialog.Builder(this@HomePageActivity)
            builder.setMessage("Are you sure you want to exit the App?")
                .setTitle("Confirmation")
                .setPositiveButton("Yes") { _, _ ->
                    GlobalValues.isActivityTransition=false
                    finish()
                }
                .setNegativeButton("No") { _, _ ->
                    // Do nothing here if the user cancels
                }
            builder.create().show()
        }
        btnMusic.setOnClickListener {
            playClickSound()
            if(GlobalValues.musicOn==true){
//                MusicManager.stopMusic()//stopMusic()
                stopService(Intent(this, MusicService::class.java))
                GlobalValues.musicOn=false
                sharedPreferences.edit()?.putBoolean("music_on", false)?.apply()
                btnMusic.setChecked(false)
            }
            else{
//                MusicManager.playMusic(this)//playMusic()
                startService(Intent(this, MusicService::class.java))
                GlobalValues.musicOn=true
                sharedPreferences.edit()?.putBoolean("music_on", true)?.apply()
                btnMusic.setChecked(true)
            }
        }
        btnSound.setOnClickListener {
            if(GlobalValues.soundOn==true){
                GlobalValues.soundOn=false
                sharedPreferences.edit()?.putBoolean("sound_on", false)?.apply()
                btnSound.setChecked(false)
                playClickSound()
            }
            else{
                GlobalValues.soundOn=true
                sharedPreferences.edit()?.putBoolean("sound_on", true)?.apply()
                btnSound.setChecked(true)
                playClickSound()
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //Alert
                val builder = AlertDialog.Builder(this@HomePageActivity)
                builder.setMessage("Are you sure you want to exit the App?")
                    .setTitle("Confirmation")
                    .setPositiveButton("Yes") { _, _ ->
                        GlobalValues.isActivityTransition=false
                        finish()
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Do nothing here if the user cancels
                    }
                builder.create().show()
            }
        })
    }
    private fun playGame() {
        GlobalValues.isActivityTransition=true
        val intent = Intent(this, LogosBookShow::class.java)
        startActivity(intent)
        finish()
    }
    private fun appHelp(){
        GlobalValues.isActivityTransition=true
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun playGameGeneral() {
        if (isInternetAvailable()){
            lifecycleScope.launch {
                findViewById<RelativeLayout>(R.id.loading).visibility=View.VISIBLE
                GlobalValues.isActivityTransition=true
                downloadSyl()
                val intent = Intent(this@HomePageActivity, GeneralQuizActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        else{
            Toast.makeText(this,"Please connect to internet", Toast.LENGTH_SHORT).show()
        }
    }
    private fun syllabus(){
        GlobalValues.isActivityTransition=true
        val intent = Intent(this, SyllabusActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showSettings(){
        val settingsLayout = findViewById<RelativeLayout>(R.id.settingsLayout)
        settingsLayout.visibility = View.VISIBLE
    }
    private fun playClickSound() {
        if (GlobalValues.soundOn == true) {
            if (this::clickSound.isInitialized) {
                clickSound.start()
            }
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
            btnMusic.setChecked(true)
        }
        else{
            stopService(Intent(this, MusicService::class.java))
            btnMusic.setChecked(false)
        }
    }

    private suspend fun downloadSyl(){
        withContext(Dispatchers.IO) {
            val sylLastDownloadDate=sharedPreferences.getInt("lastSylDownloadDate",currentDate-1)
            val weeklyQuizDate=sharedPreferences.getInt("weeklyQuizQD",1)
            val monthlyQuizDate=sharedPreferences.getInt("monthlyQuizQD",1)
            if(sylLastDownloadDate!=currentDate){
                val dataUpdater = DataUpdater(this@HomePageActivity)
                dataUpdater.updateDQS()
                sharedPreferences.edit().putInt("dailyQuizQD",currentDate).apply()
                if(weeklyQuizDate+1<=currentDate){
                    dataUpdater.updateWQS()
                    sharedPreferences.edit().putInt("weeklyQuizQD",getNextSundayDate()).apply()
                }
                if(monthlyQuizDate+5<currentDate){
                    dataUpdater.updateMQS()
                    sharedPreferences.edit().putInt("monthlyQuizQD",getLastSundayOfCurrentOrNextMonth()).apply()
                }
                sharedPreferences.edit().putInt("lastSylDownloadDate",currentDate).apply()
            }
        }
    }
    private fun getLastSundayOfCurrentOrNextMonth(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastSundayOfCurrentMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = (lastSundayOfCurrentMonth - Calendar.SUNDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract)

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val lastSundayOfNextMonth = calendar.get(Calendar.DAY_OF_WEEK)
            val daysToSubtractNextMonth = (lastSundayOfNextMonth - Calendar.SUNDAY + 7) % 7
            calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtractNextMonth)
        }
        return (calendar.timeInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    private fun getNextSundayDate(): Int {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 0) // Move to next Sunday if today is already Sunday
        } else {
            val daysUntilNextSunday = 7+(Calendar.SUNDAY - calendar.get(Calendar.DAY_OF_WEEK))//
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilNextSunday) // Move to the next Sunday
        }
        return (calendar.timeInMillis / (1000 * 60 * 60 * 24)).toInt()
    }


}