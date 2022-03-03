/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.drivesync

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.annotation.WorkerThread
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import .R
import .common.doBlocking
import .common.doInCoroutine
import .common.doInUI
import .common.toast
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.common.DB_NAME
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.jvm.Throws

/**
 * @author Vad Nik
 * @version dated Aug 07, 2019.
 * @link https://github.com/vadniks
 */
private class DriveSyncImpl(private val context: Context, im: IIndividualModel) : IndividualModelWrapper(im), DriveSync, DialogInterface.OnClickListener {
    private var drive: Drive? = null
    private val dbs = arrayOf(DB_NAME, DB_SHM_NAME, DB_WAL_NAME)

    override fun signIn() {
        if (isSignedIn()) {
            handleSignIn(null)
            return
        }

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        val client = GoogleSignIn.getClient(context, options)
        model.crossPresenterModel.startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK && result != null && requestCode == REQUEST_SIGN_IN)
            handleSignIn(result)
    }
    
    private fun isSignedIn(): Boolean = GoogleSignIn.getLastSignedInAccount(context)?.account != null
    
    private fun handleSignIn(intent: Intent?) {
        if (intent != null)
            GoogleSignIn.getSignedInAccountFromIntent(intent)
                .addOnSuccessListener(this::performGettingDrive)
                .addOnFailureListener { e -> e.printStackTrace() }
        else
            performGettingDrive(GoogleSignIn.getLastSignedInAccount(context) ?: return)
    }
    
    private fun performGettingDrive(acc: GoogleSignInAccount) {
        val credentials = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE))
        credentials.selectedAccount = acc.account ?: return
        
        drive = Drive.Builder(NetHttpTransport(), GsonFactory(), credentials)
            .setApplicationName(context.getString(R.string.app_name))
            .build()
        
        showDriveDialog()
    }
    
    override fun showDriveDialog(): Dialog? {
        if (drive == null) {
            signIn()
            return null
        }
        
        return model.crossPresenterModel.showAlertDialog() { b ->
            b.setTitle(R.string.drive_operations)
            b.setPositiveButton(R.string.upload, this)
            b.setNeutralButton(R.string.delete, this)
            b.setNegativeButton(R.string.download, this)
        }
    }
    
    override fun onClick(p0: DialogInterface?, p1: Int) {
        val pd = model.crossPresenterModel.showProgressDialog(context.getString(R.string.processing))
        doInCoroutine {
            try {
                when (p1) {
                    DialogInterface.BUTTON_POSITIVE -> uploadBt()
                    DialogInterface.BUTTON_NEUTRAL -> deleteBt()
                    DialogInterface.BUTTON_NEGATIVE -> downloadBt()
                }
                pd.dismiss()
            } catch (e: IOException) {
                doInUI { toast(context.getString(R.string.drive_error), context) }
            }
        }
    }
    
    @WorkerThread
    @Throws(IOException::class)
    private fun uploadBt() {
        if (doBlocking<Boolean> { isDBEmpty(context) }) {
            toast(context.getString(R.string.db_empty), context)
            return
        }
        
        if (!areDBsCreated())
            uploadDBsToDrive()
        else
            doInUI { toast(context.getString(R.string.database_created_alrd), context) }
    }

    @WorkerThread
    @Throws(IOException::class)
    private fun deleteBt() {
        deleteAllFromDrive()
    }

    @WorkerThread
    @Throws(IOException::class)
    private fun downloadBt() {
        if (!areDBsCreated()) {
            doInUI { toast(context.getString(R.string.no_database), context, true) }
            return
        }

        downloadDBsFromDrive()
        
        doInUI {
            toast(context.getString(R.string.successful), context)
            restart(context)
        }
    }
    
    @Throws(IOException::class)
    private fun uploadDBsToDrive() {
        for (i in dbs) {
            val metadata = File()
                .setParents(Collections.singletonList(ROOT_DIR))
                .setMimeType(DB_MIME)
                .setName(i)
    
            drive!!.files().create(
                metadata,
                InputStreamContent(DB_MIME, ByteArrayInputStream(getLocalDBInBytes(i))))
                .setFields(FIELD_ID)
                .execute()
        }
        doInUI { toast(context.getString(R.string.successful), context) }
    }
    
    @Throws(IOException::class)
    private fun downloadDBsFromDrive() {
        var e = false
        
        for (i in dbs) {
            val gf = getDBFileFromDriveAsInputStream(i).apply { if (this == null) e = true } ?: continue
            val db = context.getDatabasePath(i)
    
            db.writeBytes(gf.readBytes())
        }
        doInUI {
            if (e)
                toast(context.getString(R.string.drive_error), context)
            else
                toast(context.getString(R.string.successful), context)
        }
    }

    @Deprecated("use deleteAllFromDrive instead")
    @Throws(IOException::class)
    private fun deleteDBsFromDrive() {
        var e = false
        
        for (i in dbs)
            drive!!.files().delete(getDBFileFromDrive(i)?.id.apply { if (this == null) e = true } ?: continue)
                .setFields(FIELD_ID).execute()
    
        doInUI {
            if (e)
                toast(context.getString(R.string.drive_error), context)
            else
                toast(context.getString(R.string.successful), context)
        }
    }
    
    @Throws(IOException::class)
    private fun areDBsCreated(): Boolean {
        var b = false
        
        for (i in dbs)
            b = getDBFileFromDrive(i) != null
        
        return b
    }

    private fun getLocalDB(name: String): java.io.File = context.getDatabasePath(name)
    
    private fun getLocalDBInBytes(name: String): ByteArray = getLocalDB(name).readBytes()
    
    @Throws(IOException::class)
    private fun getDBFileFromDrive(name: String): File? {
        var gf: File? = null
        
        for (i in getFilesFromDrive().files) {
            if (i.name == name)
                gf = i
        }
        
        return gf
    }
    
    @Throws(IOException::class)
    private fun getDBFileFromDriveAsInputStream(name: String): InputStream? {
        return drive!!.files().get(getDBFileFromDrive(name)?.id ?: return null).executeMediaAsInputStream()
    }
    
    @Throws(IOException::class)
    private fun getFilesFromDrive(): FileList = drive!!.files().list()
        .setSpaces(ROOT_DIR)
        .setFields("files(id, name)")
        .execute()
    
    @Throws(IOException::class)
    private fun deleteAllFromDrive() {
        for (i in getFilesFromDrive().files)
            drive!!.files().delete(i.id).setFields(FIELD_ID).execute()
        
        doInUI { toast(context.getString(R.string.successful), context) }
    }
    
    private companion object {
        private const val REQUEST_SIGN_IN = 0x00000a5f
        
        private const val DB_MIME = "application/x-sqlite3"
    
        private const val DB_SHM_NAME = DB_NAME + "-shm"
        private const val DB_WAL_NAME = DB_NAME + "-wal"
        
        private const val ROOT_DIR = "appDataFolder"

        private const val FIELD_ID = "id"
    }
}
