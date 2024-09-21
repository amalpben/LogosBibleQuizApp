package `in`.ecsolution.logosquiz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkerDownloadQuestionsDaily(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        createNotificationChannel()

        return withContext(Dispatchers.IO) {
            if (DataUpdater(applicationContext).updateQuestions()) {
                if(GlobalValues.isNewQuestionsAvailable){
                    val dbHelper=QuizDbHelper.getInstance(applicationContext)
                    dbHelper.updateQuestionCount()
                    GlobalValues.isNewQuestionsAvailable=false
                    sendSuccessNotification()
                }
                Result.success()
            } else {
                Result.failure()
            }
        }
    }

    private fun sendSuccessNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "updates")
            .setSmallIcon(R.drawable.icon_app)
            .setContentTitle("Questions Added")
            .setContentText("New questions are added to Logos Quiz. Explore now!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(
                applicationContext, 1,
                Intent(applicationContext, LogosBookShow::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_IMMUTABLE
            ))
            .build()
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(2, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("updates", "Download Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Notifications for download updates" }
        (applicationContext.getSystemService(NotificationManager::class.java))
            .createNotificationChannel(channel)
    }
}
