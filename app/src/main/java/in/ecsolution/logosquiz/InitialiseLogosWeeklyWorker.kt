package `in`.ecsolution.logosquiz
import android.content.Context
import androidx.work.* // Import WorkManager classes
import java.util.concurrent.TimeUnit // Import TimeUnit for scheduling

// Function to schedule the worker
fun scheduleWeeklyWorker(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // Require the network to be connected
        .build()

    // Define the work request to run every Saturday after 7 AM
    val weeklyWorkRequest = PeriodicWorkRequestBuilder<WorkerUpOrDownQuestionsWeekly>(7, TimeUnit.DAYS)
        .setConstraints(constraints)
        .setInitialDelay(calculateInitialDelayForSaturdayAfter7AM(), TimeUnit.MILLISECONDS) // Calculate initial delay to next Saturday after 7 AM
        .build()

    // Enqueue the work request
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "WeeklySyncWork",
        ExistingPeriodicWorkPolicy.UPDATE, // Choose REPLACE to overwrite existing work
        weeklyWorkRequest
    )
}

// Function to calculate the delay to the next Saturday at 7 AM
fun calculateInitialDelayForSaturdayAfter7AM(): Long {
    val calendar = java.util.Calendar.getInstance()

    // Get current time
    val now = System.currentTimeMillis()
    calendar.timeInMillis = now

    // Calculate the days until the next Saturday
    val today = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    val daysUntilSaturday = if (today <= java.util.Calendar.SATURDAY) {
        java.util.Calendar.SATURDAY - today
    } else {
        7 - (today - java.util.Calendar.SATURDAY)
    }

    calendar.add(java.util.Calendar.DAY_OF_WEEK, daysUntilSaturday)
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 7) // Set hour to 7 AM
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)

    // Calculate the delay in milliseconds from now to next Saturday at 7 AM
    val delay = calendar.timeInMillis - now
    return if (delay > 0) delay else 7 * 24 * 60 * 60 * 1000 + delay // Ensure a delay of at least 7 days
}
