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
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

import com.google.zxing.*
import com.google.zxing.common.BitMatrix

import kotlinx.android.synthetic.main.activity_create.*

/**
 * @author Vad Nik.
 * @version dated August 22, 2018.
 * @link http://github.com/vadniks
 */
class CreateActivity : AppCompatActivity() {
    private val supported = BarcodeFormat.values()
    private val arr = arrayOf("AZTEC", "CODABAR", "CODE_39", "CODE_93", "CODE_128", "DATA_MATRIX", "EAN_8", "EAN_13", "ITF",
            "MAXICODE", "PDF_417", "QR_CODE", "RSS_14", "RSS_EXPANDED", "UPC-A", "UPC-E", "UPC-EAN-EXTENSION")
    private val square = arrayOf("QR_CODE", "DATA_MATRIX", "AZTEC", "MAXICODE")
    private var selected = -1
    private var isDoneFlag = false
    private var tempFileName: Uri? = null

    private companion object {
        private const val w = 500
        private const val h = 250
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        title = getString(R.string.genB)

        val ar = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        ar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ar.addAll(arr.asList())

        gen_spin.adapter = ar
        gen_spin.setSelection(11)
        gen_spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                isDoneFlag = false
                selected = id.toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                isDoneFlag = false
                selected = -1
            }
        }
        //{ _, _, _, id -> selected = arr[id.toInt()] }

        MobileAds.initialize(this, getString(R.string.adMobAppId))
        adView2.loadAd(AdRequest.Builder().build())

        gen_bt.setOnClickListener {
            if (!gen_et.text.isEmpty() && selected != -1) {
                try {
                    performCreating()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@CreateActivity, R.string.error, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.m_share -> {
                if (!isDoneFlag || tempFileName == null) {
                    Toast.makeText(this, R.string.optMError, Toast.LENGTH_LONG).show()
                    return false
                }

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, tempFileName)
                }

                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun performCreating() {
        val b = createBarcode(gen_et.text.toString(), BarcodeFormat.values()[selected],
                if (isSquare(selected)) h else w, h)

        if (b == null) {
            Toast.makeText(this, R.string.inCorDoF, Toast.LENGTH_LONG).show()
            return
        }

        val name = "Barcode_${System.currentTimeMillis()}.png"

        MediaStore.Images.Media.insertImage(contentResolver, b, name, "Barcode")

        val path = MainActivity.bitmapToPng(b, name)!!.toURI()
        gen_im.setImageBitmap(b)
        gen_im.visibility = View.VISIBLE

        Toast.makeText(this,
                getString(R.string.genFinal1) + Environment.getExternalStorageDirectory().path +
                        getString(R.string.genFinal2) + name,
                Toast.LENGTH_LONG).show()

        tempFileName = Uri.parse(path.toString())
        isDoneFlag = true
    }

    private fun isSquare(s: Int): Boolean {
        for (o in square)
            if (arr[s] == o) return true
        return false
    }

    private fun createBarcode(msg: String, format: BarcodeFormat, w: Int, h: Int): Bitmap? {
        val writer = MultiFormatWriter()
        val bm: BitMatrix?

        try {
            bm = writer.encode(Uri.encode(msg, "UTF-8"), format, w, h)
        } catch (e: Exception) {
            e.printStackTrace()
            return null // Toast.makeText(this, R.string.inCorDoF, Toast.LENGTH_LONG).show()
        }

        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        for (i in 0 until w) {
            for (j in 0 until h)
                b.setPixel(i, j, if (bm.get(i, j)) Color.BLACK else Color.WHITE)
        }

        return b
    }
}
