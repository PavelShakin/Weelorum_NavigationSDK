package com.example.navigationsdk.drive

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navigationsdk.R
import com.example.navigationsdk.databinding.FragmentDriveBinding
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

class DriveFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var binding: FragmentDriveBinding
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    handleSignInResult(intent)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSwitchToMapFragmentOnClick()
        requestSignIn()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun setSwitchToMapFragmentOnClick() {
        binding.switchButton.setOnClickListener {
            findNavController().navigate(R.id.action_driverFragment_to_mapFragment)
        }
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
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
        startForResult.launch(client.signInIntent)
    }
}