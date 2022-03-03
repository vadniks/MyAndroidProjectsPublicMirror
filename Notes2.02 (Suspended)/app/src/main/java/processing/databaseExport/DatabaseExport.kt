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
import .mvp.model.individual.IIndividualModel

/**
 * @author Vad Nik
 * @version dated Jan 11, 2020.
 * @link https://github.com/vadniks
 */
interface DatabaseExport : IIndividualModel {

    fun showImportExportDialog(): Dialog?

    companion object {

        fun get(context: Context): DatabaseExport = DatabaseExportGetter.getDatabaseExport(context)
    }
}
