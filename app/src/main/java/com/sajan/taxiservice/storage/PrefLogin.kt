package com.sajan.taxiservice.storage

import android.content.Context

class PrefLogin(context: Context) {

    private val prefsManager = context.getSharedPreferences("Login_Prefs", Context.MODE_PRIVATE)

    private val keyIsLogin = "KEY_IS_Login"
    private val keyID = "KEY_ID"
    private val keyName = "KEY_NAME"
    private val keyAddress = "KEY_ADDRESS"
    private val keyContact = "KEY_CONTACT"
    private val keyZoneCode = "KEY_ZONE_CODE"
    private val keyLotNumber = "KEY_LOT_NUMBER"
    private val keyVehicleType = "KEY_VEHICLE_TYPE"
    private val keyTaxiNumber = "KEY_TAXI_NUMBER"
    private val keyState = "KEY_STATE"
    private val keyVehicleManagementCode = "KEY_VEHICLE_MANAGEMENT_CODE"
    private val keyVehicleRegistrationSystem = "KEY_VEHICLE_REGISTRATION_SYSTEM"

    var isLogin: Boolean
        set(value) {
            prefsManager.edit().putBoolean(keyIsLogin, value).apply()
        }
        get() {
            return prefsManager.getBoolean(keyIsLogin, false)
        }

    var id: Int
        set(value) {
            prefsManager.edit().putInt(keyID, value).apply()
        }
        get() {
            return prefsManager.getInt(keyID, 0)
        }

    var name: String?
        set(value) {
            prefsManager.edit().putString(keyName, value).apply()
        }
        get() {
            return prefsManager.getString(keyName, null)
        }

    var address: String?
        set(value) {
            prefsManager.edit().putString(keyAddress, value).apply()
        }
        get() {
            return prefsManager.getString(keyAddress, null)
        }

    var contact: String?
        set(value) {
            prefsManager.edit().putString(keyContact, value).apply()
        }
        get() {
            return prefsManager.getString(keyContact, null)
        }

    var zoneCode: String?
        set(value) {
            prefsManager.edit().putString(keyZoneCode, value).apply()
        }
        get() {
            return prefsManager.getString(keyZoneCode, null)
        }

    var lotNumber: String?
        set(value) {
            prefsManager.edit().putString(keyLotNumber, value).apply()
        }
        get() {
            return prefsManager.getString(keyLotNumber, null)
        }

    var vehicleType: String?
        set(value) {
            prefsManager.edit().putString(keyVehicleType, value).apply()
        }
        get() {
            return prefsManager.getString(keyVehicleType, null)
        }

    var taxiNumber: String?
        set(value) {
            prefsManager.edit().putString(keyTaxiNumber, value).apply()
        }
        get() {
            return prefsManager.getString(keyTaxiNumber, null)
        }

    var state: String?
        set(value) {
            prefsManager.edit().putString(keyState, value).apply()
        }
        get() {
            return prefsManager.getString(keyState, null)
        }

    var vehicleManagementCode: String?
        set(value) {
            prefsManager.edit().putString(keyVehicleManagementCode, value).apply()
        }
        get() {
            return prefsManager.getString(keyVehicleManagementCode, null)
        }

    var vehicleRegistrationCode: String?
        set(value) {
            prefsManager.edit().putString(keyVehicleRegistrationSystem, value).apply()
        }
        get() {
            return prefsManager.getString(keyVehicleRegistrationSystem, null)
        }

    fun resetLoginPrefs() {
        prefsManager.edit().clear().apply()
    }
}