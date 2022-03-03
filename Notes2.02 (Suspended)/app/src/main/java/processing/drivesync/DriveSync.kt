/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.drivesync

import android.app.Dialog
import android.content.Context
import android.content.Intent
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.IIndividualModel
import .processing.drivesync.DriveSyncGetter.getDriveSync

/**
 * @author Vad Nik
 * @version dated Aug 07, 2019.
 * @link https://github.com/vadniks
 */
interface DriveSync : IIndividualModel, CrossPresenterModel.OnActivityResult {

    fun signIn()

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?)

    fun showDriveDialog(): Dialog?

    companion object {

        fun create(context: Context): DriveSync = getDriveSync(context)
    }
}
