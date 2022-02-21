/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * @author Vad Nik.
 * @version dated Dec 13, 2018.
 * @link http://github.com/vadniks
 */
@Suppress("deprecation")
class EditorActivity : AppCompatActivity(), EditorView {
    private lateinit var editor: TextEditor
    private lateinit var pd: ProgressDialog
    private var type: String? = null

    companion object {
        const val EXTRA_FILE = "extra_file"
    }

    override fun showToast(msg: String): Unit = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    override fun _getString(id: Int): String = getString(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        if (intent != null && intent.extras != null)
            editor = TextEditor(intent.getSerializableExtra(EXTRA_FILE) as File, this as EditorView)
        else if (intent != null && intent.data != null) {
            type = intent.type
            editor = TextEditor(File(intent.data!!.toString().substring("file://".length)), this as EditorView)
        }

        try {
            startLoading()
            launch(CommonPool) {
                Handler(Looper.getMainLooper()).post {
                    ed.text = Editable.Factory.getInstance().newEditable(editor.read())
                    stopLoading()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            //Should catch ErrnoException but it is only available on API 21 and higher.

            if (e.message != null && e.message!!.contains("premission denied", true) && !Processing.isRooted.invoke())
                Toast.makeText(this, R.string.access_tip, Toast.LENGTH_LONG).show()
            else
                throw e
        }

        if (!editor.canEdit())
            makeReadOnly(true)
    }

    private fun startLoading() {
        pd = ProgressDialog(this).apply {
            setTitle(R.string.loading)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
        pd.show()
    }

    private fun stopLoading(): Unit = pd.cancel()

    private fun makeReadOnly(flag: Boolean) {
        ed.isFocusable = !flag
        ed.isFocusableInTouchMode = !flag
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null)
            return super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.ed_save -> {
                if (!editor.canEdit()) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.access_tip, Snackbar.LENGTH_SHORT).show()
                    return false
                }

                startLoading()
                launch(CommonPool) {
                    Handler(Looper.getMainLooper()).post {
                        editor.write(ed.text.toString())
                        stopLoading()
                        finish()
                    }
                }
            }
            R.id.text_share -> {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, ed.text.toString())
                    type = type ?: "text/*"
                }, getString(R.string.choose_app)))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
