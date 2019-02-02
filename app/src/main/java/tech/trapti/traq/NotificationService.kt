package tech.trapti.traq

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.app.NotificationBuilderWithBuilderAccessor
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class NotificationService : FirebaseMessagingService() {
    var id = 1000
    companion object {
        const val GROUP_KEY = "traQ-notification"
        const val CHANNEL_ID_NORMAL = "channel_01"
        public var notificationMap = HashMap<String, Array<String>>()
        public var notificationIDMap = HashMap<String, Int>()

    }
    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d("traq-debug", p0.toString())
        Log.d("traq-debug", p0?.data.toString())

        val title = p0?.data?.get("title")
        val body = p0?.data?.get("body")
        val tag = p0?.data?.get("tag")
        val iconURL = p0?.data?.get("icon")
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID_NORMAL)
                    .setGroupSummary(true)
                    .setGroup(GROUP_KEY)
                    .setContentTitle("traQ")
                    .setSmallIcon(R.drawable.notification_icon_background)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true) // 重要。各通知がすべて消えた時に、サマリーも自動で消える
                    .setColor(Color.rgb(0x0D, 0x67, 0xEA))
                    .build()
            mNotificationManager.notify(1, summaryNotification)
        }

        try {
            var bodyList = notificationMap.get(tag)
            var notificationID = notificationIDMap.get(tag)
            var mBuilder: NotificationCompat.Builder
            if (bodyList == null) {
                 mBuilder = NotificationCompat.Builder(this, CHANNEL_ID_NORMAL)
                        .setStyle(NotificationCompat.BigTextStyle()
                                .setBigContentTitle(title)
                                .bigText(body))
                        .setSmallIcon(R.drawable.notification_icon_background)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setColor(Color.rgb(0x0D, 0x67, 0xEA))
                        .setGroup(GROUP_KEY)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                if (body != null) {
                    bodyList = arrayOf(body)
                    if (tag != null) {
                        notificationMap.set(tag, bodyList)
                    }
                }
            }else {
                mBuilder = NotificationCompat.Builder(this, CHANNEL_ID_NORMAL)
                        .setSmallIcon(R.drawable.notification_icon_background)
                        .setContentTitle(title)
                        .setContentText((bodyList.size+1).toString() + "件の新規メッセージ" )
                        .setGroup(GROUP_KEY)
                        .setColor(Color.rgb(0x0D, 0x67, 0xEA))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                val inbox = NotificationCompat.InboxStyle().setBigContentTitle(title).setSummaryText((bodyList.size+1).toString() + "件の新規メッセージ" ).addLine(body)
                for (message in bodyList.reversedArray()) {
                    Log.d("traq-debug", message)
                    inbox.addLine(message)
                }
                mBuilder.setStyle(inbox)
                if (body != null) {
                    bodyList = bodyList.plus(body)
                }
                if (tag != null) {
                    notificationMap.set(tag, bodyList)
                }
            }


            if (notificationID == null) {
                notificationID = Random().nextInt()
                if (tag != null) {
                    notificationIDMap.set(tag, notificationID)
                }
            }


            val intent = Intent(this, MainActivity::class.java)

            intent.putExtra("path", p0?.data?.get("path"))
            intent.putExtra("tag", tag)

//        val stackBuilder = TaskStackBuilder.create(this.applicationContext)
//        stackBuilder.addParentStack(MainActivity::class.java)
//        stackBuilder.addNextIntent(intent)
//
//        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT)
//        mBuilder.setContentIntent((resultPendingIntent))

            val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder.setContentIntent(pIntent)

            mNotificationManager.notify(notificationID, mBuilder.build())
        } catch (e: Exception){
            Log.e("traq-debug", e.toString())
        }

    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { result ->
            val refreshedToken = result.token
            Log.d("traq-debug", refreshedToken)

            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            editor.putString("FCMToken", refreshedToken)
            editor.apply()
        }
    }
}
