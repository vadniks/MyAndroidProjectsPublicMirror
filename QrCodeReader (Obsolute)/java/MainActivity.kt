/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

import kotlinx.android.synthetic.main.content_main.*

import java.io.*
import kotlin.system.exitProcess

/**
 * @author Vad Nik.
 * @version dated August 19, 2018.
 * @link http://github.com/vadniks
 */
final class MainActivity : AppCompatActivity()/*, NavigationView.OnNavigationItemSelectedListener*/ {
    private lateinit var camSrc: CameraSource
    private lateinit var detector: Detector<Barcode>
    private var isFlashOn = false

    internal companion object {
        private const val CAMERA_REQUEST: Int = 1888
        //private const val CAMERA_PERMISSION_CODE: Int = 100
        private val path: String = Environment.getExternalStorageDirectory().absolutePath + "/QR code reader"
        private val compressedBitmapName: String = "Bitmap_${System.currentTimeMillis()}"

        private const val TAG: String = "QRCodeReader"

        @Suppress("deprecation")
        private var pdg: ProgressDialog? = null

        internal fun writeFile(arr: ByteArray) {
            val fileName: String = path + "Barcode_${System.currentTimeMillis()}"

            val dir = File(path)

            if (!dir.exists())
                dir.mkdirs()

            val writer = FileOutputStream(fileName)
            writer.write(arr)
            writer.flush()
            writer.close()
        }

        internal fun writeFile(msg: String) {
            val fileName: String = path + "Barcode_${System.currentTimeMillis()}"

            val dir = File(path)

            if (!dir.exists())
                dir.mkdirs()

            val writer = FileWriter(fileName)
            writer.write(msg)
            writer.flush()
            writer.close()
        }

        internal fun readFile(fileName: String): ByteArray {
            val reader = FileInputStream(fileName)

            val arr: ByteArray = reader.readBytes()

            reader.close()
            return arr
        }

        internal fun readFile2(fileName: String): String {
            val reader = FileReader(fileName)

            val msg: String = reader.readText()

            reader.close()
            return msg
        }

        internal fun fileToBitmap(file: File): Bitmap = BitmapFactory.decodeStream(file.inputStream())

        internal fun bitmapToFile(bitmap: Bitmap, context: Context, name: String = "Image_${System.currentTimeMillis()}"): File? {
            val file = File(Environment.getExternalStorageDirectory().path + "/QR Code Reader/" + name)
            val b = file.createNewFile()

            val out: OutputStream = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()

            return if (b) file else null
        }

        internal fun bitmapToPng(bitmap: Bitmap, name: String = "Image_${System.currentTimeMillis()}"): File? {
            val a: String = Environment.getExternalStorageDirectory().path + "/QR Code Reader"
            val s: String = "$a/$name"

            val dir = File(a)
            if (!dir.exists())
                dir.mkdirs()

            val fos = FileOutputStream(s)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

            val f = File(s)

            return if (f.exists()) f else null
        }

        internal fun isPng(file: File): Boolean = file.name.endsWith("png", true)

        @Suppress("deprecation")
        internal fun startLoading(context: Context) {
            val pd = ProgressDialog(context)
            pd.setCancelable(false)
            pd.setTitle(R.string.processing)
            pd.show()
            pdg = pd
        }

        @Suppress("deprecation")
        internal fun stopLoading() {
            pdg!!.dismiss()
            pdg = null
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return

        if (hasCamP() && hasReadP() && hasWriteP())
            return

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
    }

    private fun hasCamP(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun hasReadP(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED

    private fun hasWriteP(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            finish()
            startActivity(baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        } else {
            Toast.makeText(this, R.string.permErroe, Toast.LENGTH_LONG).show()
            exitProcess(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.content_main)
        //setSupportActionBar(toolbar)

//        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open,
//                R.string.navigation_drawer_close)
//        drawer_layout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        nav_view.setNavigationItemSelectedListener(this)

        //scan_dn.setOnClickListener { _ -> scan() }

        requestPermissions()

        scan_upload.setOnClickListener { _ -> upload() }

        var barcodes: SparseArray<Barcode>? = null

        scan_dn.isEnabled = false
        scan_dn.setOnClickListener {
            if (barcodes!!.size() > 0)
                Handler(Looper.getMainLooper()).post {
                    showDialog(getString(R.string.barcode_information), barcodes!!.valueAt(0).displayValue)
                    scan_dn.isEnabled = false
                }
        }

        scan_nav.setOnClickListener { startActivity(Intent(this, CreateActivity::class.java)) }

        detector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
        detector.setProcessor(object : Detector.Processor<Barcode> {

            override fun release() {}

            override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
                barcodes = p0!!.detectedItems
                Handler(Looper.getMainLooper()).post {
                    scan_dn.isEnabled = true
                }
            }
        })

        camSrc = CameraSource.Builder(this, detector)
                //.setRequestedPreviewSize(640, 640)
                .setAutoFocusEnabled(true)
                .build()

        scan_flash.setOnClickListener {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                Toast.makeText(this@MainActivity, R.string.flashE, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            light(!isFlashOn)
            isFlashOn = !isFlashOn
        }

        MobileAds.initialize(this, getString(R.string.adMobAppId))
        adView.loadAd(AdRequest.Builder().build())

        scan_img.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d("test", "created")
                camSrc.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                Log.d("test", "changed")
                camSrc.start(scan_img.holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Log.d("test", "destroyed")
            }
        })

        //TODO("add ads")

        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true)
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { it -> recieveUpload(it) }

//        if (intent.action == Intent.ACTION_SENDTO && intent.type?.startsWith("image/") == true)
//            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { it -> recieveUpload(it) }
    }

    @Suppress("deprecation")
    private fun light(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], enable)
        } else {
            val cam = getCamera(camSrc)
            val parameters = cam?.parameters

            if (enable) {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                cam?.parameters = parameters
                //cam.startPreview()
            } else {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                cam?.parameters = parameters
                //cam.stopPreview()
                //cam.release()
            }
        }
    }

    @Suppress("deprecation")
    private fun getCamera(cs: CameraSource): Camera? {
        val field = CameraSource::class.java.declaredFields

        for (o in field) {
            if (o.type == Camera::class.java) {
                o.isAccessible = true
                return o.get(cs) as Camera
            }
        }
        return null
    }

//    override fun onBackPressed() {
//        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
//            drawer_layout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        camSrc.stop()
    }

    //TODO: add manual focus.

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.nav_generate -> {
//                startActivity(Intent(this, CreateActivity::class.java))
//            }
////            R.id.nav_3party -> {
////                val ad = AlertDialog.Builder(this)
//////                ad.setTitle(R.string.pb)
//////                ad.setMessage("Google android gms vision\nOnBarcode (onbarcode.com)")
////            }
//        }
//
//        drawer_layout.closeDrawer(GravityCompat.START)
//        return true
//    }

    private fun upload() {
        val intent = Intent(Intent.ACTION_PICK)//, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        //Intent(Intent.ACTION_GET_CONTENT)
        //intent.type = "*/*"
        //intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(intent, 0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun readBarcode(b: Bitmap): String? {
        val arr = IntArray(b.width.times(b.height))
        b.getPixels(arr, 0, b.width, 0, 0, b.width, b.height)

        val ls = RGBLuminanceSource(b.width, b.height, arr)
        val bm = BinaryBitmap(HybridBinarizer(ls))

        val res: Result?
        try {
            res = MultiFormatReader().decode(bm)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return res.text
    }

    @Deprecated("better alternative found")
    private fun scan() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == CAMERA_PERMISSION_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                scan()
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap = data!!.extras.get("data") as Bitmap

            //scan_img.setImageBitmap(bitmap)

            val value = decode(bitmap)

            if (value != null)
                showDialog(getString(R.string.barcode_information), value)
            else
                Toast.makeText(applicationContext, R.string.barcodeError2, Toast.LENGTH_LONG).show()

        } else if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return

            //startLoading(this)
            recieveUpload(data.data)
        } else if (requestCode == 100 && resultCode != Activity.RESULT_OK)
            exitProcess(0)
    }

    private fun recieveUpload(uri: Uri) {
        if (uri.scheme.toLowerCase() != "content")
            return

        val file = File(getPath(uri))

        if (file.isFile) {
            val bitmap: Bitmap?

            try {
                val opt = BitmapFactory.Options()
                opt.inPreferredConfig = Bitmap.Config.ARGB_8888
                bitmap = BitmapFactory.decodeFile(file.absolutePath, opt)//fileToBitmap(file)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(applicationContext, R.string.barcodeError2, Toast.LENGTH_LONG).show()
                Log.e(TAG, getString(R.string.barcodeError2))
                return
            }

            //scan_img.setImageBitmap(bitmap)

//                val fileName: String
//                if (!isPng(file)) {
//                    fileName = bitmapToPng(bitmap)
//
//                    val b: Bitmap = fileToBitmap(File(fileName))
//                    bitmap = b
//                }
            val value = readBarcode(bitmap)//decode(bitmap)

            if (value != null)
                showDialog(getString(R.string.barcode_information), value)
            else
                Toast.makeText(applicationContext, R.string.barcodeError2, Toast.LENGTH_LONG).show()
        } else
            Toast.makeText(applicationContext, R.string.selectError, Toast.LENGTH_LONG).show()
    }

    private fun getPath(uri: Uri): String? {
        Log.d("test", uri.path)

        val cursor: Cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)

        Log.d("test", cursor.moveToFirst().toString())
        val s: String? = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        Log.d("test", s)
        cursor.close()
        return s

        //try {
//            val cursor: Cursor = contentResolver.query(uri, arrayOf("_data"),
//                    null, null, null)
//            val index: Int = cursor.getColumnIndexOrThrow("_data")
//
//            if (cursor.moveToFirst()) {
//                val s: String? = cursor.getString(index)
//                Log.d(TAG, (s == null).toString() + " " + s)
//                cursor.close()
//                return s
//            }
        //} catch (ex: Exception) {
        //    ex.printStackTrace()
        //} finally {
            //cursor.close()
        //}
        //return null
    }

    private fun decode(bitmap: Bitmap): String? {
        val detector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()

        if (!detector.isOperational) {
            Toast.makeText(applicationContext, R.string.barcodeError, Toast.LENGTH_LONG).show()
            return null
        }

        val frame = Frame.Builder()
                .setBitmap(bitmap)
                .build()

        var barcodes: SparseArray<Barcode>? = null

        try {
            barcodes = detector.detect(frame)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, R.string.barcodeError3, Toast.LENGTH_LONG).show()
            Log.e(TAG, getString(R.string.barcodeError3))
        }

        val barcode: Barcode

        if (barcodes!!.size() > 0)
            barcode = barcodes.valueAt(0)
        else {
            //barcodes = detector.detect()
            //Toast.makeText(applicationContext, R.string.barcodeError2, Toast.LENGTH_LONG).show()
            return null
        }

        bitmapToPng(bitmap)

        return try {
            barcode.rawValue
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, R.string.barcodeError2, Toast.LENGTH_LONG).show()
            Log.e(TAG, getString(R.string.barcodeError2))
            null
        }
    }

    private fun showDialog(title: String, msg: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(title)
        dialog.setMessage(msg)
        dialog.setNeutralButton(R.string.copy) { _, _ ->
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            // The super ^ 2 method is nullable, but idea thinks otherwise.

            val cd = if (msg.toLowerCase().contains("http://"))
                ClipData.newHtmlText(getString(R.string.copied_text), msg, msg)
            else
                ClipData.newPlainText(getString(R.string.copied_text), msg)

            cm?.primaryClip = cd
            Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show()
        }
        dialog.setCancelable(true)
        dialog.show()
    }
}
