/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .common

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import .processing.common.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis
import kotlin.reflect.KFunction0

/**
 * @author Vad Nik
 * @version dated Jul 12, 2019.
 * @link https://github.com/vadniks
 */

const val DEF_NUM = 0
const val NUM_UNDEF = -1
const val DEF_ID = 1
const val STR_NONE = "<none>"
const val STR_EMPTY = ""
const val STR_UNDEF = "UNDEFINED"

fun newNote(
    title: String,
    text: String,
    id: Int = DEF_NUM,
    color: Int = NUM_UNDEF,
    isAudio: Boolean = false,
    isDrawn: Boolean = false,
    addDate: Long = currentTimeMillis(),
    editDate: Long = currentTimeMillis(),
    nid: Long = NUM_UNDEF.toLong(),
    rid: Long = NUM_UNDEF.toLong(),
    sid: Long = NUM_UNDEF.toLong(),
    sid2: Long = NUM_UNDEF.toLong(),
    span: String? = null): Note =
    Note(
        id,
        title,
        text,
        color,
        addDate,
        editDate,
        NUM_UNDEF.toLong(),
        nid,
        rid,
        sid,
        sid2,
        span,
        isAudio,
        isDrawn)

inline fun doInCoroutine(crossinline a: () -> Unit) = GlobalScope.launch(Dispatchers.IO) { a() }

fun doInUI(a: () -> Unit) = doInUIDelayed(0, a)

fun doInUIDelayed(delay: Long, a: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(a, delay)
}

/*
fun doWithProgress(context: Context, a: () -> Unit) {
    val p = ProgressDialog(context, true)
    p.show()
    doInCoroutine {
        a()
        p.dismiss()
    }
}
*/

@Suppress("NOTHING_TO_INLINE")
inline fun visibleOrGone(v: Boolean) = if (v) View.VISIBLE else View.GONE

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

@Suppress("UNUSED_PARAMETER")
fun toast(msg: String, context: Context, isLong: Boolean = false): Unit =
    doInUI { Toast.makeText(context, msg, /*if (isLong)*/ Toast.LENGTH_LONG).show() } // else Toast.LENGTH_SHORT).show() }

@Suppress("UNUSED_PARAMETER")
fun snack(msg: String, view: View, button: String?, callback: (() -> Unit)?, isLong: Boolean = false): Unit =
    doInUI {
        Snackbar.make(view, msg, /*if (isLong)*/ Snackbar.LENGTH_LONG) //else Snackbar.LENGTH_SHORT)
            .apply {
                if (button != null && callback != null)
                    setAction(button) { callback.invoke() }
            }
            .show()
    }

@Deprecated("for debug only")
fun testo(msg: String) = Log.println(Log.ASSERT, "Notes: testo", msg)

fun View.setOnClickListener(kFunction0: KFunction0<Unit>): Unit = this.setOnClickListener { kFunction0() }

fun TextView.text(): String = this.text.toString()

inline fun doBlocking(crossinline action: () -> Unit) =
    runBlocking(Dispatchers.IO) { action() }

inline fun <T> doBlocking(crossinline action: () -> T) =
    runBlocking(Dispatchers.IO) { action() }

fun logDebug(msg: String): Int = Log.println(Log.DEBUG, "Notes", msg)

fun logError(msg: String) = Log.println(Log.ERROR, "Notes: testo:", msg)

interface EventObserver

open class EventObservable<T : EventObserver> {
    protected val subs = ArrayList<T>()
    
    fun subscribe(o: T) = subs.add(o)
    
    fun unsubscribe(o: T) = subs.remove(o)
}

class FragmentStub : NavHostFragment()

@Suppress("NOTHING_TO_INLINE")
inline fun Any.isPropertyInitialized(): Boolean =
    try {
        this.hashCode()
        true
    } catch (e: UninitializedPropertyAccessException) {
        false
    }

data class Placeholder<T>(var holded: T)
