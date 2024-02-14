package com.data.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

const val username = "your_username"
const val password = "your_password"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onLoginButtonClick() {
        var response = ""
        lifecycleScope.launch {
            response = login(username, password)
            Log.d("check-login", response)
            Toast.makeText(this@MainActivity, parseLoginResponse(response), Toast.LENGTH_SHORT)
                    .show()
        }
    }
}
