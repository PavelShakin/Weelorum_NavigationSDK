package com.example.navigationsdk.drive.service

import android.app.Activity
import com.example.navigationsdk.drive.GoogleDriveConfig
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.Drive

class GoogleDriveService(
    private val activity: Activity,
    private val config: GoogleDriveConfig
    ) {

    companion object {
        private val SCOPES = setOf<Scope>(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
        val documentMimeTypes = arrayListOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document")

        const val REQUEST_CODE_OPEN_ITEM = 100
        const val REQUEST_CODE_SIGN_IN = 101
        const val TAG = "GoogleDriveService"
    }
}