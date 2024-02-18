
package com.data.login
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import android.content.BroadcastReceiver
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WifiStateService","Device boot\n\nboot")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, WifiStateService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}

class SleepReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            // Device has gone to sleep, stop or disable your service here
            val serviceIntent = Intent(context, WifiStateService::class.java)
            context.stopService(serviceIntent)
        }
    }
}

class WakeReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WifiStateService","Device boot\n\nboot")
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            Log.d("WifiStateService","Device boot\n\nboot")
            val serviceIntent = Intent(context, WifiStateService::class.java)
            ContextCompat.startForegroundService(context,serviceIntent)
        }
    }
}

class WifiStateService : Service() {

    private lateinit var connectivityManager: ConnectivityManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available")
            LoginUtils.initializeEncryptedSharedPreferences(applicationContext)
            val data = LoginUtils.getLastLoggedInUser(applicationContext)
            data?.let { user ->
                coroutineScope.launch {
                    val response = login(user.first, user.second)
                    Log.d(TAG, "Login response: $response")
                    withContext(Dispatchers.Main) {
                        showToast(parseLoginResponse(response))
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            showToast("Disconnected from network")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        showToast("Disconnected from network")
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        startForegroundService()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = createNotificationChannel()
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("WiFi State Service")
                .setContentText("Listening for WiFi state changes")
                .setSmallIcon(R.drawable.icon)
                .build()

            startForeground(ONGOING_NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "wifi_state_service_channel"
            val channelName = "WiFi State Service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = "Channel for WiFi State Service"
            notificationManager.createNotificationChannel(channel)
            return channelId
        }
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    companion object {
        private const val TAG = "WifiStateService"
        private const val ONGOING_NOTIFICATION_ID = 12345
    }
}




