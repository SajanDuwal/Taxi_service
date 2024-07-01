package com.sajan.taxiservice.protocols

interface OnPostResponseListener {
    fun onStarted(url: String)
    fun onComplete(message: String)
    fun onError(result: String?)
}