package ca.qc.bdeb.c5gm.tp1

import GestionnaireSession
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

//Source: https://medium.com/@arjunnarikkuni00/workmanager-using-kotlin-android-c72660afef31
//Source: https://developer.android.com/develop/ui/views/notifications/build-notification
//Source: https://www.geeksforgeeks.org/notifications-in-kotlin/
class TacheNotification(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val session = GestionnaireSession.getInstance(applicationContext)
    private val idCanal = "1"
    private val description = "Une notification"
    private var gestionnaireNotification: NotificationManager

    // Initialiser le gestionnaire de notification
    init {
        gestionnaireNotification = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            creerCanalNotification()
        }

        val aCompleterStreak = session.retournerAContinuerStreak()

        if (!aCompleterStreak) {
            Log.d("NOTIF", "ENVOIE NOTIF")
            envoyerNotification(getString(applicationContext, R.string.message_notification))
        }

        val travailNotification = OneTimeWorkRequestBuilder<TacheNotification>()
            .setInitialDelay(1 , TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(travailNotification)

        return Result.success()
    }

    // Méthode pour créer un notification
    private fun envoyerNotification(message: String) {
        val notification = NotificationCompat.Builder(applicationContext, idCanal)
            .setSmallIcon(R.drawable.study_logo)
            .setContentTitle(getString(applicationContext, R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        gestionnaireNotification.notify(1, notification)
    }

    // Méthode pour créer un canal de notification
    private fun creerCanalNotification() {
        // Assurer qu'on a pas déjà créer
        if (gestionnaireNotification.getNotificationChannel(idCanal) == null) {
            val canalNotification = NotificationChannel(idCanal, description, NotificationManager.IMPORTANCE_HIGH)
            canalNotification.enableVibration(true)
            gestionnaireNotification.createNotificationChannel(canalNotification)
            Log.d("NOTIF CANAL", "CANAL CREER")
        }
    }
}
