package com.data.login

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var adapter: CredentialAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var savedAccountsContainer: LinearLayout
    private lateinit var showAccountsButton: MaterialButton
    private var isAccountsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize credential manager
        credentialManager = CredentialManager(this)

        // Initialize UI components
        savedAccountsContainer = findViewById(R.id.savedAccountsContainer)
        showAccountsButton = findViewById(R.id.showAccountsButton)

        // Set up RecyclerView for credentials
        setupCredentialsRecyclerView()

        // Load primary credential if available
        loadPrimaryCredential()

        // Start service
        startWifiStateService()
    }

    private fun setupCredentialsRecyclerView() {
        // Create RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        // Clear container and add RecyclerView
        savedAccountsContainer.removeAllViews()
        savedAccountsContainer.addView(recyclerView)

        // Set up adapter
        adapter = CredentialAdapter(
            credentials = credentialManager.getAllCredentials(),
            onDeleteClick = { credential ->
                deleteCredential(credential)
            },
            onPrimarySelected = { credential ->
                setPrimaryCredential(credential)
            }
        )

        recyclerView.adapter = adapter

        // Update show/hide button text
        updateShowHideButtonText()

        // Initially hide the saved accounts list
        if (credentialManager.getAllCredentials().isEmpty()) {
            // If no credentials, hide the entire recycler view
            recyclerView.visibility = View.GONE
            isAccountsVisible = false
        } else {
            // Show the list if we have credentials
            recyclerView.visibility = View.VISIBLE
            isAccountsVisible = true
        }
    }

    private fun loadPrimaryCredential() {
        val primaryCredential = credentialManager.getPrimaryCredential()
        if (primaryCredential != null) {
            findViewById<EditText>(R.id.loginUsername).setText(primaryCredential.username)
            findViewById<EditText>(R.id.loginPassword).setText(primaryCredential.password)
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
                val message = parseLoginResponse(response, username)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()

                if (response.contains("You are signed in as {username}")) {
                    // Save credential
                    val credential = SavedWifiCredential(
                        username = username,
                        password = password,
                        isPrimary = true
                    )
                    credentialManager.saveCredential(credential)

                    // Update UI
                    updateCredentialsList()
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "Username or password cannot be empty", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun onShowAccountsClick(view: View) {
        isAccountsVisible = !isAccountsVisible
        recyclerView.visibility = if (isAccountsVisible) View.VISIBLE else View.GONE
        updateShowHideButtonText()
    }

    private fun updateShowHideButtonText() {
        showAccountsButton.text = getString(
            if (isAccountsVisible) R.string.hide_saved_accounts else R.string.show_saved_accounts
        )
    }

    private fun updateCredentialsList() {
        val credentials = credentialManager.getAllCredentials()
        adapter.updateCredentials(credentials)

        // Show the recycler view if we have credentials and accounts are set to visible
        if (credentials.isNotEmpty() && isAccountsVisible) {
            recyclerView.visibility = View.VISIBLE
        } else if (credentials.isEmpty()) {
            // Hide the recycler view if no credentials
            recyclerView.visibility = View.GONE
        }
    }

    private fun deleteCredential(credential: SavedWifiCredential) {
        AlertDialog.Builder(this)
            .setTitle("Delete Credential")
            .setMessage("Are you sure you want to delete ${credential.username}'s credential?")
            .setPositiveButton("Delete") { _, _ ->
                credentialManager.deleteCredential(credential.username)
                updateCredentialsList()

                // If we deleted the primary credential, update the UI fields
                if (credential.isPrimary) {
                    loadPrimaryCredential()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setPrimaryCredential(credential: SavedWifiCredential) {
        credentialManager.setPrimaryCredential(credential.username)
        updateCredentialsList()
        loadPrimaryCredential()
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

    private fun startWifiStateService() {
        val serviceIntent = Intent(this, WifiStateService::class.java)
        startService(serviceIntent)
    }
}