package tech.trapti.q

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class NotificationService : FirebaseMessagingService() {
    var id = 1000
    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d("traq-debug", p0.toString())
        Log.d("traq-debug", p0?.data.toString())

        val title = p0?.data?.get("title")
        val body = p0?.data?.get("body")
        val tag = p0?.data?.get("tag")

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val mBuilder =  NotificationCompat.Builder(this)
                .setStyle(NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(body))
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle(title)
                .setContentText(body)
                .setGroup(tag)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)

        val intent = Intent(this, MainActivity::class.java)

        intent.putExtra("path", p0?.data?.get("path"))

//        val stackBuilder = TaskStackBuilder.create(this.applicationContext)
//        stackBuilder.addParentStack(MainActivity::class.java)
//        stackBuilder.addNextIntent(intent)
//
//        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT)
//        mBuilder.setContentIntent((resultPendingIntent))

        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pIntent)


        mNotificationManager.notify(Random().nextInt(), mBuilder.build())
    }
}
