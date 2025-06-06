package com.data.login

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import android.content.BroadcastReceiver
import android.net.wifi.WifiManager
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
    private lateinit var wifiManager: WifiManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager
    private lateinit var credentialManager: CredentialManager
    private var isAutoLoginInProgress = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available")
            checkAndAttemptAutoLogin()
        }

        override fun onLost(network: Network) {
            showToast("Disconnected from network")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        showToast("WiFi service started")
        credentialManager = CredentialManager(this)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        startForegroundService()
    }

    private fun checkAndAttemptAutoLogin() {
        // Check if this is a captive portal network
        if (isLikelyCaptivePortal()) {
            Log.d(TAG, "Detected likely captive portal, attempting auto-login")
            showToast("Detected a captive portal, attempting auto-login")
            attemptAutoLogin()
        } else {
            Log.d(TAG, "Not a captive portal network, skipping auto-login")
        }
    }

    // Check if current network is likely a captive portal
    private fun isLikelyCaptivePortal(): Boolean {
        val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connManager.activeNetwork ?: return false
        val capabilities = connManager.getNetworkCapabilities(network) ?: return false

        // Check if network has internet capability
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        // Check if network is validated (can reach the internet)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        // If network has internet capability but isn't validated, it might be a captive portal
        return hasInternet && !isValidated
    }

    private fun attemptAutoLogin() {
        if (isAutoLoginInProgress) return

        val credentials = credentialManager.getAllCredentials()
        if (credentials.isEmpty()) {
            Log.d(TAG, "No credentials available")
            return
        }

        // Sort by primary first
        val sortedCredentials = credentials.sortedByDescending { it.isPrimary }

        isAutoLoginInProgress = true
        coroutineScope.launch {
            var loginSuccessful = false

            for (credential in sortedCredentials) {
                if (loginSuccessful) break

                Log.d(TAG, "Attempting login with username: ${credential.username}")
                try {
                    val response = login(credential.username, credential.password)

                    // Check if login was successful
                    if (response.contains("You are signed in as {username}")) {
                        loginSuccessful = true

                        withContext(Dispatchers.Main) {
                            showToast("Auto-login successful with ${credential.username}")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showToast("Login failed for ${credential.username}: ${parseLoginResponse(response)}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during auto-login: ${e.message}")
                    withContext(Dispatchers.Main) {
                        showToast("Error during login: ${e.message}")
                    }
                }

                // Add a small delay between attempts
                delay(1000)
            }

            isAutoLoginInProgress = false

            if (!loginSuccessful) {
                Log.d(TAG, "All login attempts failed")
                withContext(Dispatchers.Main) {
                    showToast("All login attempts failed")
                }
            }
        }
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
        coroutineScope.cancel()
    }

    companion object {
        private const val TAG = "WifiStateService"
        private const val ONGOING_NOTIFICATION_ID = 12345
    }
}