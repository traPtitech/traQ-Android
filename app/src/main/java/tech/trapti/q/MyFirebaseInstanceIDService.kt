package tech.trapti.q

import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("traq-debug", refreshedToken)

        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString("FCMToken", refreshedToken)
        editor.apply()
    }
}
