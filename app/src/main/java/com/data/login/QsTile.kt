package com.data.login

import android.content.Context
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QsTile : TileService() {

    private val tag: String = QsTile::class.java.simpleName
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onTileAdded() {
        val tile: Tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        updateTileIcon(tile)
        tile.updateTile()
    }

    override fun onClick() {
        val tile: Tile = qsTile
        tile.state = Tile.STATE_ACTIVE
            LoginUtils.initializeEncryptedSharedPreferences(this)
            val data =  LoginUtils.getLastLoggedInUser(this)
        coroutineScope.launch {
            if(data!=null){
            val response = login( data.first, data.second)
                Toast.makeText(applicationContext, parseLoginResponse(response), Toast.LENGTH_SHORT).show()
            }
            else   {
                Toast.makeText(applicationContext, "login from your id and pass from app for first time", Toast.LENGTH_SHORT).show()
            }

        }
        Log.d(tag, "Tile is Pressed")
    }

    private fun updateTileIcon(tile: Tile) {
        val iconResId: Int = R.drawable.icon
        val icon: Icon = Icon.createWithResource(applicationContext, iconResId)
        tile.icon = icon
    }
}

fun parseLoginResponse(response: String): String {
    // Parse the XML response and extract the message
    val xml = response.trim()
    val start = xml.indexOf("<message><![CDATA[") + "<message><![CDATA[".length
    val end = xml.indexOf("]]></message>")
    return if (start >= 0 && end >= 0) {
        xml.substring(start, end)
    } else {
        "Unknown error occurred"
    }
}
