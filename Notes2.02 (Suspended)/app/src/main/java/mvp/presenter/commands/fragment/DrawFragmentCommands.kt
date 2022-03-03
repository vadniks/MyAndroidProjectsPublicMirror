/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.commands.fragment

import android.content.Context
import android.os.Bundle
import .mvp.model.individual.ICommands

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
interface DrawFragmentCommands : ICommands {
    
    fun getArgs(): Bundle?
    
    fun getContext(): Context
}
