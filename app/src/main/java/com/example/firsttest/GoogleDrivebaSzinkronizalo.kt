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

    private var currentUploadableFile: File? = null
    private var googleFileTaskType: GoogleFileTaskType = GoogleFileTaskType.LIST

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
//                if (isRestore) {
//                    if (null != lastUploadFileId) {
//                        val downloadTask = mDriveServiceHelper?.readFile(lastUploadFileId)
//                        downloadTask?.addOnCompleteListener {
//                            println("Name==>${downloadTask.result?.first}")
//                            println("Content==>${downloadTask.result?.second}")
//                            Toast.makeText(this@MainActivity, "Backup download successfully", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                } else {
                if (googleFileTaskType == GoogleFileTaskType.UPLOAD) {
                    szinkronizal(currentUploadableFile!!)
                    currentUploadableFile = null
                } else if (googleFileTaskType == GoogleFileTaskType.LIST) {
                    fajlListatLeker()
                }
//                }
            }
        }
    }

    fun fajlListatLeker() {
        if (mDriveServiceHelper == null) {
            googleFileTaskType = GoogleFileTaskType.LIST
            googleAuth()
        } else {
            val listTask = mDriveServiceHelper?.queryFiles()
            listTask?.addOnCompleteListener {
                val fileList = listTask.result

                val items = fileList!!.files
                    .map { it.name.substringBeforeLast(".txt") }
                    .toTypedArray()

                mainActivity.createMentettKalkulacioLista(items)
            }
        }
    }


    fun szinkronizal(fajl: File) {
        if (mDriveServiceHelper == null) {
            currentUploadableFile = fajl
            googleFileTaskType = GoogleFileTaskType.UPLOAD
            googleAuth()
        } else {
            val uploadTask = mDriveServiceHelper?.uploadFile(fajl.name, fajl)
            uploadTask?.addOnCompleteListener {
                val lastUploadFileId = uploadTask.result
                Toast.makeText(mainActivity, "Backup upload successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun googleAuth() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(mainActivity.getString(R.string.web_client_id))
            .requestScopes(Scope(Scopes.PROFILE), Scope("https://www.googleapis.com/auth/drive.file"))
            .build()
        mGoogleApiClient = GoogleSignIn.getClient(mainActivity, signInOptions)
        startActivityForResult(mainActivity, mGoogleApiClient!!.signInIntent, RQ_GOOGLE_SIGN_IN, null)
    }
}