/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import java.io.File

/**
 * @author Vad Nik.
 * @version dated Jan 23, 2019.
 * @link http://github.com/vadniks
 */
@Deprecated("use ChBTranslated instead")
internal data class ChmodBundle(
    var file: File,

    var owner: String,
    var group: String,

    var oRead: Boolean,
    var oWrite: Boolean,
    var oExec: Boolean,

    var gRead: Boolean,
    var gWrite: Boolean,
    var gExec: Boolean,

    var aRead: Boolean,
    var aWrite: Boolean,
    var aExec: Boolean)
