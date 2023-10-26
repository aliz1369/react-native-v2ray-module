package com.sijav.reactnativev2raymodule.service

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.sijav.reactnativev2raymodule.AppConfig
import com.sijav.reactnativev2raymodule.R
import com.sijav.reactnativev2raymodule.V2rayModule
import com.sijav.reactnativev2raymodule.util.MessageUtil
import com.sijav.reactnativev2raymodule.util.Utils
import java.lang.ref.SoftReference

@TargetApi(Build.VERSION_CODES.N)
class QSTileService : TileService() {

    fun setState(state: Int) {
        if (state == Tile.STATE_INACTIVE) {
            qsTile?.state = Tile.STATE_INACTIVE
            qsTile?.label = V2rayModule.NAME
            qsTile?.icon = Icon.createWithResource(applicationContext, R.drawable.ic_stat_name)
        } else if (state == Tile.STATE_ACTIVE) {
            qsTile?.state = Tile.STATE_ACTIVE
            qsTile?.label = V2RayServiceManager.currentConfig?.remarks
            qsTile?.icon = Icon.createWithResource(applicationContext, R.drawable.ic_stat_name)
        }

        qsTile?.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        setState(Tile.STATE_INACTIVE)
        mMsgReceive = ReceiveMessageHandler(this)
        registerReceiver(mMsgReceive, IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY))
        MessageUtil.sendMsg2Service(this, AppConfig.MSG_REGISTER_CLIENT, "")
    }

    override fun onStopListening() {
        super.onStopListening()

        unregisterReceiver(mMsgReceive)
        mMsgReceive = null
    }

    override fun onClick() {
        super.onClick()
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                Utils.startVServiceFromToggle(this)
            }
            Tile.STATE_ACTIVE -> {
                Utils.stopVService(this)
            }
        }
    }

    private var mMsgReceive: BroadcastReceiver? = null

    private class ReceiveMessageHandler(context: QSTileService) : BroadcastReceiver() {
        internal var mReference: SoftReference<QSTileService> = SoftReference(context)
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val context = mReference.get()
            when (intent?.getIntExtra("key", 0)) {
                AppConfig.MSG_STATE_RUNNING -> {
                    context?.setState(Tile.STATE_ACTIVE)
                }
                AppConfig.MSG_STATE_NOT_RUNNING -> {
                    context?.setState(Tile.STATE_INACTIVE)
                }
                AppConfig.MSG_STATE_START_SUCCESS -> {
                    context?.setState(Tile.STATE_ACTIVE)
                }
                AppConfig.MSG_STATE_START_FAILURE -> {
                    context?.setState(Tile.STATE_INACTIVE)
                }
                AppConfig.MSG_STATE_STOP_SUCCESS -> {
                    context?.setState(Tile.STATE_INACTIVE)
                }
            }
        }
    }
}
