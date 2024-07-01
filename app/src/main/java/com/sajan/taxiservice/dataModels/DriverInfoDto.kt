package com.sajan.taxiservice.dataModels

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class DriverInfoDto() : Parcelable {
    var driverId: Int = 0
    var imageUri: Uri? = null
    var imageFileName: String? = null
    var name: String? = null
    var address: String? = null
    var contact: String? = null
    var taxiDetailDto: TaxiInfoDto? = null

    constructor(parcel: Parcel) : this() {
        driverId = parcel.readInt()
        imageUri = parcel.readParcelable(Uri::class.java.classLoader)
        imageFileName = parcel.readString()
        name = parcel.readString()
        address = parcel.readString()
        contact = parcel.readString()
        taxiDetailDto = parcel.readParcelable(TaxiInfoDto::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(driverId)
        parcel.writeParcelable(imageUri, flags)
        parcel.writeString(imageFileName)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(contact)
        parcel.writeParcelable(taxiDetailDto, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "DriverInfoDto(driverId=$driverId, imageUri=$imageUri, imageFileName=$imageFileName, name=$name, address=$address, contact=$contact, taxiDetailDto=$taxiDetailDto)"
    }

    companion object CREATOR : Parcelable.Creator<DriverInfoDto> {
        override fun createFromParcel(parcel: Parcel): DriverInfoDto {
            return DriverInfoDto(parcel)
        }

        override fun newArray(size: Int): Array<DriverInfoDto?> {
            return arrayOfNulls(size)
        }
    }


}