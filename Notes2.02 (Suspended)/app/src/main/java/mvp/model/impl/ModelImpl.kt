/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.impl

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import .R
import .common.*
import .mvp.model.CrossPresenterModel
import .mvp.model.Model
import .mvp.model.Model.Companion.LOGS_FOLDER_POSTFIX
import .mvp.model.Model.Companion.LOG_FILE
import .mvp.model.individual.ICommands
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelFactory
import .mvp.view.MainActivity
import .processing.billing.Billing
import .processing.common.Note
import .processing.common.broadcastreceiver.BroadcastReceiver
import .processing.database.NoteDao
import .processing.database.NotesDatabase
import .processing.database.legacy.LegacyDatabaseUpdater
import .processing.drivesync.DriveSync
import .processing.firebasemessaging.FirebaseMessagingServiceDelegate
import .processing.notifications.Notifications
import .processing.security.Security
import .processing.single.UpdateChecker
import .processing.widgets.Widgets
import io.fabric.sdk.android.Fabric
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */

object ModelAccess {
    private var model: Model? = null
    
    fun initModel(context: Context): Model {
        if (model != null)
            throw RuntimeException()
            
        ModelImpl(context)
            .apply { model = this }

        return model!!
    }
}

private class ModelImpl(private val context: Context) : Model {
    override var password: String? = null
    private lateinit var driveSync: DriveSync
    private val individualModel: IIndividualModel
    private lateinit var interstitialAd: InterstitialAd
    override var isActivityShown: Boolean = false

    override val crossPresenterModel: CrossPresenterModel =
        getCrossPresenterModel(getIndividualModel(ICommands.STUB, this))

    init {
        crossPresenterModel.subscribeForActivityStart(this)
        crossPresenterModel.subscribeForUIRendered(this)
        crossPresenterModel.subscribeForPermissionsResult(this)
        
        IndividualModelFactory(crossPresenterModel, this)
        individualModel = IndividualModelFactory.imf.getWrapped(ICommands.STUB)

        val s = Security.get(context)
        
        if (s.isDBEncrypted())
            password = s.loadKey()
        
        if (s.isDBEncrypted() && password != null && !s.checkPassword(password!!))
            password = null

        if (!individualModel.areAdsDisabled(context))
            doInCoroutine {
                MobileAds.initialize(
                    context,
                    individualModel.decodeHardcodedWithoutEqual(""))
            }

            doInCoroutine {
                interstitialAd = InterstitialAd(context)
                interstitialAd.adUnitId =
                    individualModel.decodeHardcodedWithoutEqual("")

                //val r = createAdRequest()
                //doInUI { interstitialAd.loadAd(r ?: return@doInUI) }
            }

        doInCoroutine { Fabric.with(context, Crashlytics()) }
        doInCoroutine { Billing.get(context).startForChecking {
            individualModel.setDark(context, individualModel.isDark(context) && it.areAllEnabled()) }
        }
        doInCoroutine { FirebaseMessagingServiceDelegate.get(context).subscribeToAllTopic() }
    }

    override fun onUIRendered() {
        doInCoroutine { UpdateChecker(context, individualModel) }

        if (crossPresenterModel.checkSelfPermission2(context, Manifest.permission.READ_EXTERNAL_STORAGE) &&
            crossPresenterModel.checkSelfPermission2(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            return

        crossPresenterModel.requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
            R_W_PERM_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != R_W_PERM_REQUEST)
            return

        var b = false
        for (i in grantResults)
            b = i == PackageManager.PERMISSION_GRANTED

        if (!b) {
            toast(context.getString(R.string.need_these_permissions), context)
            doInUIDelayed(1000) { exitProcess(0) }
        }
    }

    override fun handleUncoughtException(t: Thread, e: Throwable) {
        e.printStackTrace()

        try {
            crashlyticsLog(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val storage = getExternalStorageFolder() ?: return

        try {
            val folder = File(storage, LOGS_FOLDER_POSTFIX)

            if (!folder.exists())
                folder.mkdir()

            val log = File(folder, LOG_FILE)

            if (!log.exists())
                log.createNewFile()

            val fos = FileOutputStream(log, true)
            fos.write((makeDateTime() + '\n').toByteArray())
            e.printStackTrace(PrintStream(fos, true))
            fos.write('\n'.toByte().toInt())

            fos.flush()
            fos.close()
        } catch (e2: Exception) {
            //ignore in release //e2.printStackTrace()
        }

        exitProcess(1)
    }

    private fun crashlyticsLog(e: Throwable) {
        val crshCore = CrashlyticsCore.getInstance()
        val crshControllerField = crshCore::class.java.getDeclaredField("controller")
        crshControllerField.isAccessible = true

        //TODO: don't obfuscate Crashlytics in proguard.

        val crshController = crshControllerField.get(crshCore)

        val crshLogMethod = Class.forName("com.crashlytics.android.core.CrashlyticsController")
            .getDeclaredMethod("doWriteNonFatal", Date::class.java, Thread::class.java, Throwable::class.java)

        crshLogMethod.isAccessible = true
        crshLogMethod.invoke(crshController, Date(), Thread.currentThread(), e)
        crshLogMethod.isAccessible = false

        crshControllerField.isAccessible = false
    }

    override fun logToFile(msg: String) {
        val folder = File(getExternalStorageFolder() ?: return, LOGS_FOLDER_POSTFIX)

        if (!folder.exists())
            folder.mkdir()

        val log = File(folder, LOG_FILE)

        if (!log.exists())
            log.createNewFile()

        val fos = FileOutputStream(log, true)
        fos.write((makeDateTime() + msg).toByteArray())
        fos.write('\n'.toByte().toInt())

        fos.flush()
        fos.close()
    }

    @SuppressLint("SimpleDateFormat")
    override fun makeDateTime(): String =
        SimpleDateFormat("MM.dd.yyyy-HH:mm").format(Date())

    override fun getExternalStorageFolder(): File? {
        val f = context.getExternalFilesDir(null)

        if (f != null && !f.exists())
            f.mkdirs()

        return f
    }

    override fun noteDao(): NoteDao = NotesDatabase.getInstance(context, password).noteDao()
    
    override fun onReceivedEventForDecryption(event: Int, intent: Intent?) {
        if (password != null) {
            performEventHandling(event, intent)
            return
        }
        
        context.startActivity(Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_EVENT, event)
            .putExtra(EXTRA_EVENT_INTENT, intent))
        Security.get(context).notifyUserDBNeedsDecryption()
    }
    
    override fun resetReminders() {
        for (i in noteDao().getAllNonPureNonWidgetedNotes() ?: return) {
            if (i.nid != NUM_UNDEF.toLong())
                Notifications.create(context).resetReminder(Notifications.MODE_NOTIF, i.title, i.text, i.nid)
            else if (i.rid != NUM_UNDEF.toLong())
                Notifications.create(context).resetReminder(Notifications.MODE_REM, i.title, i.text, i.rid)
            else if (i.sid != NUM_UNDEF.toLong() && i.sid2 != NUM_UNDEF.toLong())
                Notifications.create(context).resetReminder(Notifications.MODE_SCH, i.title, i.text, i.sid, i.sid2)
        }
    }
    
    override fun resetWidgets() {
        for (i in noteDao().getAllWidgetedNotes() ?: return)
            Widgets.getDelegate().updateWidget(
                context,
                i.wid,
                i.title,
                i.text,
                i.color)
    }

    @Suppress("deprecation")
    override fun onActivityStarted(): Unit =
        performInitialPasswordAsking {
            driveSync = DriveSync.create(context)
            
            crossPresenterModel.unsubscribeOfActivityResult(driveSync)
            crossPresenterModel.subscribeForActivityResult(driveSync)

            LegacyDatabaseUpdater.init(
                context,
                individualModel,
                Billing.get(context).areAllEnabled(),
                crossPresenterModel::showCustomDialog) {

                if (it) {
                    individualModel.restart(context)
                    return@init
                }

                if (proceedForUnusual())
                    crossPresenterModel.initiateMain(null)

                performEventHandling(
                    crossPresenterModel.getIntent().getIntExtra(EXTRA_EVENT, NUM_UNDEF),
                    crossPresenterModel.getIntent().getParcelableExtra(EXTRA_EVENT_INTENT) as Intent?)
            }
        }
    
    private fun performEventHandling(event: Int, intent: Intent?) {
        when (event) {
            IIndividualModel.EVENT_NOTIFICATIONS -> Notifications.create(context, intent!!)
            IIndividualModel.EVENT_BOOT_COMPLETED -> doInCoroutine {
                resetReminders()
                resetWidgets()
            }
            IIndividualModel.EVENT_DELETE_WIDGET -> {
                doInCoroutine {
                    for (i in intent?.getIntArrayExtra(Widgets.EXTRA_WIDGET_ID)?.iterator() ?:
                        emptyArray<Int>().iterator())
                        individualModel.update(Widgets.getDelegate().getNoteByWidgetId(i.toLong())
                            ?.apply { wid = NUM_UNDEF.toLong() } ?: continue)
                }
            }
        }
    }
    
    private fun proceedForUnusual(): Boolean {
        val intent = crossPresenterModel.getIntent()
        
        return when (intent.action) {
            Widgets.ACTION_WIDGET_CONFIGURE -> {
                proceedForWidgetConfiguring()
                false
            }
            BroadcastReceiver.makeAction(context, BroadcastReceiver.ACTION_OPEN) -> {
                proceedForOpen()
                false
            }
            BroadcastReceiver.makeAction(context, Widgets.ACTION_OPEN_WIDGET) -> {
                proceedForOpen()
                false
            }
            else -> {
                return if (intent.getLongExtra(BroadcastReceiver.EXTRA_ID, NUM_UNDEF.toLong()) != NUM_UNDEF.toLong() &&
                    intent.getIntExtra(BroadcastReceiver.EXTRA_MODE, NUM_UNDEF) != NUM_UNDEF) {
                    proceedForOpen()
                    false
                } else
                    true
            }
        }
    }
    
    private fun proceedForWidgetConfiguring() {
        if (!individualModel.checkIsAllAvailable(context))
            return

        if (crossPresenterModel.getIntent().action != Widgets.ACTION_WIDGET_CONFIGURE)
            return

        crossPresenterModel.setActivityResult(Activity.RESULT_CANCELED, null)

        val widgetId = crossPresenterModel.getIntent().getIntExtra(Widgets.EXTRA_WIDGET_ID, DEF_NUM).toLong()
        
        if (widgetId == DEF_NUM.toLong()) {
            crossPresenterModel.finishActivity()
            return
        }

        crossPresenterModel.queueForMenuCreation {
            it?.findItem(R.id.menu_main_actions_search)?.isEnabled = false
            it?.findItem(R.id.menu_main_actions_stub)?.isEnabled = false
        }
        crossPresenterModel.initiateMain(Bundle().apply { putLong(Widgets.EXTRA_WIDGET_ID, widgetId) })
    }
    
    private fun proceedForOpen() {
        val id = crossPresenterModel.getIntent().getLongExtra(BroadcastReceiver.EXTRA_ID, NUM_UNDEF.toLong())
        val mode = crossPresenterModel.getIntent().getIntExtra(BroadcastReceiver.EXTRA_MODE, NUM_UNDEF)
        
        if (id == NUM_UNDEF.toLong() || mode == NUM_UNDEF)
            return
        
        val n =
            if (mode != Widgets.MODE_WIDGET)
                doBlocking<Note?> { Notifications.create(context).getNoteByNotificationsId(id, mode) }
            else if (individualModel.checkIsAllAvailable(context))
                doBlocking<Note?> { Widgets.getDelegate().getNoteByWidgetId(id) }
            else
                null
        
        if (n == null) {
            toast(context.getString(R.string.noteNotFound), context)
            return
        }
        
        if (mode == Notifications.MODE_REM)
            doInCoroutine { individualModel.update(n.apply { rid = NUM_UNDEF.toLong() }) }
        
        if (id != NUM_UNDEF.toLong())
            crossPresenterModel.initiateViewing(Model.MODE_NOTE_USUAL, n)
    }
    
    private fun performInitialPasswordAsking(action: () -> Unit) {
        val s = Security.get(context)

        if (!s.isDBEncrypted() || password != null) {
            action()
            return
        }

        s.showPassDialog { pass, instance ->
            if (s.checkPassword(pass)) {
                password = pass
                action()
                instance.dismiss()
            } else
                s.notifyUserPasswordWrong()
        }
    }
    
    override fun driveOperations() {
        if (individualModel.checkIsAllAvailable(context))
            driveSync.showDriveDialog()
    }

    override fun getKeyForEncryption(): String = ""

    override fun rememberPassword() {
        if (!individualModel.checkIsAllAvailable(context))
            return

        val s = Security.get(context)

        if (!s.isDBEncrypted()) {
            toast(context.getString(R.string.encrypt_database_first), context)
            return
        }

        s.showPassDialog { pass, instance ->
            if (!s.checkPassword(pass)) {
                toast(context.getString(R.string.wrong_password), context)
                return@showPassDialog
            }

            instance.dismiss()
            s.showSavePasswordDialog(pass)
        }
    }

    override fun createAdRequest(): AdRequest? =
        if (individualModel.areAdsDisabled(context))
            null
        else
            AdRequest.Builder().build()

    override fun showInterstitial() {
//        if (!interstitialAd.isPropertyInitialized())
//            return
//
//        if (!interstitialAd.isLoaded) {
//            doInCoroutine {
//                val ar = createAdRequest()
//                doInUI lb@ { interstitialAd.loadAd(ar ?: return@lb) }
//            }
//        } else
//            doInUI { interstitialAd.show() }
    }

    private companion object {
        private const val EXTRA_EVENT = 0x00000002.toString()
        private const val EXTRA_EVENT_INTENT = 0x00000004.toString()
        private const val R_W_PERM_REQUEST = 3241
    }
}
