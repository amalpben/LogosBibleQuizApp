package `in`.ecsolution.logosquiz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class WorkerGenQuizNotification(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val isLastSunday= dayOfWeek == Calendar.SUNDAY && today + 7 > lastDayOfMonth
        createNotificationChannel()
        // Send Daily Notification
        sendNotification("Daily Quiz", "Daily Quiz is active now")
        // Send Weekly Notification (on every Saturday)
        if (dayOfWeek == Calendar.SUNDAY) {
            sendNotification("Weekly Quiz", "Weekly Quiz is active now")
        }
        // Send Monthly Notification (on the last Saturday of the month)
        if (isLastSunday) {
            sendNotification("Monthly Quiz", "Monthly quiz is active now")
        }
        Result.success()
    }
    private fun sendNotification(title: String, content: String) {
        val notificationId = System.currentTimeMillis().toInt()
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1,
            Intent(applicationContext, HomePageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_app)  // Use your app's icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Quiz Notification Channel", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for Quiz Notifications"
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "QUIZ_NOTIFICATION_CHANNEL"
    }
}
fun scheduleGenQuizNotifications(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .build()
    // Schedule for 8 AM
    val workRequest8AM = PeriodicWorkRequestBuilder<WorkerGenQuizNotification>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialDelay(8, 0), TimeUnit.MILLISECONDS)
        .setConstraints(constraints)
        .build()
    // Enqueue both workers
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "QuizNotification8AM", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest8AM
    )
}

fun calculateInitialDelay(hour: Int, minute: Int): Long {
    val currentTime = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    if (targetTime.before(currentTime)) {
        targetTime.add(Calendar.DAY_OF_YEAR, 1)
    }
    return targetTime.timeInMillis - currentTime.timeInMillis
}