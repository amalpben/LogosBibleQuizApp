package `in`.ecsolution.logosquiz

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class QuizActivity : AppCompatActivity() {
    private lateinit var markTxt :TextView
    private lateinit var questCountTxt : TextView
    private lateinit var questionTxt : TextView
    private lateinit var option1Txt : TextView
    private lateinit var option2Txt : TextView
    private lateinit var option3Txt : TextView
    private lateinit var option4Txt : TextView
    private lateinit var option1Layout : LinearLayout
    private lateinit var option2Layout : LinearLayout
    private lateinit var option3Layout : LinearLayout
    private lateinit var option4Layout : LinearLayout
    private lateinit var timerTxt : TextView
    private lateinit var backBtn:ImageButton
    private lateinit var musicOnOff:ImageView

    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var questions: List<Question>
    private var timer: CountDownTimer? = null
    private var totalQuestions = 0
    private var bgmm: MediaPlayer? = null
    private var semm: MediaPlayer? = null
    private var book = 0
    private var chapter = 0
    private var type=""
    private lateinit var bookName:String
    private var isActivityPaused = false
    private var moveToNextQuestionRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var dbHelper: QuizDbHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        dbHelper = QuizDbHelper.getInstance(this@QuizActivity)
        //initialize views
        markTxt = findViewById(R.id.markTxt)
        questCountTxt = findViewById(R.id.questionNo)
        questionTxt=findViewById(R.id.question)
        option1Txt=findViewById(R.id.option1)
        option2Txt=findViewById(R.id.option2)
        option3Txt=findViewById(R.id.option3)
        option4Txt=findViewById(R.id.option4)
        option1Layout=findViewById(R.id.opt1Layout)
        option2Layout=findViewById(R.id.opt2Layout)
        option3Layout=findViewById(R.id.opt3Layout)
        option4Layout=findViewById(R.id.opt4Layout)
        timerTxt=findViewById(R.id.timer)

        //get intent data
        type = intent.getStringExtra("type").toString()
        //get questions from db
        if(type=="logos"){
            book = intent.getIntExtra("bookNo",0)
            chapter = intent.getIntExtra("chapterNo", 0)
            bookName=intent.getStringExtra("bookName").toString()
            questions = dbHelper.getQuestions(book, chapter)
        }else if(type=="daily"){
            val date=intent.getLongExtra("date",0)
            questions=dbHelper.getDailyQuizQuestions(date.toInt())
        }
        else if(type=="weekly"){
            val date=intent.getLongExtra("date",0)
            questions=dbHelper.getWeeklyQuizQuestions(date.toInt())
        }
        else if(type=="monthly"){
            val date=intent.getLongExtra("date",0)
            questions=dbHelper.getMonthlyQuizQuestions(date.toInt())
        }
        totalQuestions = questions.size
        //start quiz
        startQuiz()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //Alert
                val builder = AlertDialog.Builder(this@QuizActivity)
                builder.setMessage("Are you sure you want to exit the quiz?")
                    .setTitle("Confirmation")
                    .setPositiveButton("Yes") { _, _ ->
                        stopMusic()
                        timer?.cancel()
                        val intent = Intent(this@QuizActivity, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Do nothing here if the user cancels
                    }
                builder.create().show()
            }
        })
        backBtn=findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent:Intent
            if(type=="logos"){
                intent=Intent(this@QuizActivity,LogosChapterShow::class.java)
                intent.putExtra("bookName", bookName)
                intent.putExtra("bookNo",book)
            }
            else{
                intent=Intent(this@QuizActivity,GeneralQuizActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
        musicOnOff=findViewById(R.id.musicOnOffBtn)
        if(GlobalValues.musicOn==true){
            musicOnOff.setImageResource(R.drawable.icon_sound_on)
        }
        else{
            musicOnOff.setImageResource(R.drawable.icon_sound_off)
        }
        musicOnOff.setOnClickListener {
            if(GlobalValues.musicOn==true){
                GlobalValues.musicOn=false
                musicOnOff.setImageResource(R.drawable.icon_sound_off)
                stopMusic()
            }
            else{
                GlobalValues.musicOn=true
                musicOnOff.setImageResource(R.drawable.icon_sound_on)
                playMusic()
            }
        }
    }
    private fun startQuiz(){
        showNextQuestion()
    }
    private fun showNextQuestion(){
        if(currentQuestionIndex<questions.size) {
            val question = questions[currentQuestionIndex]
            questionTxt.text = question.question
            option1Txt.text = question.opt1
            option2Txt.text = question.opt2
            option3Txt.text = question.opt3
            option4Txt.text = question.opt4
            dbHelper.updateViewStatus(question.id)
            resetOptions()
            startTimer()
            option1Layout.setOnClickListener { checkAnswer(1) }
            option2Layout.setOnClickListener { checkAnswer(2) }
            option3Layout.setOnClickListener { checkAnswer(3) }
            option4Layout.setOnClickListener { checkAnswer(4) }
            questCountTxt.text = "${currentQuestionIndex + 1}/$totalQuestions"
        }
        else{
            //insert total mark
            if(type=="logos"){
                dbHelper.insertTotalMarks(score,chapter,book,totalQuestions)
                val intent = Intent(this@QuizActivity, ShowScoreActivity::class.java)
                intent.putExtra("score", score)
                intent.putExtra("totalQuestions", totalQuestions)
                intent.putExtra("bookName", bookName)
                intent.putExtra("bookNo",book)
                intent.putExtra("type",type)
                startActivity(intent)
                finish()
            }
            else{
                if(type=="daily"){
                    val date=intent.getLongExtra("date",0)
                    dbHelper.insertTotalMarksDaily(score,date,totalQuestions)
                }
                else if(type=="weekly"){
                    val date=intent.getLongExtra("date",0)
                    dbHelper.insertTotalMarksWeekly(score,date,totalQuestions)
                }
                else if(type=="monthly"){
                    val date=intent.getLongExtra("date",0)
                    dbHelper.insertTotalMarksMonthly(score,date,totalQuestions)
                }
                val intent = Intent(this@QuizActivity, ShowScoreActivity::class.java)
                intent.putExtra("score", score)
                intent.putExtra("totalQuestions", totalQuestions)
                intent.putExtra("type",type)
                startActivity(intent)
                finish()
            }
            stopMusic()
            timer?.cancel()
        }
    }
    private fun resetOptions(){
        option1Layout.isEnabled=true
        option2Layout.isEnabled=true
        option3Layout.isEnabled=true
        option4Layout.isEnabled=true

        option1Layout.setBackgroundColor(getColor(R.color.white))
        option2Layout.setBackgroundColor(getColor(R.color.white))
        option3Layout.setBackgroundColor(getColor(R.color.white))
        option4Layout.setBackgroundColor(getColor(R.color.white))
        option1Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.secondary))
        option2Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.secondary))
        option3Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.secondary))
        option4Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.secondary))
        }
    private fun startTimer(){
        playMusic()
        timer?.cancel()
        timer=object : CountDownTimer(31000,1000){
            override fun onTick(millisUntilFinished: Long) {
                timerTxt.text=(millisUntilFinished/1000).toString()
            }
            override fun onFinish() {
                showCorrectAnswer()
                playWrongAnswerSound()
                moveToNextQuestionWithDelay(4000)
            }
        }.start()
    }
    private fun showCorrectAnswer() {
        val correctAnswerIndex = questions[currentQuestionIndex].answer
        setOptionColor(correctAnswerIndex, R.color.greenLite)
        option1Layout.isEnabled=false
        option2Layout.isEnabled=false
        option3Layout.isEnabled=false
        option4Layout.isEnabled=false
    }
    private fun setOptionColor(optionIndex: Int, color: Int) {
        when (optionIndex) {
            1 -> {
                option1Layout.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, color))
                option1Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            }
            2 -> {
                option2Layout.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, color))
                option2Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            }
            3 -> {
                option3Layout.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, color))
                option3Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            }
            4 -> {
                option4Layout.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, color))
                option4Txt.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            }
        }

    }
    private fun moveToNextQuestionWithDelay(delayTime: Long) {
        if(isActivityPaused){
            return
        }
        moveToNextQuestionRunnable = Runnable {
            currentQuestionIndex++
            showNextQuestion()
        }
        handler.postDelayed(moveToNextQuestionRunnable!!, delayTime)
    }
    private fun checkAnswer(selectedOptionIndex: Int) {
        timer?.cancel()
        option1Layout.isEnabled=false
        option2Layout.isEnabled=false
        option3Layout.isEnabled=false
        option4Layout.isEnabled=false
        val correctAnswerIndex = questions[currentQuestionIndex].answer
        if (selectedOptionIndex == correctAnswerIndex) {
            score++
            setOptionColor(selectedOptionIndex, R.color.greenLite)
            markTxt.text = " $score"
            playCorrectAnswerSound()
            dbHelper.updateCorrect(questions[currentQuestionIndex].id)
        } else {
            setOptionColor(selectedOptionIndex, R.color.redLite)
            setOptionColor(correctAnswerIndex, R.color.greenLite)
            playWrongAnswerSound()
        }
        moveToNextQuestionWithDelay(4000)
    }
    //music player
    private fun playMusic() {
        if(GlobalValues.musicOn ==true){
            bgmm?.release()
            bgmm=MediaPlayer.create(this, R.raw.timer)
            bgmm!!.isLooping=true
            bgmm!!.setVolume(0.75f,0.75f)
            bgmm!!.start()
        }
    }
    private fun playCorrectAnswerSound() {
        if(GlobalValues.musicOn==true){
            bgmm?.pause()
            playSoundEffect(R.raw.correct_ans)
        }
    }

    private fun playWrongAnswerSound() {
        if(GlobalValues.musicOn==true){
            bgmm?.pause()
            playSoundEffect(R.raw.wrong_ans)
        }
    }

    private fun playSoundEffect(soundResId: Int) {
        if (isActivityPaused) {
            return // Don't play sound if the activity is paused
        }
        semm?.release() // Release previous sound effect
        semm = MediaPlayer.create(this, soundResId)
        semm?.apply {
            isLooping = false
            setVolume(1f, 1f)
            start()
        }
    }
    private fun stopMusic() {
        bgmm?.release()
        bgmm = null
        semm?.release()
        semm = null
    }
    override fun onPause() {
        super.onPause()
        stopMusic()
        isActivityPaused = true
        timer?.cancel()
        timerTxt.text="0"
        moveToNextQuestionRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
    override fun onResume() {
        super.onResume()
        if (isActivityPaused) {
            showCorrectAnswer()
            isActivityPaused = false
            moveToNextQuestionWithDelay(1000)
        }
        isActivityPaused = false
    }
    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
        timer?.cancel()
        moveToNextQuestionRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
}