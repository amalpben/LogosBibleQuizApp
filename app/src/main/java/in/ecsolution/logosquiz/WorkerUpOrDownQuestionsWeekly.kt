package `in`.ecsolution.logosquiz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkerUpOrDownQuestionsWeekly(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        createNotificationChannel()
        return withContext(Dispatchers.IO) {
            if (DataUpdater(applicationContext).weeklyUpdateQuestionsLogos()) {
                DataUpdater(applicationContext).weeklyDownloadQuestionsLogos()
                if(GlobalValues.isNewQuestionsAvailable){
                    GlobalValues.isNewQuestionsAvailable=false
                    val dbHelper=QuizDbHelper.getInstance(applicationContext)
                    dbHelper.updateQuestionCount()
                    sendSuccessNotification()
                }
                Result.success()
            } else {
                Result.failure()
            }
        }
    }
    private fun sendSuccessNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "weeklyUpdates")
            .setSmallIcon(R.drawable.icon_app)
            .setContentTitle("Questions Updated")
            .setContentText("New questions are added or updated some questions to Logos Quiz. Explore now!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(1, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("updates", "Download Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Notifications for download updates" }
        (applicationContext.getSystemService(NotificationManager::class.java))
            .createNotificationChannel(channel)
    }
}
