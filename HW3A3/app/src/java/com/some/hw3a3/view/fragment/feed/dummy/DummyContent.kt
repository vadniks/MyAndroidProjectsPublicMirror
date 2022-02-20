package com.some.hw3a3.view.fragment.feed.dummy

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
//@Suppress("serial")
@Suppress("ParcelCreator")
object DummyContent : Parcelable {

    override fun writeToParcel(dest: Parcel?, flags: Int) {}

    override fun describeContents(): Int = 0

    /**
     * A dummy item representing a piece of content.
     */
    data class DummyItem(var id: Int, var title: String, var image: Bitmap = defImg(), var details: String = "") : Parcelable {

        override fun toString(): String {
            return "DummyItem-$id-$title-=-$details"
        }

        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString(),
                parcel.readParcelable(Bitmap::class.java.classLoader),
                parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(title)
            parcel.writeParcelable(null, flags) // To avoid TTLE (android.os.TransactionTooLargeException).
            parcel.writeString(details)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<DummyItem> {

            override fun createFromParcel(parcel: Parcel): DummyItem {
                return DummyItem(parcel)
            }

            override fun newArray(size: Int): Array<DummyItem?> {
                return arrayOfNulls(size)
            }

            fun defImg(): Bitmap {
                val b = Bitmap.createBitmap(100, 125, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                val p = Paint().apply { color = Color.BLACK }
                c.drawRect(0F, 0F, 100F, 125F, p)

                return b
            }
        }
    }
}
