package hu.keratomi.moneysavingcalculator.logic

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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
import hu.keratomi.moneysavingcalculator.CALCULATION_DATA_FILE_EXTENSION
import hu.keratomi.moneysavingcalculator.MainActivity
import hu.keratomi.moneysavingcalculator.R
import hu.keratomi.moneysavingcalculator.createWindowWithSavedCalculationList
import hu.keratomi.moneysavingcalculator.util.DriveServiceHelper
import java.io.File
import java.util.*

class GoogleDriveSyncHandler(val mainActivity: MainActivity) {

    val RQ_GOOGLE_SIGN_IN = 210
    private var mGoogleApiClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null

    private lateinit var filelistFromGoogleDrive: List<com.google.api.services.drive.model.File>
    private var loadedFileId: String? = null

    fun authActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RQ_GOOGLE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnFailureListener {
                println(it) // TODO
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

                mDriveServiceHelper =
                    DriveServiceHelper(googleDriveService)

                queryFileList(false)
            }
        }
    }

    fun queryFileList(withCalculationChooser: Boolean = true) {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }
        val listTask = mDriveServiceHelper?.queryFiles()
        listTask?.addOnCompleteListener {
            filelistFromGoogleDrive = listTask.result!!.files // TODO: nullpointer

            val wasSync = ifThereAreFilesLocallySyncToGoogleDriveAndDeleteLocally()

            if (wasSync) {
                queryFileList(withCalculationChooser)
            }

            if (withCalculationChooser) {
                val items = filelistFromGoogleDrive
                    .map { it.name.substringBeforeLast(CALCULATION_DATA_FILE_EXTENSION) }
                    .toTypedArray()

                createWindowWithSavedCalculationList(
                    mainActivity,
                    items,
                    readSelectedItemFormGoogleDrive
                )
            }

        }
    }

    fun uploadOrUpdateFile(file: File) {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }
        loadedFileId = filelistFromGoogleDrive.find { it.name == file.name }?.id

        val task: Task<String>?
        if (loadedFileId == null) {
            task = mDriveServiceHelper?.uploadFile(file.name, file)
        } else {
            task = mDriveServiceHelper?.updateFile(loadedFileId, file)
        }

        task?.addOnCompleteListener {
            loadedFileId = it.result
            Toast.makeText(mainActivity,
                R.string.calculation_saved, Toast.LENGTH_LONG).show()
            file.delete()
        }
    }

    fun deleteLoadedCalculation() {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }
        val deleteTask = mDriveServiceHelper?.deleteFile(loadedFileId)
        deleteTask?.addOnCompleteListener {
            Toast.makeText(mainActivity,
                R.string.calculation_deleted, Toast.LENGTH_LONG).show()
        }
    }

    fun clearLoadedFileId() {
        loadedFileId = null
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

    fun getJustCalculationNamesFromLoadedFiles(): List<String> {
        return filelistFromGoogleDrive.map { it.name.substringBeforeLast(
            CALCULATION_DATA_FILE_EXTENSION) }
    }

    private fun readFileFromGoogleDrive(fileName: String) {
        if (mDriveServiceHelper == null) {
            googleDriveIsNotWorking()
            return
        }

        loadedFileId = filelistFromGoogleDrive.find { it.name == fileName }?.id
        val readTask = mDriveServiceHelper?.readFile(loadedFileId)
        readTask?.addOnCompleteListener {
            mainActivity.loadSelectedCalculation(it.result?.first!!, it.result?.second!!)
        }
    }

    private fun ifThereAreFilesLocallySyncToGoogleDriveAndDeleteLocally(): Boolean {
        val filesOnDevice = File(mainActivity.applicationContext.filesDir.path)
            .listFiles { file -> file.name.endsWith(CALCULATION_DATA_FILE_EXTENSION) }

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
            R.string.gd_is_not_available,
            Toast.LENGTH_LONG
        ).show()
    }

    val readSelectedItemFormGoogleDrive = { dialogInterface: DialogInterface, which: Int ->
        readFileFromGoogleDrive((dialogInterface as AlertDialog).listView.adapter.getItem(which).toString() + CALCULATION_DATA_FILE_EXTENSION)
    }
}
