package com.sajan.taxiservice.dataModels

import android.os.Parcel
import android.os.Parcelable

class TaxiInfoDto() : Parcelable {
    var driverId: Int = 0
    var taxiId: Int = 0
    var zoneCode: String? = null
    var lotNumber: String? = null
    var vehicleType: String? = null
    var taxiNumber: String? = null
    var state: String? = null
    var vehicleManagementCode: String? = null
    var vehicleRegisterSystem: String? = null

    constructor(parcel: Parcel) : this() {
        driverId = parcel.readInt()
        taxiId = parcel.readInt()
        zoneCode = parcel.readString()
        lotNumber = parcel.readString()
        vehicleType = parcel.readString()
        taxiNumber = parcel.readString()
        state = parcel.readString()
        vehicleManagementCode = parcel.readString()
        vehicleRegisterSystem = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(driverId)
        parcel.writeInt(taxiId)
        parcel.writeString(zoneCode)
        parcel.writeString(lotNumber)
        parcel.writeString(vehicleType)
        parcel.writeString(taxiNumber)
        parcel.writeString(state)
        parcel.writeString(vehicleManagementCode)
        parcel.writeString(vehicleRegisterSystem)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaxiInfoDto> {
        override fun createFromParcel(parcel: Parcel): TaxiInfoDto {
            return TaxiInfoDto(parcel)
        }

        override fun newArray(size: Int): Array<TaxiInfoDto?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "TaxiInfoDto(driverId=$driverId, " +
                "taxiId=$taxiId, " +
                "zoneCode=$zoneCode, " +
                "lotNumber=$lotNumber, " +
                "vehicleType=$vehicleType, " +
                "taxiNumber=$taxiNumber, " +
                "state=$state, " +
                "vehicleManagementCode=$vehicleManagementCode, " +
                "vehicleRegisterSystem=$vehicleRegisterSystem)"
    }
}