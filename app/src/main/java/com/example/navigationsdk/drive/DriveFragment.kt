package com.example.navigationsdk.drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.navigationsdk.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.*


class DriveFragment : Fragment(R.layout.fragment_drive), CoroutineScope by MainScope() {

    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    lateinit var observer: MyLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observer = MyLifecycleObserver(requireActivity().activityResultRegistry)
        lifecycle.addObserver(observer)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val selectButton = view.findViewById<Button>(R.id.select_button)
//
//        selectButton.setOnClickListener {
//            // Open the activity to select an image
//            observer.selectImage()
//        }
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // .requestScopes(Drive.SCOPE_FILE)
            // .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(DriveScopes.DRIVE))
            .build()
        return GoogleSignIn.getClient(requireActivity(), signInOptions)
    }

    private fun handleSignInResult(intent: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(intent)
            .addOnSuccessListener { googleAccount ->

                val credential = GoogleAccountCredential.usingOAuth2(
                    requireActivity(), listOf(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount.account

                val googleDriveService = Drive.Builder(
                    NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                launch(Dispatchers.IO) {
                    val pageToken: String? = null
                    do {
                        val result = googleDriveService.files().list().apply {
                            q = "mimeType='application/vnd.google-apps.spreadsheet'"
                            spaces = "drive"
                            fields = "nextPageToken, files(id, name)"
                            this.pageToken = pageToken
                        }.execute()
                        for (file in result.files) {
                            Log.d(file.name, file.id)
                        }
                    } while (pageToken != null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("$e", "Signing error")
            }
    }

    private fun requestSignIn() {
        val client = buildGoogleSignInClient()
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }
}