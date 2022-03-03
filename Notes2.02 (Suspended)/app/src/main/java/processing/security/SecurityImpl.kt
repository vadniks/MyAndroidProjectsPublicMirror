/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.security

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import com.commonsware.cwac.saferoom.SQLCipherUtils
import .R
import .common.doInCoroutine
import .common.text
import .common.toEditable
import .common.toast
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.common.DB_NAME
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @author Vad Nik
 * @version dated Aug 03, 2019.
 * @link https://github.com/vadniks
 */
private class SecurityImpl(private val context: Context, im: IIndividualModel) : IndividualModelWrapper(im), Security {

    @SuppressLint("InflateParams")
    override fun showPassDialog(callback: (pass: String, instance: Dialog) -> Unit) {
        lateinit var d: Dialog

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_password, null)
        
        view.findViewById<TextView>(R.id.dialog_password_title)
            .setTextColor(if (isDark(context)) Color.WHITE else Color.BLACK)

        val field = view.findViewById<EditText>(R.id.dialog_password_field)
        field.setTextColor(if (isDark(context)) Color.WHITE else Color.BLACK)
        field.addTextChangedListener(object : TextWatcher {
            
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (field.text().length == Security.PASSWORD_LENGTH)
                    callback(field.text(), d)
            }
        })

        d = model.crossPresenterModel.showCustomDialog(null, view, false)
    }

    override fun encryptDB(key: String) {
        if (SQLCipherUtils.getDatabaseState(context, DB_NAME) == SQLCipherUtils.State.UNENCRYPTED)
            SQLCipherUtils.encrypt(context, DB_NAME, key.toEditable())
    }

    override fun decryptDB(key: String) {
        if (SQLCipherUtils.getDatabaseState(context, DB_NAME) == SQLCipherUtils.State.ENCRYPTED)
            SQLCipherUtils.decrypt(context, context.getDatabasePath(DB_NAME), key.toByteArray())
    }

    override fun isDBEncrypted(): Boolean =
        SQLCipherUtils.getDatabaseState(context, DB_NAME) == SQLCipherUtils.State.ENCRYPTED

    override fun notifyUserEncryptionFailed(): Unit =
        toast(context.getString(R.string.ecryption_failed), context)

    override fun showEncryptionDialog(pass: String, callback: () -> Unit) {
        model.crossPresenterModel.showAlertDialog { b ->
            b.setTitle(R.string.database_encryption)
            b.setPositiveButton(R.string.encrypt) { _, _ -> performCrypt(pass, false, callback) }
            b.setNegativeButton(R.string.decrypt) { _, _ -> performCrypt(pass, true, callback) }
        }
    }

    override fun checkPassword(pass: String): Boolean {
        var db: SQLiteDatabase? = null
        var ret = true
        try {
            db = SQLiteDatabase.openDatabase(
                context.getDatabasePath(DB_NAME).absolutePath,
                pass,
                null,
                SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
            ret = false
        } finally {
            db?.close()
        }
        return ret
    }

    override fun notifyUserDBNeedsDecryption(): Unit =
        toast(context.getString(R.string.db_needs_decryption), context)

    override fun notifyUserPasswordWrong(): Unit =
        toast(context.getString(R.string.wrong_password), context)

    private fun performCrypt(key: String, decrypt: Boolean, callback: () -> Unit) {
        val pd = model.crossPresenterModel.showProgressDialog(context.getString(R.string.processing))
        doInCoroutine {
            if (!decrypt)
                encryptDB(key)
            else
                decryptDB(key)
            
            if (isKeySaved())
                deleteKey()
            
            pd.dismiss()
            callback()
        }
    }
    
    private fun performPasswordSaving(pass: String, delete: Boolean) {
        if (delete) {
            deleteKey()
            toast(context.getString(R.string.key_deleted), context)
        } else {
            savePassword(pass)
            toast(context.getString(R.string.key_saved), context)
        }
    }
    
    private fun savePassword(pass: String) {
        val key = File(context.cacheDir, FILE_KEY)
        key.writeBytes(encryptKey(pass))
    }
    
    override fun loadKey(): String? {
        val key = File(context.cacheDir, FILE_KEY)
        
        if (!isKeySaved())
            return null
        
        val k = decryptKey(key.readBytes())
        
        if (!checkPassword(k))
            return null
        
        return k
    }
    
    override fun deleteKey() {
        val key = File(context.cacheDir, FILE_KEY)
        
        if (isKeySaved())
            key.delete()
    }
    
    private fun isKeySaved(): Boolean = File(context.cacheDir, FILE_KEY).exists()
    
    private fun encryptKey(key: String): ByteArray =
        performMessageEncryption(false, key.toByteArray(), Base64.decode(model.getKeyForEncryption(), 0))
    
    private fun decryptKey(key: ByteArray): String =
        String(performMessageEncryption(true, key, Base64.decode(model.getKeyForEncryption(), 0)))
    
    private fun performMessageEncryption(decrypt: Boolean, msg: ByteArray, key: ByteArray): ByteArray {
        val iv = IvParameterSpec(decodeHardcodedWithoutEquals("").toByteArray())
        // RandomInitVector
        val sks = SecretKeySpec(key, decodeHardcoded(""))
        // AES
        val cipher = Cipher.getInstance(decodeHardcodedWithoutEqual(""))
        // AES/CBC/PKCS5PADDING
        cipher.init(if (!decrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, sks, iv)
        
        return cipher.doFinal(msg)
    }
    
    override fun showSavePasswordDialog(pass: String): Dialog =
        model.crossPresenterModel.showAlertDialog { d ->
            d.setTitle(R.string.remember_password)
            d.setPositiveButton(R.string.save) { _, _ -> performPasswordSaving(pass, false) }
            d.setNegativeButton(R.string.delete) { _, _ -> performPasswordSaving(pass, true) }
        }

    private companion object {
        private const val FILE_KEY = "a"
    }
}
