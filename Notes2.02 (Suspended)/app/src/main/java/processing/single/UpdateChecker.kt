/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.single

import android.content.Context
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import .BuildConfig
import .R
import .common.doInUI
import .common.doInUIDelayed
import .common.toast
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import kotlin.jvm.Throws
import kotlin.system.exitProcess

/**
 * @author Vad Nik
 * @version dated Sep 15, 2019.
 * @link https://github.com/vadniks
 */
@WorkerThread
class UpdateChecker(context: Context, im: IIndividualModel) : IndividualModelWrapper(im) {

    init {
        var version = 0.0f
        try {
            version = load()
        } catch (e: IOException) {
            // ignored
        } finally {
            if (check(version, context))
                doInUI { showDialog() }
            //finish(context)
        }
    }

    @UiThread
    private fun showDialog() = model
        .crossPresenterModel
        .showAlertDialog(
            {
                it.setCancelable(false)
                it.setCanceledOnTouchOutside(false)
                it
            },
            {
                it.setTitle(R.string.update_is_needed)
                it.setMessage(R.string.update_needed)
                it.setPositiveButton(R.string.ok) { _, _ -> exitProcess(0) }
            })

    private fun check(version: Float, context: Context): Boolean {
        val b =
            version > BuildConfig.VERSION_NAME.toFloat() || (version == 0.0f && isUpdateNeeded(context))

        setUpdateNeeded(b, context)
        return b
    }

    @Deprecated("replaced")
    private fun finish(context: Context) {
        toast(context.getString(R.string.update_needed), context)
        doInUIDelayed(4000) { exitProcess(0) }
    }

    @Throws(IOException::class)
    private fun load(): Float {
        val url = decodeHardcodedWithoutEqual("")
        val userAgent = decodeHardcodedWithoutEqual("")

        val doc: Document? =  Jsoup
            .connect(url)
            .timeout(30000)
            .userAgent(userAgent)
            .referrer(decodeHardcodedWithoutEquals(""))
            .get()

        for (i in doc?.getElementsContainingOwnText(decodeHardcoded("")) ?: return 0.0f) {
            for (j in i?.siblingElements() ?: continue)
                return j.text().toFloatOrNull() ?: continue
        }

        return 0.0f
    }

    private fun isUpdateNeeded(context: Context): Boolean = getBoolean(IS_UPDATE_NEEDED_KEY, context)

    private fun setUpdateNeeded(b: Boolean, context: Context) = setBoolean(IS_UPDATE_NEEDED_KEY, context, b)

    private companion object {
        private const val IS_UPDATE_NEEDED_KEY = 0x34.toString()
    }
}
