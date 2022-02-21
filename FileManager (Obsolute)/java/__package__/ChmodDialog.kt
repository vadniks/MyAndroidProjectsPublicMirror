/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_change_permissions.*

/**
 * @author Vad Nik.
 * @version dated Jan 23, 2019.
 * @link http://github.com/vadniks
 */
internal class ChmodDialog(private val chb: ChBTranslated, context: Context, private val onDone: (res: ChBTranslated) -> Unit) :
    Dialog(context, true, {}), View.OnClickListener {
    private lateinit var arr: Array<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_permissions)

        arr = arrayOf(
            dcp_ownerRead,
            dcp_ownerWrite,
            dcp_ownerExecute,
            dcp_groupRead,
            dcp_groupWrite,
            dcp_groupExecute,
            dcp_anyoneRead,
            dcp_anyoneWrite,
            dcp_anyoneExecute
        )

        setDefaults()

        dcp_done.setOnClickListener(this)
        dcp_restore.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            dcp_done.id -> {
                if (dcp_ownerText.text.isBlank() || dcp_groupText.text.isBlank()) {
                    Toast.makeText(super.getContext(), super.getContext().getString(R.string.emptyTip), Toast.LENGTH_SHORT).show()
                    return
                }

                val p = removePrefixes()

                val res = ChBTranslated(
                    p.first,
                    p.second,
                    setRWXes(),
                    chb.file)

                super.cancel()
                onDone.invoke(res)
            }
            dcp_restore.id -> setDefaults()
        }
    }

    private fun setDefaults() {
        addPrefixes(chb.owner, chb.group)

        for ((j, i) in arr.withIndex()) {
            val a = chb.permissions[j]
            i.isChecked = a == ChBTranslated.PERM_READ ||
                    a == ChBTranslated.PERM_WRITE ||
                    a == ChBTranslated.PERM_EXECUTE
        }
    }

    private fun setRWXes(): IntArray {
        val ar = IntArray(ChBTranslated.CHB_ARR_SIZE)
        for ((j, i) in arr.withIndex()) {
            when (i.text) {
                super.getContext().getString(R.string.read) -> ar[j] =
                        if (i.isChecked) ChBTranslated.PERM_READ else ChBTranslated.PERM_NONE
                super.getContext().getString(R.string.write) -> ar[j] =
                        if (i.isChecked) ChBTranslated.PERM_WRITE else ChBTranslated.PERM_NONE
                super.getContext().getString(R.string.execute) -> ar[j] =
                        if (i.isChecked) ChBTranslated.PERM_EXECUTE else ChBTranslated.PERM_NONE
            }
        }
        return ar
    }

//        arrayOf(
//            if (dcp_ownerRead.isChecked)     ChBTranslated.PERM_READ    else ChBTranslated.PERM_NONE,
//            if (dcp_ownerWrite.isChecked)    ChBTranslated.PERM_WRITE   else ChBTranslated.PERM_NONE,
//            if (dcp_ownerExecute.isChecked)  ChBTranslated.PERM_EXECUTE else ChBTranslated.PERM_NONE,
//            if (dcp_groupRead.isChecked)     ChBTranslated.PERM_READ    else ChBTranslated.PERM_NONE,
//            if (dcp_groupWrite.isChecked)    ChBTranslated.PERM_WRITE   else ChBTranslated.PERM_NONE,
//            if (dcp_groupExecute.isChecked)  ChBTranslated.PERM_EXECUTE else ChBTranslated.PERM_NONE,
//            if (dcp_anyoneRead.isChecked)    ChBTranslated.PERM_READ    else ChBTranslated.PERM_NONE,
//            if (dcp_anyoneWrite.isChecked)   ChBTranslated.PERM_WRITE   else ChBTranslated.PERM_NONE,
//            if (dcp_anyoneExecute.isChecked) ChBTranslated.PERM_EXECUTE else ChBTranslated.PERM_NONE).toIntArray()

    @SuppressLint("SetTextI18n")
    private fun addPrefixes(owner: String, group: String) {
        dcp_ownerText.text = "${super.getContext().getString(R.string.owner)} $owner"
        dcp_groupText.text = "${super.getContext().getString(R.string.group)} $group"
    }

    private fun removePrefixes(): Pair<String, String> {
        val o = dcp_ownerText.text.toString().substring(5)
        val g = dcp_groupText.text.toString().substring(5)
        return Pair(o, g)
    }
}
