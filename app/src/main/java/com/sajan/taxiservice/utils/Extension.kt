package com.sajan.taxiservice.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.sajan.taxiservice.BuildConfig
import com.sajan.taxiservice.dataModels.DriverInfoDto

fun getParam(driverInfoDto: DriverInfoDto): StringBuilder {
    val paramBuilder = StringBuilder()
    paramBuilder.append(
        "name=" + driverInfoDto.name +
                "&address=" + driverInfoDto.address +
                "&contact=" + driverInfoDto.contact +
                "&zoneCode=" + driverInfoDto.taxiDetailDto!!.zoneCode +
                "&lotNumber=" + driverInfoDto.taxiDetailDto!!.lotNumber +
                "&vehicleType=" + driverInfoDto.taxiDetailDto!!.vehicleType +
                "&taxiNumber=" + driverInfoDto.taxiDetailDto!!.taxiNumber +
                "&state=" + driverInfoDto.taxiDetailDto!!.state +
                "&vehicleManagementCode=" + driverInfoDto.taxiDetailDto!!.vehicleManagementCode +
                "&vehicleRegisterSystem=" + driverInfoDto.taxiDetailDto!!.vehicleRegisterSystem
    )
    return paramBuilder
}

fun getParamOld(driverInfoDto: DriverInfoDto): StringBuilder {
    val paramBuilder = StringBuilder()
    paramBuilder.append(
        "id=" + driverInfoDto.driverId +
                "&name=" + driverInfoDto.name +
                "&address=" + driverInfoDto.address +
                "&contact=" + driverInfoDto.contact +
                "&zoneCode=" + driverInfoDto.taxiDetailDto!!.zoneCode +
                "&lotNumber=" + driverInfoDto.taxiDetailDto!!.lotNumber +
                "&vehicleType=" + driverInfoDto.taxiDetailDto!!.vehicleType +
                "&taxiNumber=" + driverInfoDto.taxiDetailDto!!.taxiNumber +
                "&state=" + driverInfoDto.taxiDetailDto!!.state +
                "&vehicleManagementCode=" + driverInfoDto.taxiDetailDto!!.vehicleManagementCode +
                "&vehicleRegisterSystem=" + driverInfoDto.taxiDetailDto!!.vehicleRegisterSystem
    )
    return paramBuilder
}

fun getParamNew(driverInfoDto: DriverInfoDto): StringBuilder {
    val paramBuilder = StringBuilder()
    paramBuilder.append(
        "id=" + driverInfoDto.driverId +
                "&name=" + driverInfoDto.name +
                "&address=" + driverInfoDto.address +
                "&contact=" + driverInfoDto.contact +
                "&zoneCode=" + driverInfoDto.taxiDetailDto!!.zoneCode +
                "&lotNumber=" + driverInfoDto.taxiDetailDto!!.lotNumber +
                "&vehicleType=" + driverInfoDto.taxiDetailDto!!.vehicleType +
                "&taxiNumber=" + driverInfoDto.taxiDetailDto!!.taxiNumber +
                "&state=" + driverInfoDto.taxiDetailDto!!.state +
                "&vehicleManagementCode=" + driverInfoDto.taxiDetailDto!!.vehicleManagementCode +
                "&vehicleRegisterSystem=" + driverInfoDto.taxiDetailDto!!.vehicleRegisterSystem
    )
    return paramBuilder
}

var BottomSheetBehavior<View>.isExpanded: Boolean
    get() {
        return this.state == BottomSheetBehavior.STATE_EXPANDED
    }
    set(value) {
        if (value) {
            this.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

var BottomSheetBehavior<View>.isCollapsed: Boolean
    get() {
        return this.state == BottomSheetBehavior.STATE_COLLAPSED
    }
    set(value) {
        if (value) {
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

fun <T> Class<T>.log(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e("TAXI_SERVICE", "${this.simpleName}::$message")
    }
}

val Context.isInternetConnected: Boolean
    get() {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

fun zoneToEnglish(zoneInNepali: String): String {
    var zoneToEnglish: String? = null
    when (zoneInNepali) {
        "मे" -> {
            zoneToEnglish = "ME"
        }
        "रा" -> {
            zoneToEnglish = "RA"
        }
        "बा" -> {
            zoneToEnglish = "BA"
        }
        "क" -> {
            zoneToEnglish = "KA"
        }
        "स" -> {
            zoneToEnglish = "SA"
        }
        "को" -> {
            zoneToEnglish = "KO"
        }
        "ना" -> {
            zoneToEnglish = "NA"
        }
        "म" -> {
            zoneToEnglish = "MA"
        }
        "से" -> {
            zoneToEnglish = "SE"
        }
        "लु" -> {
            zoneToEnglish = "LU"
        }
        "ज" -> {
            zoneToEnglish = "JA"
        }
        "भे" -> {
            zoneToEnglish = "BH"
        }
        "ग" -> {
            zoneToEnglish = "GA"
        }
        "ध" -> {
            zoneToEnglish = "DH"
        }
    }
    return zoneToEnglish!!
}

fun vehicleTypeToEnglish(vehicleTypeInNepali: String): String {
    var vehicleTypeToEnglish: String? = null
    when (vehicleTypeInNepali) {
        "च" -> {
            vehicleTypeToEnglish = "CHA"
        }
        "प" -> {
            vehicleTypeToEnglish = "PA"
        }
        "ज" -> {
            vehicleTypeToEnglish = "JA"
        }
    }
    return vehicleTypeToEnglish!!
}

fun zoneToNepali(zoneInEnglish: String): String {
    var zoneToNepali: String? = null
    when (zoneInEnglish) {
        "ME" -> {
            zoneToNepali = "मे"
        }
        "RA" -> {
            zoneToNepali = "रा"
        }
        "BA" -> {
            zoneToNepali = "बा"
        }
        "KA" -> {
            zoneToNepali = "क"
        }
        "SA" -> {
            zoneToNepali = "स"
        }
        "KO" -> {
            zoneToNepali = "को"
        }
        "NA" -> {
            zoneToNepali = "ना"
        }
        "MA" -> {
            zoneToNepali = "म"
        }
        "SE" -> {
            zoneToNepali = "से"
        }
        "LU" -> {
            zoneToNepali = "लु"
        }
        "JA" -> {
            zoneToNepali = "ज"
        }
        "BH" -> {
            zoneToNepali = "भे"
        }
        "GA" -> {
            zoneToNepali = "ग"
        }
        "DH" -> {
            zoneToNepali = "ध"
        }
    }
    return zoneToNepali!!
}

fun vehicleTypeToNepali(vehicleTypeInEnglish: String): String {
    var vehicleTypeToNepali: String? = null
    when (vehicleTypeInEnglish) {
        "CHA" -> {
            vehicleTypeToNepali = "च"
        }
        "PA" -> {
            vehicleTypeToNepali = "प"
        }
        "JA" -> {
            vehicleTypeToNepali = "ज"
        }
    }
    return vehicleTypeToNepali!!
}