/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.databaseExport

import android.app.Dialog
import android.content.Context
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import .R
import .common.doInCoroutine
import .common.doInUI
import .common.toast
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.common.DB_NAME
import java.io.File

/**
 * @author Vad Nik
 * @version dated Jan 11, 2020.
 * @link https://github.com/vadniks
 */
private class DatabaseExportImpl(private val context: Context, im: IIndividualModel) :
    IndividualModelWrapper(im),
    DatabaseExport {

    private var callback: ((path: File) -> Unit)? = null
    private var pd: Dialog? = null

    override fun showImportExportDialog(): Dialog? {
        if (!checkIsAllAvailable(context))
            return null

        return model.crossPresenterModel.showAlertDialog {
            it.setTitle(R.string.database_export_import)
            it.setMessage(
                context.getString(R.string.db_export_tip) +
                        " ${model.getExternalStorageFolder()}" +
                        context.getString(R.string.folder))
            it.setPositiveButton(context.getText(R.string.export)) { _, _ ->
                callback = this::export
                choosePath()
            }
            it.setNegativeButton(context.getString(R.string._import)) { _, _ ->
                callback = this::importDB
                choosePath()
            }
            it.setNeutralButton(context.getString(R.string.del_db)) { _, _ ->
                callback = this::delete
                choosePath()
            }
        }
    }

    @WorkerThread
    private fun export(path: File) {
        if (!path.isDirectory) {
            toast(context.getString(R.string.not_dir), context)
            pd?.dismiss()
            return
        }

        val db = context.getDatabasePath(DB_NAME)
        val dbShm = File(db.absolutePath + "-shm")
        val dbWal = File(db.absolutePath + "-wal")

        val exported = File(path, DB_NAME)
        val exportedShm = File(exported.absolutePath + "-shm")
        val exportedWal = File(exported.absolutePath + "-wal")

        if (!db.exists() || !dbShm.exists() || !dbWal.exists()) {
            toast(context.getString(R.string.no_db), context)
            pd?.dismiss()
            return
        }

        if (!exported.exists())
            exported.createNewFile()

        if (!exportedShm.exists())
            exportedShm.createNewFile()

        if (!exportedWal.exists())
            exportedWal.createNewFile()

        if (!exported.canRead() || !exported.canWrite()) {
            toast(context.getString(R.string.no_rw_for_file_folder), context)
            pd?.dismiss()
            return
        }

        exported.writeBytes(db.readBytes())
        exportedShm.writeBytes(dbShm.readBytes())
        exportedWal.writeBytes(dbWal.readBytes())

        pd?.dismiss()
        toast(context.getString(R.string.done), context)
    }

    @WorkerThread
    private fun importDB(path: File) {
        if (!path.isDirectory) {
            toast(context.getString(R.string.not_dir), context)
            pd?.dismiss()
            return
        }

        val db = context.getDatabasePath(DB_NAME)
        val dbShm = File(db.absolutePath + "-shm")
        val dbWal = File(db.absolutePath + "-wal")

        val imported = File(path, DB_NAME)
        val importedShm = File(imported.absolutePath + "-shm")
        val importedWal = File(imported.absolutePath + "-wal")

        if (!db.exists() || !dbShm.exists() || !dbWal.exists()) {
            toast(context.getString(R.string.no_db), context)
            pd?.dismiss()
            return
        }

        if (!path.canRead() || !path.canWrite()) {
            toast(context.getString(R.string.no_rw_for_file_folder), context)
            pd?.dismiss()
            return
        }

        if (!imported.exists() || !importedShm.exists() || !importedWal.exists()) {
            toast(
                context.getString(R.string.no_dbs_tip) +
                        ' ' +
                        model.getExternalStorageFolder() +
                        context.getString(R.string.folder),
                context)
            pd?.dismiss()
            return
        }

        db.writeBytes(imported.readBytes())
        dbShm.writeBytes(importedShm.readBytes())
        dbWal.writeBytes(importedWal.readBytes())

        pd?.dismiss()
        toast(context.getString(R.string.done), context)
    }

    @WorkerThread
    private fun delete(path: File) {
        if (!path.isDirectory) {
            toast(context.getString(R.string.not_dir), context)
            pd?.dismiss()
            return
        }

        val db = context.getDatabasePath(DB_NAME)
        val dbShm = File(db.absolutePath + "-shm")
        val dbWal = File(db.absolutePath + "-wal")

        if (!db.exists() || !dbShm.exists() || !dbWal.exists()) {
            toast(context.getString(R.string.no_db), context)
            pd?.dismiss()
            return
        }

        if (!path.canRead() || !path.canWrite()) {
            toast(context.getString(R.string.no_rw_for_file_folder), context)
            pd?.dismiss()
            return
        }

        db.delete()
        dbShm.delete()
        dbWal.delete()

        pd?.dismiss()

        doInUI { model.crossPresenterModel.restart(context) }
    }

    @UiThread
    private fun choosePath() {
        val root = model.getExternalStorageFolder() ?: return
        val folder = File(root, DB_FOLDER_NAME)

        if (!folder.exists())
            folder.mkdir()

        pd = model.crossPresenterModel.showProgressDialog(context.getString(R.string.processing))
        doInCoroutine { callback?.invoke(folder) }
    }

    private companion object {
        private const val DB_FOLDER_NAME = "Database"
    }
}
