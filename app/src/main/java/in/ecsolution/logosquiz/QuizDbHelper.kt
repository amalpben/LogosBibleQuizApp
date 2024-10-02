package `in`.ecsolution.logosquiz

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class Question(val id : Int, val question: String, val opt1: String, val opt2: String,val opt3: String,val opt4: String, val answer: Int)
class QuizDbHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "quiz.db"
        private const val DATABASE_VERSION = 2
        @Volatile
        private var INSTANCE: QuizDbHelper? = null
        fun getInstance(context: Context): QuizDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QuizDbHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE book (id INTEGER PRIMARY KEY AUTOINCREMENT, bookTitle TEXT, bookNo INTEGER)")
        db?.execSQL("CREATE TABLE syllabus (id INTEGER PRIMARY KEY AUTOINCREMENT, chapter TEXT, bookNo INTEGER, star INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE marks (id INTEGER PRIMARY KEY AUTOINCREMENT, mark INTEGER DEFAULT 0,markTotal INTEGER DEFAULT 0,totalQuestions DEFAULT 0,star INTEGER DEFAULT 0,chapter INTEGER,book INTEGER)")
        db?.execSQL("CREATE TABLE questions (id INTEGER PRIMARY KEY AUTOINCREMENT, question TEXT, option1 TEXT, option2 TEXT, option3 TEXT, option4 TEXT, answer INTEGER, chapter INTEGER,book INTEGER,viewStatus INTEGER DEFAULT 0,correct INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE daily_quiz(id INTEGER PRIMARY KEY AUTOINCREMENT,quiz_date INTEGER,syllabus TEXT,score INTEGER DEFAULT 0,total INTEGER DEFAULT 0,isPlayed INTEGER DEFAULT 0,streak INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE dailyQuizQuestions(id INTEGER PRIMARY KEY AUTOINCREMENT,question TEXT,option1 TEXT,option2 TEXT,option3 TEXT,option4 TEXT,answer INTEGER,chapter TEXT,book INTEGER,testDate TEXT)")
        db?.execSQL("CREATE TABLE weekly_quiz(id INTEGER PRIMARY KEY AUTOINCREMENT,quiz_date TEXT,syllabus TEXT,score INTEGER DEFAULT 0,total INTEGER DEFAULT 0,isPlayed INTEGER DEFAULT 0,streak INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE weeklyQuizQuestions(id INTEGER PRIMARY KEY AUTOINCREMENT,question TEXT,option1 TEXT,option2 TEXT,option3 TEXT,option4 TEXT,answer INTEGER,testDate TEXT)")
        db?.execSQL("CREATE TABLE monthly_quiz(id INTEGER PRIMARY KEY AUTOINCREMENT,quiz_date TEXT,syllabus TEXT,score INTEGER DEFAULT 0,total INTEGER DEFAULT 0,isPlayed INTEGER DEFAULT 0,streak INTEGER DEFAULT 0,month INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE monthlyQuizQuestions(id INTEGER PRIMARY KEY AUTOINCREMENT,question TEXT,option1 TEXT,option2 TEXT,option3 TEXT,option4 TEXT,answer INTEGER,testDate TEXT)")

    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS questions")
            db.execSQL("DROP TABLE IF EXISTS marks")
            db.execSQL("DROP TABLE IF EXISTS syllabus")
            db.execSQL("DROP TABLE IF EXISTS star")
            db.execSQL("DROP TABLE IF EXISTS books")
            onCreate(db)
        }
    }
    //create fun to get questions from db based on book and chapter
    fun getQuestions(book: Int, chapter: Int): List<Question> {
        val questions = mutableListOf<Question>()
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT id,question,option1,option2,option3,option4,answer FROM questions WHERE book=? AND chapter=? ORDER BY RANDOM(), viewStatus DESC, correct DESC LIMIT 30", arrayOf(book.toString(),chapter.toString()))
        if(cursor.moveToFirst()){
            do{
                val question = Question(
                    id = cursor.getInt(0),
                    question = cursor.getString(1),
                    opt1 = cursor.getString(2),
                    opt2 = cursor.getString(3),
                    opt3 = cursor.getString(4),
                    opt4 = cursor.getString(5),
                    answer = cursor.getInt(6)
                )
                questions.add(question)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return questions
    }
    //update viewStatus
    fun updateViewStatus(questionId:Int){
        val db=writableDatabase
        db.execSQL("UPDATE questions SET viewStatus=1 WHERE id=?", arrayOf(questionId.toString()))
    }
    //update correct
    fun updateCorrect(questionId:Int){
        val db=writableDatabase
        db.execSQL("UPDATE questions SET correct=1 WHERE id=?", arrayOf(questionId.toString()))
    }
    //get count of questions viewed
//    fun getCount():Int{
//        val db=readableDatabase
//        val cursor=db.rawQuery("SELECT COUNT(*) FROM questions WHERE viewStatus=1", null)
//        cursor.moveToFirst()
//        val count=cursor.getInt(0)
//        cursor.close()
//        return count
//    }
//    //get count of questions answered correctly
//    fun getCorrectCount():Int{
//        val db=readableDatabase
//        val cursor=db.rawQuery("SELECT COUNT(*) FROM questions WHERE correct=1", null)
//        cursor.moveToFirst()
//        val count=cursor.getInt(0)
//        cursor.close()
//        return count
//    }
    //get max of question id
    fun getMaxId():Int{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT MAX(id) FROM questions", null)
        var maxId = 0
        if (cursor.moveToFirst()) {
            maxId = cursor.getInt(0)
        }
        cursor.close()
        return maxId
    }
    //count of question
//    fun totalNumberOfQuestions():Int{
//        val db=readableDatabase
//        val cursor=db.rawQuery("SELECT COUNT(*) FROM questions", null)
//        cursor.moveToFirst()
//        val qCount=cursor.getInt(0)
//        cursor.close()
//        return qCount
//    }
    //total marks Insert
    fun insertTotalMarks(mark:Int,chapter:Int,book:Int,total:Int){
        val db=writableDatabase
        var star=0
        if(mark.toFloat()/total.toFloat()>=0.9){
            star=3
        }
        else if(mark.toFloat()/total.toFloat()>=0.6){
            star=2
        }
        else if(mark.toFloat()/total.toFloat()>=0.4){
            star=1
        }
        db.execSQL("UPDATE marks SET mark=?,star=? WHERE chapter=? AND book=?", arrayOf(mark.toString(),star.toString(),chapter.toString(),book.toString()))
    }
    fun getLogosSyllabus():List<Triple<String,String,Int>>{
        val db=readableDatabase
        val syllabusList= mutableListOf<Triple<String,String,Int>>()
        val cursor=db.rawQuery("SELECT book.bookTitle,syllabus.chapter,syllabus.bookNo FROM syllabus JOIN book ON syllabus.bookNo=book.bookNo", null)
        if(cursor.moveToFirst()){
            do {
                val bookTitle=cursor.getString(0)
                val chapter=cursor.getString(1)
                val bookNo=cursor.getInt(2)
                syllabusList.add(Triple(bookTitle,chapter,bookNo))
            }while (cursor.moveToNext())
        }
        cursor.close()
        return syllabusList
    }
    fun createDataOfStar() {
        val db = writableDatabase // Use writableDatabase for insert operations
        val chapterDetails = mutableListOf<Pair<Int, String>>()

        // Fetch data from the syllabus table
        val cursor = db.rawQuery("SELECT bookNo, chapter FROM syllabus", null)
        if (cursor.moveToFirst()) {
            do {
                val bookNo = cursor.getInt(0)
                val chapter = cursor.getString(1)
                chapterDetails.add(Pair(bookNo, chapter))
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Create a map to store counts of questions for each chapter
        val questionCounts = mutableMapOf<Pair<Int, String>, Int>()

        // Fetch counts for all chapters at once
        val countCursor = db.rawQuery("SELECT book, chapter, COUNT(*) as count FROM questions GROUP BY book, chapter",null)
        if (countCursor.moveToFirst()) {
            do {
                val bookNo = countCursor.getInt(0)
                val chapterNo = countCursor.getString(1)
                val count = countCursor.getInt(2)
                questionCounts[Pair(bookNo, chapterNo)] = count
            } while (countCursor.moveToNext())
        }
        countCursor.close()

        // Prepare SQL statement for inserting data into the star table
        val insertStatement = db.compileStatement("INSERT INTO marks (book, chapter, star, totalQuestions, markTotal) VALUES (?, ?, ?, ?, ?)")
        db.beginTransaction()
        try {
            for (chapter in chapterDetails) {
                val bookNo = chapter.first
                val chapterNos = chapter.second.split(",")
                for (chapterNo in chapterNos) {
                    val count = questionCounts[Pair(bookNo, chapterNo)] ?: 0
                    insertStatement.clearBindings()
                    insertStatement.bindLong(1, bookNo.toLong())
                    insertStatement.bindString(2, chapterNo)
                    insertStatement.bindLong(3, 0)
                    insertStatement.bindLong(4, count.toLong())
                    insertStatement.bindLong(5, count.toLong().coerceAtMost(30))
                    insertStatement.executeInsert()
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    fun getDataForLogosBook(): MutableMap<Int, Triple<Int, Int, Int>>{
        val db=readableDatabase
        val dataMap= mutableMapOf<Int,Triple<Int,Int,Int>>()
        val cursor=db.rawQuery("SELECT m.book, SUM(m.totalQuestions) AS totalQuestionsInBook, SUM(m.star) AS totalStarsInBook, ( SELECT COUNT(q.id) FROM questions q WHERE q.viewStatus = 1 AND q.book = m.book ) AS countOfViewStatus1 FROM marks m GROUP BY m.book", null)
        if(cursor.moveToFirst()){
            do {
                val book=cursor.getInt(0)
                val totalQuestionsInBook=cursor.getInt(1)
                val totalStarsInBook=cursor.getInt(2)
                val countOfViewStatus1=cursor.getInt(3)
                dataMap[book]=Triple(totalQuestionsInBook,totalStarsInBook,countOfViewStatus1)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return dataMap
    }
    fun resetTables(){
        //delete from table question, chapter, book
        val db=writableDatabase
        db.execSQL("DELETE FROM questions")
        db.execSQL("DELETE FROM book")
        db.execSQL("DELETE FROM syllabus")
    }
    fun getDataForLogosChapter(bookNo:Int): List<List<Any>>{
        val db=readableDatabase
        val dataLists= mutableListOf<List<Int>>()
        val cursor=db.rawQuery("SELECT m.chapter,m.star,m.totalQuestions,m.mark,m.markTotal ,COUNT(q.id) AS c FROM marks as m  LEFT JOIN questions as q on q.chapter=m.chapter and q.book=m.book and q.viewStatus = 1  WHERE m.book=? GROUP BY m.book,m.chapter", arrayOf(bookNo.toString()))
        if(cursor.moveToFirst()){
            do {
                val chapter=cursor.getInt(0)
                val star=cursor.getInt(1)
                val totalQuestions=cursor.getInt(2)
                val mark=cursor.getInt(3)
                val markTotal=cursor.getInt(4)
                val viewedQuestions=cursor.getInt(5)
                dataLists.add(listOf(chapter,mark,markTotal,viewedQuestions,totalQuestions,star))
            }while (cursor.moveToNext())
        }
        cursor.close()
        return dataLists
    }
    fun totalStarsInLogos():Int{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT SUM(star) FROM marks", null)
        var totalStars=0
        if(cursor.moveToFirst()){
            totalStars=cursor.getInt(0)
        }
        cursor.close()
        return totalStars
    }
    fun ifSomeChapterHasNoQuestions():Boolean{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT count(*) FROM marks WHERE totalQuestions=0", null)
        cursor.moveToFirst()
        val count=cursor.getInt(0)
        cursor.close()
        return count>=0
    }
    fun updateQuestionCount(){
        val db=writableDatabase
        val dataLists= mutableListOf<List<Int>>()
        val cursor=db.rawQuery("SELECT count(*), chapter, book FROM questions GROUP BY book, chapter", null)
        if(cursor.moveToFirst()){
            do{
                val count=cursor.getInt(0)
                val chapter=cursor.getInt(1)
                val book=cursor.getInt(2)
                dataLists.add(listOf(count,chapter,book))
            }while (cursor.moveToNext())
        }
        cursor.close()
        val insertStatement = db.compileStatement("UPDATE marks SET totalQuestions=?,markTotal=?  WHERE book=? AND chapter=? ")
        db.beginTransaction()
        try {
            for (chapter in dataLists) {
                val totQuest=chapter[0]
                val chapterNo=chapter[1]
                val bookNo=chapter[2]
                insertStatement.clearBindings()
                insertStatement.bindLong(1, totQuest.toLong())
                insertStatement.bindLong(2, totQuest.toLong().coerceAtMost(30))
                insertStatement.bindLong(3, bookNo.toLong())
                insertStatement.bindLong(4, chapterNo.toLong())
                insertStatement.executeInsert()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    fun getDailyQuizQuestions(date:Int): List<Question> {
        val questions = mutableListOf<Question>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id,question,option1,option2,option3,option4,answer FROM dailyQuizQuestions WHERE testDate=?",
            arrayOf(date.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val question = Question(
                    id = cursor.getInt(0),
                    question = cursor.getString(1),
                    opt1 = cursor.getString(2),
                    opt2 = cursor.getString(3),
                    opt3 = cursor.getString(4),
                    opt4 = cursor.getString(5),
                    answer = cursor.getInt(6)
                )
                questions.add(question)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return questions
    }
    fun getWeeklyQuizQuestions(date:Int): List<Question> {
        val questions = mutableListOf<Question>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id,question,option1,option2,option3,option4,answer FROM weeklyQuizQuestions WHERE testDate=?",
            arrayOf(date.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val question = Question(
                    id = cursor.getInt(0),
                    question = cursor.getString(1),
                    opt1 = cursor.getString(2),
                    opt2 = cursor.getString(3),
                    opt3 = cursor.getString(4),
                    opt4 = cursor.getString(5),
                    answer = cursor.getInt(6)
                )
                questions.add(question)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return questions
    }
    fun getMonthlyQuizQuestions(date:Int): List<Question> {
        val questions = mutableListOf<Question>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id,question,option1,option2,option3,option4,answer FROM monthlyQuizQuestions WHERE testDate=?",
            arrayOf(date.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val question = Question(
                    id = cursor.getInt(0),
                    question = cursor.getString(1),
                    opt1 = cursor.getString(2),
                    opt2 = cursor.getString(3),
                    opt3 = cursor.getString(4),
                    opt4 = cursor.getString(5),
                    answer = cursor.getInt(6)
                )
                questions.add(question)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return questions
    }
    fun getDailyQuizQuestCount():Boolean{
        val db=readableDatabase
        val currentDate= TimeUnit.MILLISECONDS.toDays(Date().time).toInt()
        val cursor=db.rawQuery("SELECT count(*) FROM dailyQuizQuestions WHERE testDate=?", arrayOf(currentDate.toString()))
        cursor.moveToFirst()
        val count=cursor.getInt(0)
        cursor.close()
        return count>0
    }
    fun getDailyQuizDetails():List<Any>{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT * FROM daily_quiz ORDER BY id DESC LIMIT 1", null)
        var date=0
        var syllabus=""
        var score=0
        var total=0
        var isPlayed=0
        var streak=0
        if(cursor.moveToFirst()){
            date=cursor.getInt(1)
            syllabus=cursor.getString(2)
            score=cursor.getInt(3)
            total=cursor.getInt(4)
            isPlayed=cursor.getInt(5)
            streak=cursor.getInt(6)
        }
        cursor.close()
        return listOf(date,syllabus,score,total,isPlayed,streak)
    }
    fun getWeeklyQuizQuestCount(date:Long):Boolean{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT count(*) FROM weeklyQuizQuestions WHERE testDate=?", arrayOf(
            date.toString()
        ))
        cursor.moveToFirst()
        val count=cursor.getInt(0)
        cursor.close()
        return count>0
    }
    fun getWeeklyQuizDetails():List<Any>{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT * FROM weekly_quiz ORDER BY id DESC LIMIT 1", null)
        var date=0
        var syllabus=""
        var score=0
        var total=0
        var isPlayed=0
        var streak=0
        if(cursor.moveToFirst()){
            date=cursor.getInt(1)
            syllabus=cursor.getString(2)
            score=cursor.getInt(3)
            total=cursor.getInt(4)
            isPlayed=cursor.getInt(5)
            streak=cursor.getInt(6)
        }
        cursor.close()
        return listOf(date,syllabus,score,total,isPlayed,streak)
    }
    fun getMonthlyQuizQuestCount(date:Long):Boolean{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT count(*) FROM monthlyQuizQuestions WHERE testDate=?", arrayOf(
            date.toString()
        ))
        cursor.moveToFirst()
        val count=cursor.getInt(0)
        cursor.close()
        return count>0
    }
    fun getMonthlyQuizDetails():List<Any>{
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT * FROM monthly_quiz ORDER BY id DESC LIMIT 1", null)
        var date=0
        var syllabus=""
        var score=0
        var total=0
        var isPlayed=0
        var streak=0
        if(cursor.moveToFirst()){
            date=cursor.getInt(1)
            syllabus=cursor.getString(2)
            score=cursor.getInt(3)
            total=cursor.getInt(4)
            isPlayed=cursor.getInt(5)
            streak=cursor.getInt(6)
        }
        cursor.close()
        return listOf(date,syllabus,score,total,isPlayed,streak)
    }
    fun insertTotalMarksDaily(score:Int,date:Long,totalQuestions:Int){
        val db=writableDatabase
        var newStreak = 1
        val cursor = db.rawQuery("SELECT quiz_date, streak FROM daily_quiz WHERE isPlayed = 1 ORDER BY quiz_date DESC LIMIT 1",null)
        if (cursor.moveToFirst()) {
            val lastQuizDate = cursor.getInt(0)
            val lastStreak = cursor.getInt(1)
            newStreak = if (date.toInt() - lastQuizDate == 1) {
                lastStreak + 1
            } else {
                1
            }
        }
        cursor.close()
        GlobalValues.dStreak=newStreak
        db.execSQL("UPDATE daily_quiz SET score=?,total=?,isPlayed=1,streak=? WHERE quiz_date=?",arrayOf(score.toString(),totalQuestions.toString(),newStreak.toString(),date.toString()))
    }
    fun insertTotalMarksWeekly(score:Int,date:Long,totalQuestions:Int){
        val db=writableDatabase
        var newStreak = 1
        val cursor = db.rawQuery("SELECT quiz_date, streak FROM weekly_quiz WHERE isPlayed = 1 ORDER BY quiz_date DESC LIMIT 1",null)
        if (cursor.moveToFirst()) {
            val lastQuizDate = cursor.getInt(0)
            val lastStreak = cursor.getInt(1)
            newStreak = if (date.toInt() - lastQuizDate == 7) {
                lastStreak + 1
            } else {
                1
            }
        }
        cursor.close()
        GlobalValues.wStreak=newStreak
        db.execSQL("UPDATE weekly_quiz SET score=?,total=?,isPlayed=1,streak=? WHERE quiz_date=?",arrayOf(score.toString(),totalQuestions.toString(),newStreak.toString(),date.toString()))
    }
    fun insertTotalMarksMonthly(score:Int,date:Long,totalQuestions:Int){
        val db=writableDatabase
        val months = getMonthsSinceEpoch(date)
        var newStreak = 1
        val cursor = db.rawQuery("SELECT month, streak FROM daily_quiz WHERE isPlayed = 1 ORDER BY quiz_date DESC LIMIT 1",null)
        if (cursor.moveToFirst()) {
            val lastQuizDate = cursor.getInt(0)
            val lastStreak = cursor.getInt(1)
            newStreak = if (months - lastQuizDate == 1) {
                lastStreak + 1
            } else {
                1
            }
        }
        cursor.close()
        GlobalValues.mStreak=newStreak
        db.execSQL("UPDATE monthly_quiz SET score=?,total=?,isPlayed=1, month=?,streak=? WHERE quiz_date=?",arrayOf(score.toString(),totalQuestions.toString(),months.toString(),newStreak.toString(),date.toString()))
    }
    private fun getMonthsSinceEpoch(daysSinceEpoch: Long): Int {
        val epochCalendar = Calendar.getInstance().apply {
            set(1970, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateInMillis = TimeUnit.DAYS.toMillis(daysSinceEpoch)
        val dateCalendar = Calendar.getInstance().apply {
            timeInMillis = dateInMillis
        }
        val yearsSinceEpoch = dateCalendar.get(Calendar.YEAR) - epochCalendar.get(Calendar.YEAR)
        val monthsSinceEpoch = yearsSinceEpoch * 12 + (dateCalendar.get(Calendar.MONTH) - epochCalendar.get(Calendar.MONTH))
        return monthsSinceEpoch
    }
    fun calculateDailyStreak(){
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT quiz_date,streak FROM daily_quiz WHERE isPlayed=1 ORDER BY quiz_date DESC LIMIT 1", null)
        var streak=0
        var quizDate=0
        if(cursor.moveToFirst()){
            quizDate=cursor.getInt(0)
            streak=cursor.getInt(1)
        }
        if(TimeUnit.MILLISECONDS.toDays(Date().time).toInt()-quizDate<=1){
            //do nothing
        }
        else{
            streak=0
        }
        cursor.close()
        GlobalValues.dStreak=streak
    }
    fun calculateWeeklyStreak(){
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT quiz_date,streak FROM weekly_quiz WHERE isPlayed=1 ORDER BY quiz_date DESC LIMIT 1", null)
        var streak=0
        var quizDate=0
        if(cursor.moveToFirst()){
            quizDate=cursor.getInt(0)
            streak=cursor.getInt(1)
        }
        if(TimeUnit.MILLISECONDS.toDays(Date().time).toInt()-quizDate<=7){
            //do nothing
        }
        else{
            streak=0
        }
        cursor.close()
        GlobalValues.wStreak=streak
    }
    fun calculateMonthlyStreak(){
        val db=readableDatabase
        val cursor=db.rawQuery("SELECT month,streak FROM monthly_quiz WHERE isPlayed=1 ORDER BY quiz_date DESC LIMIT 1", null)
        var streak=0
        var quizDate=0
        if(cursor.moveToFirst()){
            quizDate=cursor.getInt(0)
            streak=cursor.getInt(1)
        }
        if(getMonthsSinceEpoch(TimeUnit.MILLISECONDS.toDays(Date().time))-quizDate<=1){
            //do nothing
        }
        else{
            streak=0
        }
        cursor.close()
        GlobalValues.mStreak=streak
    }
}