package com.example.navigationsdk.drive.service

import com.google.api.services.drive.model.File

interface ServiceListener {
    fun loggedIn()
    fun fileDownloaded(file: File)
    fun cancelled()
    fun handleError(exception: Exception)
}