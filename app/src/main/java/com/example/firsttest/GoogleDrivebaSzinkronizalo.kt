package com.example.firsttest

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.util.*

class GoogleDrivebaSzinkronizalo(val mainActivity: MainActivity) {

    val RQ_GOOGLE_SIGN_IN = 210
    private var mGoogleApiClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null

    private lateinit var filelistFromGoogleDrive: List<com.google.api.services.drive.model.File>

    fun authActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RQ_GOOGLE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnFailureListener {
                println(it)
            }
            task.addOnSuccessListener {
                val credential = GoogleAccountCredential.usingOAuth2(
                    mainActivity,
                    Collections.singleton(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = it.account
                val googleDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(), GsonFactory(),
                    credential
                ).setApplicationName(mainActivity.getString(R.string.app_name)).build()

                mDriveServiceHelper = DriveServiceHelper(googleDriveService)
            }
        }
    }

    fun fajlListatLeker() {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }
        val listTask = mDriveServiceHelper?.queryFiles()
        listTask?.addOnCompleteListener {
            filelistFromGoogleDrive = listTask.result!!.files // TODO: nullpointer

            val wasSync = ifThereAreFilesLocallySyncToGoogleDriveAndDeleteLocally()

            if (wasSync) {
                fajlListatLeker()
            }

            val items = filelistFromGoogleDrive
                .map { it.name.substringBeforeLast(".txt") }
                .toTypedArray()

            mainActivity.createMentettKalkulacioLista(items)

        }
    }

    fun uploadOrUpdateFile(file: File) {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }
        val fileId = filelistFromGoogleDrive.find { it.name == file.name }?.id

        val task: Task<String>?
        if (fileId == null) {
            task = mDriveServiceHelper?.uploadFile(file.name, file)
        } else {
            task = mDriveServiceHelper?.updateFile(fileId, file)
        }

        task?.addOnCompleteListener {
            Toast.makeText(mainActivity, "A fájl sikeresen mentve a Google Drive-ba", Toast.LENGTH_LONG).show()
            file.delete()
        }
    }

    fun readFileFromGoogleDrive(fileName: String) {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }

        val fileId = filelistFromGoogleDrive.find { it.name == fileName }?.id
        val readTask = mDriveServiceHelper?.readFile(fileId)
        readTask?.addOnCompleteListener {
            mainActivity.kivalasztottKalkulaciotBetolt(it.result?.first!!, it.result?.second!!)
        }
    }

    fun googleAuth() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(mainActivity.getString(R.string.web_client_id))
            .requestScopes(
                Scope(Scopes.PROFILE),
                Scope("https://www.googleapis.com/auth/drive.file")
            )
            .build()
        mGoogleApiClient = GoogleSignIn.getClient(mainActivity, signInOptions)
        startActivityForResult(
            mainActivity,
            mGoogleApiClient!!.signInIntent,
            RQ_GOOGLE_SIGN_IN,
            null
        )
    }

    private fun ifThereAreFilesLocallySyncToGoogleDriveAndDeleteLocally(): Boolean {
        val filesOnDevice = File(mainActivity.applicationContext.filesDir.path)
            .listFiles { file -> file.name.endsWith(".txt") }

        if (filesOnDevice == null || filesOnDevice.isEmpty()) {
            return false
        }

        filesOnDevice.forEach {
            uploadOrUpdateFile(it)
        }

        return true
    }

    private fun googleDriveIsNotWorking() {
        Toast.makeText(
            mainActivity,
            "A Google Drive jelenleg nem elérhető",
            Toast.LENGTH_LONG
        ).show()
    }
}
