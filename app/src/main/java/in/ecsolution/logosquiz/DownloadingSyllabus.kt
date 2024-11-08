package `in`.ecsolution.logosquiz

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteStatement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class DownloadedBook(val book: String, val bookno: Int)
data class DownloadedSyllabus(val bookno: Int, val chapter: String, val star: Int)
data class DownloadedQuest(
    val id: Int, val question: String, val opt1: String, val opt2: String,
    val opt3: String, val opt4: String, val answer: Int, val bookno: Int, val chapterid: Int
)
data class DownloadedDailyQuizQuestion(val id: Int, val question: String, val option1: String, val option2: String,
                                       val option3: String, val option4: String, val answer: Int, val chapter: String, val testDate:String, val book:Int)
data class Syllabus(val testDate: Int, val syllabus:String, val id:Int )

class DataUpdater(private val context: Context) {
    private val dbHelper = QuizDbHelper.getInstance(context)
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://ecsolution.in/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun updateBooks(): Boolean = fetchAndInsertData(
        fetchData = { apiService.getLogosBooks("get_syllabus_book") },
        insertQuery = "INSERT INTO book (bookTitle, bookNo) VALUES (?, ?)",
        bindData = { statement, book ->
            statement.bindString(1, book.book)
            statement.bindLong(2, book.bookno.toLong())
        }
    )


    suspend fun updateSyllabus(): Boolean = fetchAndInsertData(
        fetchData = { apiService.getLogosSyllabus("get_syllabus_chapter") },
        insertQuery = "INSERT INTO syllabus (chapter, bookNo, star) VALUES (?, ?, ?)",
        bindData = { statement, syllabus ->
            statement.bindString(1, syllabus.chapter)
            statement.bindLong(2, syllabus.bookno.toLong())
            statement.bindLong(3, syllabus.star.toLong())
        }
    )


    suspend fun updateQuestions(): Boolean = fetchAndInsertData(
        fetchData = { apiService.getNewQuestions(dbHelper.getMaxId()) },
        insertQuery = """
            INSERT OR IGNORE INTO questions 
            (id, question, option1, option2, option3, option4, answer, chapter, book) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        bindData = { statement, question ->
            statement.bindLong(1, question.id.toLong())
            statement.bindString(2, question.question)
            statement.bindString(3, question.opt1)
            statement.bindString(4, question.opt2)
            statement.bindString(5, question.opt3)
            statement.bindString(6, question.opt4)
            statement.bindLong(7, question.answer.toLong())
            statement.bindLong(8, question.chapterid.toLong())
            statement.bindLong(9, question.bookno.toLong())
        }
    )
    suspend fun weeklyDownloadQuestionsLogos(): Boolean = fetchAndInsertData(
        fetchData = { apiService.getNewQuestions(dbHelper.getMaxId()) },
        insertQuery = """
            INSERT OR IGNORE INTO questions 
            (id, question, option1, option2, option3, option4, answer, chapter, book) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        bindData = { statement, question ->
            statement.bindLong(1, question.id.toLong())
            statement.bindString(2, question.question)
            statement.bindString(3, question.opt1)
            statement.bindString(4, question.opt2)
            statement.bindString(5, question.opt3)
            statement.bindString(6, question.opt4)
            statement.bindLong(7, question.answer.toLong())
            statement.bindLong(8, question.chapterid.toLong())
            statement.bindLong(9, question.bookno.toLong())
        }
    )
    suspend fun weeklyUpdateQuestionsLogos(): Boolean = fetchAndInsertData(
        fetchData = { apiService.getLogosQuestionUpdate(dbHelper.getMaxId()) },
        insertQuery = """
            UPDATE questions
        SET question = ?, option1 = ?, option2 = ?, option3 = ?, option4 = ?, answer = ?, chapter = ?, book = ?
        WHERE id = ?
        """,
        bindData = { statement, question ->
            statement.bindLong(1, question.id.toLong())
            statement.bindString(2, question.question)
            statement.bindString(3, question.opt1)
            statement.bindString(4, question.opt2)
            statement.bindString(5, question.opt3)
            statement.bindString(6, question.opt4)
            statement.bindLong(7, question.answer.toLong())
            statement.bindLong(8, question.chapterid.toLong())
            statement.bindLong(9, question.bookno.toLong())
        }
    )
    //SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date())
    suspend fun dailyQuizDownload(date: Int): Boolean=fetchAndInsertData(
        fetchData = { apiService.dailyQuizDownload(date.toString()) },
        insertQuery = """
             INSERT OR REPLACE INTO dailyQuizQuestions (question, option1, option2, option3, option4, answer, chapter, book, testDate,id) 
             values (?,?,?,?,?,?,?,?,?,?)
        """.trimIndent(),
        bindData = { statement, questions ->
            statement.bindString(1, questions.question)
            statement.bindString(2, questions.option1)
            statement.bindString(3, questions.option2)
            statement.bindString(4, questions.option3)
            statement.bindString(5, questions.option4)
            statement.bindLong(6, questions.answer.toLong())
            statement.bindString(7, questions.chapter)
            statement.bindLong(8, questions.book.toLong())
            statement.bindString(9, questions.testDate)
            statement.bindLong(10,questions.id.toLong())
        }
    )
    suspend fun weeklyQuizDownload(date: Long): Boolean=fetchAndInsertData(
        fetchData = { apiService.weeklyQuizDownload(date.toString()) },
        insertQuery = """
             INSERT INTO weeklyQuizQuestions (question, option1, option2, option3, option4, answer, testDate,id) 
             values (?,?,?,?,?,?,?,?)
        """.trimIndent(),
        bindData = { statement, questions ->
            statement.bindString(1, questions.question)
            statement.bindString(2, questions.option1)
            statement.bindString(3, questions.option2)
            statement.bindString(4, questions.option3)
            statement.bindString(5, questions.option4)
            statement.bindLong(6, questions.answer.toLong())
            statement.bindString(7, questions.testDate)
            statement.bindLong(8,questions.id.toLong())
        }
    )
    suspend fun monthlyQuizDownload(date: Long): Boolean=fetchAndInsertData(
        fetchData = { apiService.monthlyQuizDownload(date.toString()) },
        insertQuery = """
             INSERT INTO monthlyQuizQuestions (question, option1, option2, option3, option4, answer, testDate,id) 
             values (?,?,?,?,?,?,?,?)
        """.trimIndent(),
        bindData = { statement, questions ->
            statement.bindString(1, questions.question)
            statement.bindString(2, questions.option1)
            statement.bindString(3, questions.option2)
            statement.bindString(4, questions.option3)
            statement.bindString(5, questions.option4)
            statement.bindLong(6, questions.answer.toLong())
            statement.bindString(7, questions.testDate)
            statement.bindLong(8,questions.id.toLong())
        }
    )
    suspend fun updateDQS(): Boolean = fetchAndInsertData(
        fetchData = { apiService.updateDQSyl(TimeUnit.MILLISECONDS.toDays(Date().time).toInt()) },
        insertQuery = """
            INSERT INTO daily_quiz (id, quiz_date, syllabus)
        values (?,?,?)
        """,
        bindData = { statement, syllabus ->
            statement.bindLong(1, syllabus.id.toLong())
            statement.bindLong(2, syllabus.testDate.toLong())
            statement.bindString(3, syllabus.syllabus)
        }
    )
    suspend fun updateWQS(): Boolean = fetchAndInsertData(
        fetchData = { apiService.updateWQSyl(getNextSundayDate()) },
        insertQuery = """
            INSERT INTO weekly_quiz (id, quiz_date, syllabus)
        values (?,?,?)
        """,
        bindData = { statement, syllabus ->
            statement.bindLong(1, syllabus.id.toLong())
            statement.bindLong(2, syllabus.testDate.toLong())
            statement.bindString(3, syllabus.syllabus)
        }
    )
    suspend fun updateMQS(): Boolean = fetchAndInsertData(
        fetchData = { apiService.updateMQSyl(getLastSundayOfCurrentOrNextMonth()) },
        insertQuery = """
            INSERT INTO monthly_quiz (id, quiz_date, syllabus)
        values (?,?,?)
        """,
        bindData = { statement, syllabus ->
            statement.bindLong(1, syllabus.id.toLong())
            statement.bindLong(2, syllabus.testDate.toLong())
            statement.bindString(3, syllabus.syllabus)
        }
    )

    private suspend fun <T> fetchAndInsertData(
        fetchData: suspend () -> List<T>,
        insertQuery: String,
        bindData: (insertStatement: SQLiteStatement, data: T) -> Unit
    ): Boolean {
        var isSuccessful = false
        try {
            val dataList = fetchData()
            GlobalValues.isNewQuestionsAvailable=dataList.isNotEmpty()
            dbHelper.writableDatabase.use { db ->
                db.beginTransaction()
                try {
                    val insertStatement = db.compileStatement(insertQuery)
                    for (data in dataList) {
                        insertStatement.clearBindings()
                        bindData(insertStatement, data)
                        insertStatement.executeInsert()
                    }
                    db.setTransactionSuccessful()
                    isSuccessful = true
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isSuccessful
    }
    fun isOneTimeCodeExecuted(): Boolean {
        val sharedPreferences = context.getSharedPreferences("login_data", MODE_PRIVATE)
        return sharedPreferences.getBoolean("one_time_code_executed", false)
    }

    fun runOneTimeCode() {
        dbHelper.createDataOfStar()
        scheduleWeeklyWorker(context)
//        scheduleGenQuizNotifications(context)
    }


    fun setOneTimeCodeExecuted() {
        val sharedPreferences = context.getSharedPreferences("login_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("one_time_code_executed", true)
        editor.apply()
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
            calendar.add(Calendar.DAY_OF_YEAR, 0) // Move to next Sunday if today is already Sunday put 7 as zero
        } else {
            val daysUntilNextSunday = 7+(Calendar.SUNDAY - calendar.get(Calendar.DAY_OF_WEEK))//
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilNextSunday) // Move to the next Sunday
        }
        return (calendar.timeInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

}
interface ApiService {
    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun getNewQuestions(@Query("last_id") lastId: Int): List<DownloadedQuest>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun getLogosBooks(@Query("get_syllabus_book") logosSyllabus: String): List<DownloadedBook>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun getLogosSyllabus(@Query("get_syllabus_chapter") logosSyllabus: String): List<DownloadedSyllabus>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun getLogosQuestionUpdate(@Query("get_id_update") lastId: Int): List<DownloadedQuest>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun dailyQuizDownload(@Query("date_daily_quiz") date: String): List<DownloadedDailyQuizQuestion>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun weeklyQuizDownload(@Query("date_weekly_quiz") date: String): List<DownloadedDailyQuizQuestion>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun monthlyQuizDownload(@Query("date_monthly_quiz") date: String): List<DownloadedDailyQuizQuestion>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun updateDQSyl(@Query("dailyQuizSyl") dailyQuiz :Int): List<Syllabus>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun updateWQSyl(@Query("weeklyQuizSyl") weeklyQuiz :Int): List<Syllabus>

    @GET("logos_quiz/get_data_for_logos.php")
    suspend fun updateMQSyl(@Query("monthlyQuizSyl") monthly :Int): List<Syllabus>

}