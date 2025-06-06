package com.data.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CredentialAdapter(
    private var credentials: List<SavedWifiCredential>,
    private val onDeleteClick: (SavedWifiCredential) -> Unit,
    private val onPrimarySelected: (SavedWifiCredential) -> Unit
) : RecyclerView.Adapter<CredentialAdapter.CredentialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_credential, parent, false)
        return CredentialViewHolder(view)
    }

    override fun onBindViewHolder(holder: CredentialViewHolder, position: Int) {
        val credential = credentials[position]
        holder.bind(credential)
    }

    override fun getItemCount(): Int = credentials.size

    fun updateCredentials(newCredentials: List<SavedWifiCredential>) {
        credentials = newCredentials
        notifyDataSetChanged()
    }

    inner class CredentialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.textUsername)
        private val radioButtonPrimary: RadioButton = itemView.findViewById(R.id.radioPrimary)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(credential: SavedWifiCredential) {
            usernameTextView.text = credential.username
            radioButtonPrimary.isChecked = credential.isPrimary

            radioButtonPrimary.setOnClickListener {
                if (!credential.isPrimary) {
                    onPrimarySelected(credential)
                }
            }

            buttonDelete.setOnClickListener {
                onDeleteClick(credential)
            }
        }
    }
}