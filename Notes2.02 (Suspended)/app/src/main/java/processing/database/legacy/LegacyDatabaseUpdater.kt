/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.database.legacy

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import .R
import .common.*
import .mvp.model.individual.IIndividualModel
import .processing.common.*
import java.io.File
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws

/**
 * @author Vad Nik
 * @version dated Sep 16, 2019.
 * @link https://github.com/vadniks
 */
@Deprecated("legacy code")
@WorkerThread
class LegacyDatabaseUpdater
private constructor(
    private val context: Context,
    private val key: String?,
    private val callback: (hasUpdated: Boolean) -> Unit,
    private val im: IIndividualModel)
: SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    init {
        upgrade(readableDatabase)
    }

    override fun onCreate(db: SQLiteDatabase) = Unit

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    private fun upgrade(db: SQLiteDatabase) {
        val cursor: Cursor? =
            db.query("notes", columns, null, null, null, null, null)

        if (cursor == null) {
            callback(false)
            return
        }

        val colIdId = cursor.getColumnIndex(ID)
        val colNameId = cursor.getColumnIndex(TITLE_LEGACY)
        val colTextId = cursor.getColumnIndex(TEXT)
        val colWidId = cursor.getColumnIndex(WID)
        val colNidId = cursor.getColumnIndex(NID)
        val colRidId = cursor.getColumnIndex(RID)
        val colAddDateId = cursor.getColumnIndex(ADD_DATE)
        val colEditDateId = cursor.getColumnIndex(EDIT_DATE)
        val colSidId = cursor.getColumnIndex(SID)

        val _caught = Placeholder(false)
        val elements = ArrayList<Note>()

        while (cursor.moveToNext()) {
            onOldElementUpgrade(
                Note(
                    cursor.getInt(colIdId),
                    cursor.getString(colNameId),
                    cursor.getString(colTextId),
                    wid = cursor.getInt(colWidId).toLong(),
                    nid = cursor.getInt(colNidId).toLong(),
                    rid = cursor.getLong(colRidId),
                    addDate = cursor.getLong(colAddDateId),
                    editDate = cursor.getLong(colEditDateId),
                    sid = cursor.getLong(colSidId),
                    sid2 = -1L,
                    color = NUM_UNDEF,
                    span = null,
                    audio = false,
                    drawn = false),
                _caught,
                elements)
        }

        if (_caught.holded)
            doInUI { toast(context.getString(R.string.wrong_password), context) }

        cursor.close()

        deleteLagacyDB()

        insertElements(elements)

        callback(true)
    }

    private fun insertElements(elements: ArrayList<Note>) {
        for (i in elements)
            im.save(i, context)
    }

    private fun onOldElementUpgrade(n: Note, caught: Placeholder<Boolean>, buf: ArrayList<Note>) {
        val _n =
            try {
                decrypt(n)
            } catch (e: Exception) {
                /* TODO: debug */ e.printStackTrace()
                caught.holded = true
                n
            }

        buf.add(_n)
    }

    @Throws(Exception::class)
    private fun decrypt(n: Note): Note {
        if (key == null)
            return n

        n.title = performDecrypt(n.title)
        n.text = performDecrypt(n.text)
        n.span = performDecrypt(n.span ?: return n)

        return n
    }

    @Throws(Exception::class)
    private fun performDecrypt(text: String): String {
        val keyAdding = "00000000"

        val iv = IvParameterSpec("RandomInitVector".toByteArray(Charset.forName("UTF-8")))
        val sks = SecretKeySpec("$key$keyAdding".toByteArray(Charset.forName("UTF-8")), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, sks, iv)

        return String(cipher.doFinal(Base64.decode(text, Base64.DEFAULT)))
    }

    private fun deleteLagacyDB() {
        val folder = File(context.getDatabasePath(DB_NAME).canonicalPath.substringBeforeLast('/'))

        val def = File(folder, DB_NAME)
        if (def.exists())
            def.delete()

        val journal = File(folder, "$DB_NAME-journal")
        if (journal.exists())
            journal.delete()
    }

    companion object {
        private const val TITLE_LEGACY = "name"

        private val columns = arrayOf(
            ID,
            TITLE_LEGACY,
            TEXT,
            WID,
            NID,
            RID,
            ADD_DATE,
            EDIT_DATE,
            SID,
            COLOR,
            SPAN,
            AUDIO,
            DRAWN)

        @Deprecated("legacy code")
        @AnyThread
        fun isUpdateNeeded(context: Context): Boolean {
            val folder = File(context.getDatabasePath(DB_NAME).canonicalPath.substringBeforeLast('/'))

            val def = File(folder, DB_NAME)
            val wal = File(folder, "$DB_NAME-wal")
            val shm = File(folder, "$DB_NAME-shm")
            val journal = File(folder, "$DB_NAME-journal")

            return def.exists() && !wal.exists() && !shm.exists() && journal.exists()
        }

        @Deprecated("legacy code")
        @UiThread
        @SuppressLint("InflateParams")
        fun init(
            context: Context,
            im: IIndividualModel,
            isEncryptionAbled: Boolean,
            showCustomDialogFun:
                (title: String?,
                 view: View,
                 isCancelable: Boolean,
                 onCreateArgs: (AlertDialog.Builder) -> Unit,
                 onCreateArgs2: (Dialog) -> Unit) -> Dialog,
            callback: (hasUpdated: Boolean) -> Unit) {

            if (!isUpdateNeeded(context)) {
                callback(false)
                return
            }

            if (!isEncryptionAbled) {
                doInCoroutine { LegacyDatabaseUpdater(context, null, callback, im) }
                return
            }

            val view = LayoutInflater.from(context).inflate(R.layout.dialog_password, null)

            val field = view.findViewById<EditText>(R.id.dialog_password_field)
            field.hint = context.getString(R.string.legacy_db_upgrade_encrypted_hint)

            val bt = view.findViewById<Button>(R.id.dialog_password_bt)
            bt.setOnClickListener {
                val text = field.text()
                doInCoroutine { LegacyDatabaseUpdater(context, if (text.isBlank()) null else text, callback, im) }
            }

            showCustomDialogFun(context.getString(R.string.type_8_length_password), view, false, {}, {})
        }
    }
}
