/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.impl.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Switch
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.google.android.gms.ads.AdView
import .App
import .R
import .common.*
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.IndividualModelFactory
import .mvp.model.individual.presenter.fragment.EditFragmentModel
import .mvp.presenter.commands.fragment.EditFragmentCommands
import .mvp.presenter.fragment.EditFragmentPresenter
import .mvp.presenter.viewbridge.fragment.EditFragmentViewBridge
import .mvp.presenter.viewstate.fragment.EditFragmentView
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Jul 15, 2019.
 * @link https://github.com/vadniks
 */

fun getEditFragmentPresenter(view: EditFragmentView, vararg args: Any): EditFragmentPresenter =
    EditFragmentPresenterImpl(view, *args)

private class EditFragmentPresenterImpl(view: EditFragmentView, vararg args: Any) :
    EditFragmentPresenter(view, *args), EditFragmentCommands, EditFragmentViewBridge, CrossPresenterModel.OnMenuButtonClicked {
    
    private lateinit var title: EditText
    private lateinit var text: EditText
    private var item: Note? = null
    private lateinit var context: Context
    private var selectedColor = NUM_UNDEF
    private var isSpanSet = false
    private var isGonnaSaveOutside = false
    private var hasUserChangedColor = false
    
    override val model: EditFragmentModel =
        IndividualModelFactory.imf.getPresenterModel(IndividualModelFactory.ID_EF, this) as EditFragmentModel
    
    init {
        model.init()
        model.crossPresenterModel.subscribeForMenuButton(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_edit, container, false)

        model.crossPresenterModel.setToolbar(v.findViewById(R.id.fragment_edit_toolbar))
        model.crossPresenterModel.inflateMenu(R.menu.menu_stub)

        model.crossPresenterModel.setShowHomeButton(true)

        context = v.context
        title = v.findViewById(R.id.fragment_edit_title)
        text = v.findViewById(R.id.textField)

        item = model.getNote()

        initTextFieldWatcher()
        initTextFields()

        v.findViewById<AdView>(R.id.fragment_edit_ad_view).apply {
            doInCoroutine {
                val r = model.createAdRequest()

                if (r == null) {
                    this.visibility = visibleOrGone(false)
                    return@doInCoroutine
                }

                doInUI { loadAd(r) }
            }
        }

        v.findViewById<Switch>(R.id.fragment_edit_switch).apply {
            setOnCheckedChangeListener { _, isChecked ->
                this@EditFragmentPresenterImpl.text.isFocusable = isChecked
                this@EditFragmentPresenterImpl.text.isFocusableInTouchMode = isChecked
            }

            isChecked = item == null
        }

        text.isFocusable = item == null
        text.isFocusableInTouchMode = item == null

        model.onCreateView()

        return v
    }

    override fun onResume() {
        super.onResume()

        if (item != null)
            return

        text.requestFocus()
        try {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(text, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: KotlinNullPointerException) {} catch (e: ClassCastException) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        model.crossPresenterModel.unsubscribeOfMenuButton(this)

        if (isGonnaSaveOutside)
            return

        //TODO: check notifs resetting after reboot.
        //TODO: newFIle -> DSL File
        //TODO: check ads disabling
        //TODO: check billing
        //TODO: make native ads
        //TODO: add html editor to EditFragment to edit spans
        //TODO: add audios and drawns sharing

        val n = makeNote()

        if (item == null && text.text.isNotBlank()) {
            doInCoroutine { model.save(n, context) }
            snack(
                context.getString(R.string.saved),
                model.crossPresenterModel.getRootView(),
                context.getString(R.string.undo),
                { doInCoroutine {
                    model.delete(n.apply { id = model.model.noteDao().getIdByTitle(n.title) ?: NUM_UNDEF }, context)
                } },
                true)
        } else if (item != null && n != item) {
            snack(
                n.title,
                model.crossPresenterModel.getRootView(),
                context.getString(R.string.save_ask),
                { performUpdate(n.title, n.text, n.color) },
                true)
        }
    }

    private fun performUpdate(newTitle: String, newText: String, newColor: Int) {
        doInCoroutine {
            update()

            if (model.isNoteWidgeted(item!!.id))
                model.updateWidget(context, item!!.wid, newTitle, newText, newColor)
            else if (model.isNoteNotified(item!!.id))
                model.updateNotif(
                    context, model.determineNotifMode(item!!),
                    model.chooseOfNotifIds(item!!), newTitle, newText, item!!.title, item!!.text)
        }
    }

    override fun onMenuButtonClicked() {
        if (title.text().isBlank() || text.text().isBlank()) {
            toast(context.getString(R.string.texts_empty), context)
            return
        }

        BottomSheetDialog(R.menu.menu_edit, context, model.isDark(context)) { id, instance ->
            instance.dismiss()
            var needExit = true
            when (id) {
                R.id.menu_edit_save -> {
                    doInCoroutine {
                        isGonnaSaveOutside = true
                        val n = makeNote()

                        if (item == null)
                            model.save(n, context)
                        else if (item != null)
                            performUpdate(n.title, n.text, n.color)
                    }
                }
                R.id.menu_edit_delete -> {
                    if (item != null) {
                        model.crossPresenterModel.showWarningDialog(R.string.delete, R.string.warn_delete) {
                            doInCoroutine {
                                if (model.isNoteWidgeted(item!!.id)) {
                                    model.notifyUserNoteIsNotPureUsual(context)
                                    return@doInCoroutine
                                }

                                if (!model.isNotePureUsual(item!!.id))
                                    model.dismissNotifs(item!!, context)

                                model.delete(item!!, context)
                            }
                        }
                    } else
                        needExit = false
                }
                R.id.menu_edit_color -> {
                    needExit = false
                    hasUserChangedColor = true
                    colorChoose()
                }
                R.id.menu_edit_reminder -> {
                    needExit = false

                    if (item != null && !doBlocking<Boolean> { model.isNotePureUsual(item!!.id) }) {
                        model.notifyUserNoteIsNotPureUsual(context)
                        return@BottomSheetDialog
                    }

                    isGonnaSaveOutside = true
                    model.reminderChoose(makeNote(), item != null, context)
                }
                R.id.menu_edit_force -> {
                    needExit = false
                    model.crossPresenterModel.showWarningDialog(
                        R.string.reset,
                        R.string.reset_flags_tip) {

                        if (text.text().hashCode() == 195011121)
                            toast(model.decodeHardcodedWithoutEqual(App.A), context)

                        doBlocking {
                            model.update(item?.apply {
                                wid = NUM_UNDEF.toLong()
                                nid = NUM_UNDEF.toLong()
                                rid = NUM_UNDEF.toLong()
                                sid = NUM_UNDEF.toLong()
                                sid2 = NUM_UNDEF.toLong()
                            } ?: return@doBlocking)
                        }
                    }
                }
                R.id.menu_edit_span -> {
                    if (!text.hasSelection()) {
                        toast(context.getString(R.string.noSelection), context)
                        return@BottomSheetDialog
                    }

                    needExit = false
                    spanChoose()
                }
            }

            if (needExit)
                model.crossPresenterModel.initiateMain(null)
        }.show()
    }

    override fun makeNote(): Note {
        val n = item?.copy() ?: newNote(STR_EMPTY, STR_EMPTY)

        n.apply {
            title = this@EditFragmentPresenterImpl.title.text()
            text = this@EditFragmentPresenterImpl.text.text()

            if (hasUserChangedColor)
                color = selectedColor

            if (title == STR_EMPTY)
                title = STR_UNDEF

            if (text == STR_EMPTY)
                text = STR_UNDEF
        }

        if (isSpanSet)
            n.span = HtmlCompat.toHtml(
                this@EditFragmentPresenterImpl.text.text,
                HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)

        return n
    }

    override fun getArgs(): Bundle? = view.getArgs()

    override fun setStrings(title: String, text: String) {
        this.title.text = title.toEditable()
        this.text.text = text.toEditable()
    }

    private fun update() {
        //if (model.isNotePureUsual(item!!.id))
            model.update(makeNote())
//        else
//            model.notifyUserNoteIsNotPureUsual(context)
    }

    private fun colorChoose(callback: (() -> Unit)? = null) {
        if (!model.checkIsAllAvailable(context))
            return

        BottomSheetDialog(R.menu.menu_color, context, model.isDark(context)) { id, instance ->
            instance.dismiss()
            selectedColor =
                ResourcesCompat.getColor(
                    context.resources,
                    when (id) {
                        R.id.menu_color_orange -> R.color.colOrange
                        R.id.menu_color_red -> R.color.colRed
                        R.id.menu_color_blue -> R.color.colBlue
                        R.id.menu_color_yellow -> R.color.colYellow
                        R.id.menu_color_green -> R.color.colGreen
                        R.id.menu_color_grey -> R.color.colGrey
                        R.id.menu_color_reset -> NUM_UNDEF
                        else -> throw IllegalArgumentException()
                    },
                    context.theme)

            callback?.invoke()
        }.show()
    }

    private fun spanChoose() {
        if (!model.checkIsAllAvailable(context))
            return

        BottomSheetDialog(R.menu.menu_span, context, model.isDark(context)) { id, instance ->
            instance.dismiss()

            val start = text.selectionStart
            val end = text.selectionEnd

            isSpanSet = true

            if (id == R.id.menu_span_color) {
                colorChoose {
                    for (i in text.text.getSpans(start, end, ForegroundColorSpan::class.java))
                        text.text.removeSpan(i)

                    if (selectedColor != NUM_UNDEF)
                        text.text.setSpan(ForegroundColorSpan(selectedColor), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

                    selectedColor = NUM_UNDEF
                }
                return@BottomSheetDialog
            }

            if (id == R.id.menu_span_normal) {
                for (i in text.text.getSpans(start, end, StyleSpan::class.java))
                    text.text.removeSpan(i)

                return@BottomSheetDialog
            }

            val s: Any =
                when (id) {
                    R.id.menu_span_italic -> StyleSpan(Typeface.ITALIC)
                    R.id.menu_span_bold -> StyleSpan(Typeface.BOLD)
                    R.id.menu_span_bold_italic -> StyleSpan(Typeface.BOLD_ITALIC)
                    else -> throw IllegalArgumentException()
                }

            text.text.setSpan(s, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }.show()
    }

    private fun initTextFieldWatcher(): Unit = text.addTextChangedListener(object : TextWatcher {
        private var previousTextCount = DEF_NUM
        private var previousText = STR_EMPTY

        override fun afterTextChanged(p0: Editable?) = Unit

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            previousTextCount = text.text().length
            previousText = text.text()
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (item != null)
                return

            if (title.text() == previousText)
                title.text = text.text().toEditable()
        }
    })

    private fun initTextFields() {
        val s =
            if (item?.span != null)
                HtmlCompat.fromHtml(item?.span!!, HtmlCompat.FROM_HTML_MODE_LEGACY) as SpannableStringBuilder
            else
                null

        title.isEnabled = item == null

        title.text = item?.title?.toEditable() ?: return
        text.text = s ?: item?.text?.toEditable() ?: return
    }
}
