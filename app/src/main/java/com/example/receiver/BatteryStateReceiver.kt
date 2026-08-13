package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.example.domain.model.IslandEvent
import com.example.manager.IslandStateManager

class BatteryStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                val batteryStatus = context?.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                handleBatteryIntent(batteryStatus, isChargingOverride = true)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                val batteryStatus = context?.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                handleBatteryIntent(batteryStatus, isChargingOverride = false)
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                handleBatteryIntent(intent)
            }
        }
    }

    private fun handleBatteryIntent(intent: Intent?, isChargingOverride: Boolean? = null) {
        if (intent == null) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 85

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = isChargingOverride ?: (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC SuperCharge"
            else -> "AC Adapter"
        }

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            else -> "Normal"
        }

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
        val temperature = tempTenths / 10.0f

        val event = IslandEvent.BatteryStatus(
            percentage = percentage,
            isCharging = isCharging,
            isFastCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC && percentage < 80,
            powerSource = powerSource,
            batteryHealth = health,
            temperatureCelsius = temperature
        )

        IslandStateManager.postBatteryEvent(event)
    }
}
