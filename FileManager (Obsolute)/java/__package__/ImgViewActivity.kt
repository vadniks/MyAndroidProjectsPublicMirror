/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_img_view.*

/**
 * @author Vad Nik.
 * @version dated Dec 23, 2018.
 * @link http://github.com/vadniks
 */
class ImgViewActivity : AppCompatActivity() {
    private lateinit var imgUri: Uri
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_view)

        if (intent != null && intent.data != null) {
            imgUri = intent.data!!
            type = intent.type
            image.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, imgUri))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_img_view, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null && item.itemId == R.id.img_share)
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, imgUri)
                type = type ?: "image/*"
            }, getString(R.string.choose_app)))

        return super.onOptionsItemSelected(item)
    }
}
