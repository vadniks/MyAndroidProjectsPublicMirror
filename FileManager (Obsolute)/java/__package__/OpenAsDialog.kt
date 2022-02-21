/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.dialog_open_as.*

/**
 * @author Vad Nik.
 * @version dated Dec 20, 2018.
 * @link http://github.com/vadniks
 */
internal class OpenAsDialog(
    context: Context,
    private val actions: Array<() -> Unit>,
    private val items: ArrayList<String> = ArrayList()
) : Dialog(context, true, {}) {
    private lateinit var adapter: ArrayAdapter<String>

    companion object {
        private const val TEXT    = "Text"
        private const val IMAGE   = "Image"
        private const val VIDEO   = "Video"
        private const val MUSIC   = "Music" //TODO: rename to 'audio'.
        private const val ARCHIVE = "Archive"
        private const val OTHER   = "Other"

        const val TEXT_INT    = 0
        const val IMAGE_INT   = 1
        const val VIDEO_INT   = 2
        const val MUSIC_INT   = 3
        const val ARCHIVE_INT = 4
        const val OTHER_INT   = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_open_as)

        items.addAll(arrayOf(
            TEXT,/*
            IMAGE,
            VIDEO, TODO: add these.
            MUSIC,
            ARCHIVE,*/
            OTHER))

        adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)

        doa.adapter = adapter
        doa.setOnItemClickListener { _, _, _, id ->
            when (items[id.toInt()]) {
                TEXT    -> actions[TEXT_INT].invoke()
                IMAGE   -> actions[IMAGE_INT].invoke()
                VIDEO   -> actions[VIDEO_INT].invoke()
                MUSIC   -> actions[MUSIC_INT].invoke()
                ARCHIVE -> actions[ARCHIVE_INT].invoke()
                OTHER   -> actions[OTHER_INT].invoke()
            }
        }
    }
}
