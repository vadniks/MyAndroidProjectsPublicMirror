/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.audios

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.WorkerThread
import .R
import .common.*
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.IIndividualModel
import .processing.common.Note
import java.io.File

/**
 * @author Vad Nik
 * @version dated Aug 24, 2019.
 * @link https://github.com/vadniks
 */
private class AudiosImpl(context: Context, note: Note?, im: IIndividualModel) :
    Audios(context, note, im), CrossPresenterModel.OnRequestPermissionsResult, View.OnClickListener {
    
    private lateinit var title: EditText
    private lateinit var record: Button
    private lateinit var play: Button
    private lateinit var delete: Button
    private var recorder: MediaRecorder? = null
    private lateinit var dialog: Dialog
    private var player: MediaPlayer? = null
    
    private var isRecording = false
        set(value) {
            dialog.setCancelable(value)
            dialog.setCanceledOnTouchOutside(value)
    
            record.text = context.getText(if (value) R.string.stop else R.string.record)
            
            field = value
        }
    
    private var isPlaying = false
        set(value) {
            dialog.setCancelable(!value)
            dialog.setCanceledOnTouchOutside(!value)
    
            play.text = context.getText(if (value) R.string.stop else R.string.play)
            
            field = value
        }
    
    @SuppressLint("InflateParams")
    override fun showAudioDialog() {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_audios, null)
        
        title = v.findViewById(R.id.dialog_audios_title)
        record = v.findViewById(R.id.dialog_audios_record)
        play = v.findViewById(R.id.dialog_audios_play)
        delete = v.findViewById(R.id.dialog_audios_delete)
        
        title.addTextChangedListener(makeTextWatcher())
        record.setOnClickListener(this)
        play.setOnClickListener(this)
        delete.setOnClickListener(this)
    
        title.text = note?.title?.toEditable() ?: STR_EMPTY.toEditable()
        
        title.isEnabled = !isView
        record.isEnabled = isView
        play.isEnabled = isView
        delete.isEnabled = isView
        
        if (isView) {
            if (!File(getAudiosFolder(), note!!.title + AUDIO_POSTFIX).exists()) {
                toast(context.getString(R.string.no_such_audio_file), context)
                return
            }
        }
    
        if (!checkAudioPermission())
            return
        
        dialog = model.crossPresenterModel.showCustomDialog(context.getString(R.string.audio_note), v, true)
    }
    
    private fun checkAudioPermission(): Boolean =
        if (checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_DENIED) {
    
            model.crossPresenterModel.unsubscribeOfPermissionsResult(this)
            model.crossPresenterModel.subscribeForPermissionsResult(this)
    
            model.crossPresenterModel.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
    
            false
        } else
            true
    
    private fun makeTextWatcher(): TextWatcher =
        object : TextWatcher {
            
            override fun afterTextChanged(p0: Editable?) = Unit
    
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                record.isEnabled = title.text().isNotBlank()
            }
        }
    
    override fun onClick(v: View?) {
        when (v?.id ?: return) {
            R.id.dialog_audios_record -> onRecord()
            R.id.dialog_audios_play -> onPlay()
            R.id.dialog_audios_delete -> onDelete()
        }
    }
    
    private fun onRecord() {
        if (!isRecording && !isView && checkDoubles(title.text())) {
            notifyUserNoteAlreadyExists(context)
            return
        }

        if (isPlaying)
            return
    
        title.isEnabled = false
    
        if (recorder == null && !initRecorder())
            return
        
        try {
            if (!isRecording)
                recorder!!.start()
            else {
                recorder!!.stop()
                recorder!!.release()
                recorder = null
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            toast(context.getString(R.string.err_starting_recording), context)
        }
        
        if (isRecording) {
            if (!isView)
                doInCoroutine { insert(if (!isView) title.text() else note!!.title) }
            dialog.dismiss()
        }
        
        isRecording = !isRecording
    }
    
    private fun initRecorder(): Boolean {
        val folder = getAudiosFolder()
        if (!folder.exists())
            folder.mkdir()
        
        val file = File(folder, title.text() + AUDIO_POSTFIX)
        if (!file.exists()) {
          if (!isView)
              file.createNewFile()
        } else {
            if (!isView)
                return false
        }
        
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder!!.setOutputFile(file.canonicalPath)
        
        recorder!!.setOnInfoListener { _, what, _ ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
        
                toast(context.getString(R.string.max_audio_duration), context)
        
                try {
                    recorder!!.stop()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    toast(context.getString(R.string.err_stopping_recording), context)
                }
                
                isRecording = false
                
                recorder!!.reset()
                recorder!!.release()
                recorder = null
                dialog.dismiss()
            }
        }
    
        try {
            recorder!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            toast(context.getString(R.string.err_initiating_recording), context)
            
            return false
        }
        
        return true
    }
    
    private fun onPlay() {
        if (isRecording)
            return
        
        if (player == null && !initPlayer())
            return
    
        try {
            if (isPlaying) {
                player!!.stop()
                player!!.release()
                player = null
            } else
                player!!.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            toast(context.getString(R.string.err_starting_playing), context)
        }
    
        isPlaying = !isPlaying
    }
    
    private fun initPlayer(): Boolean {
        val file = File(getAudiosFolder(), title.text() + AUDIO_POSTFIX)
        if (!file.exists())
            return false
        
        player = MediaPlayer()
        player!!.setDataSource(file.canonicalPath)
        player!!.setOnCompletionListener {
            isPlaying = false
            play.text = context.getText(R.string.play)
            
            player!!.release()
            player = null
        }
    
        try {
            player!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            toast(context.getString(R.string.err_initiating_playing), context)
            return false
        }
    
        return true
    }
    
    private fun onDelete() {
        doInCoroutine { delete(note!!, context) }
        deleteAudio(note?.title!!)
        dialog.dismiss()
    }
    
    private fun checkDoubles(title: String): Boolean =
        doBlocking<Boolean> { doesNoteAlreadyExist(title) } ||
                File(getAudiosFolder(), title + AUDIO_POSTFIX).exists()
    
    override fun deleteAllAudios() {
        for (i in getAudiosFolder().listFiles() ?: return)
            i.delete()
    }
    
    override fun deleteAudio(name: String) {
        val file = File(getAudiosFolder(), title.text() + AUDIO_POSTFIX)
        if (file.exists())
            file.delete()
    }
    
    override fun getAudiosNames(): Array<String>? =
        getAudiosFolder().listFiles()?.map { it.name.substringBefore(AUDIO_POSTFIX) }?.toTypedArray()
    
    override fun restoreAudios() {
        for (i in getAudiosNames() ?: return)
            insert(i)
    }

    override fun getFileFromTitle(t: String): File = File(getAudiosFolder(), t + AUDIO_POSTFIX)

    @WorkerThread
    private fun insert(title: String) = save(newNote(title, AUDIO_NOTATION, isAudio = true), context)
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults[permissions.indexOf(Manifest.permission.RECORD_AUDIO)] == PackageManager.PERMISSION_GRANTED)
                showAudioDialog()
            else
                notifyUserPermissionsNeeded(context)
        }
    }
    
    private fun getAudiosFolder(): File = File(model.getExternalStorageFolder(), "Audios")
    
    private companion object {
        private const val REQUEST_CODE = 0x5ed
        private const val AUDIO_POSTFIX = ".mp3"
        private const val AUDIO_NOTATION = "<audio>"
    }
}
