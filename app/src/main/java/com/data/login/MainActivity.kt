package com.data.login

import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LoginUtils.initializeEncryptedSharedPreferences(this)
        val data =  LoginUtils.getLastLoggedInUser(this)

        if(data!=null){
            findViewById<EditText>(R.id.loginUsername).setText(data.first)
            findViewById<EditText>(R.id.loginPassword).setText(data.second)
        }
    }

    fun onLoginButtonClick(view: View) {
        val loginUsername = findViewById<EditText>(R.id.loginUsername)
        val loginPassword = findViewById<EditText>(R.id.loginPassword)
        val username = loginUsername.text.toString()
        val password = loginPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            lifecycleScope.launch {
                val response = login(username, password)
                Log.d("check-login", response)
                Toast.makeText(this@MainActivity, parseLoginResponse(response), Toast.LENGTH_SHORT).show()
                if(response.contains("You are signed in as {username}")){
                    LoginUtils.initializeEncryptedSharedPreferences(this@MainActivity)
                    LoginUtils.saveLastLoggedInUser(this@MainActivity,username,password)
                    showHideAppDialog()
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "Username or password cannot be empty", Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun showHideAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hide App")
            .setMessage("Do you want to hide the app from the launcher?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Hide the app
                disableLauncherActivity()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun enableLauncherActivity() {
        val componentName = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun disableLauncherActivity() {
        val componentName = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

}
