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
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import androidx.annotation.MenuRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.navigation.NavigationView
import .R

/**
 * @author Vad Nik
 * @version dated Jul 15, 2019.
 * @link https://github.com/vadniks
 */
class BottomSheetDialog(
    @MenuRes private val menuId: Int,
    context: Context,
    private val isDark: Boolean,
    private val actions: (id: Int, instance: BottomSheetDialog) -> Unit) :
    com.google.android.material.bottomsheet.BottomSheetDialog(context) {

    private lateinit var menu: Menu
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_bottom_sheet)
        
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        findViewById<NavigationView>(R.id.fragment_bottom_sheet_drawer)!!.apply {
    
            if (isDark)
                setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary2, context.theme))
            
            inflateMenu(menuId)
            setNavigationItemSelectedListener {
                actions(it.itemId, this@BottomSheetDialog)
                true
            }

            this@BottomSheetDialog.menu = this.menu
        }
    }

    fun getMenu(): Menu = menu
}
