/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.security

import android.app.Dialog
import android.content.Context
import .mvp.model.individual.IIndividualModel
import .processing.security.SecurityGetter.getSecurity

/**
 * @author Vad Nik
 * @version dated Aug 03, 2019.
 * @link https://github.com/vadniks
 */
interface Security : IIndividualModel {

    fun showPassDialog(callback: (pass: String, instance: Dialog) -> Unit)

    fun encryptDB(key: String)

    fun decryptDB(key: String)

    fun isDBEncrypted(): Boolean

    fun notifyUserEncryptionFailed()

    fun showEncryptionDialog(pass: String, callback: () -> Unit)

    fun checkPassword(pass: String): Boolean

    fun notifyUserDBNeedsDecryption()

    fun notifyUserPasswordWrong()

    fun loadKey(): String?
    
    fun deleteKey()
    
    fun showSavePasswordDialog(pass: String): Dialog
    
    companion object {
        const val PASSWORD_LENGTH = 8

        fun get(context: Context): Security = getSecurity(context)
    }
}
