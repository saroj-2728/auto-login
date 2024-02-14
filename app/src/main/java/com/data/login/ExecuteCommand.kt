package com.data.login
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


suspend fun login(username: String, password: String): String {
    return withContext(Dispatchers.IO) {
        val url = URL("https://10.100.1.1:8090/login.xml")
        val body = "mode=191&username=$username&password=$password"

        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

            // Create a hostname verifier that accepts all hostnames
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            // Write request body
            val outputStream = OutputStreamWriter(connection.outputStream)
            outputStream.write(body)
            outputStream.flush()

            val responseCode = connection.responseCode
            val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }

            connection.disconnect()

            if (responseCode == HttpURLConnection.HTTP_OK) {
                "Login successful: $responseBody"
            } else {
                "Login failed with response code $responseCode"
            }
        } catch (e: Exception) {
            "Login failed: $e"
        }
    }
}
