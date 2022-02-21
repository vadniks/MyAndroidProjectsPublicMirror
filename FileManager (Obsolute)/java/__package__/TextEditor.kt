/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

//import android.system.ErrnoException
import java.io.File
import java.nio.charset.Charset

/**
 * @author Vad Nik.
 * @version dated Dec 13, 2018.
 * @link http://github.com/vadniks
 */
internal class TextEditor(private val file: File, private val view: EditorView) {
    private var canRead = true
    private var canEdit = true

    companion object {
        const val SUPPORTED_FILE_EXTENSION = "txt"
    }

    fun canEdit(): Boolean = canEdit

    init {
        canRead = file.canRead()
        canEdit = if (Processing.isRooted.invoke()) true else file.canWrite()
    }

    /**
     * @throws [ErrnoException] if access denied.
     * Only on API 19 and higher.
     */
    //@Throws(ErrnoException::class)
    fun read(): String {
        return if (Processing.isRooted.invoke() && !file.canRead()) {
            String(rootRead(file.path)/*.apply {
                for (i in this)
                    println("testo byte $i")
                println("testo byte arr ${String(this, Charset.forName("US-ASCII"))}")
            }*/)
//            try {
//                rootRead(file.path) ?: ""
//            } catch (ex: Exception) {
//                view.showToast(view._getString(R.string.errRead))
//                ""
//            }/*.toString()*/
        } else file.readText()
    }

    /**
     * @throws [ErrnoException] if access denied.
     * Only on API 19 and higher.
     */
    //@Throws(ErrnoException::class)
    fun write(text: String) =
        if (Processing.isRooted.invoke() && !file.canRead())
            rootWrite(file.path, text)
        else
            file.writeText(text)

    private external fun rootRead(file: String): ByteArray

    private external fun rootWrite(file: String, text: String)
}
