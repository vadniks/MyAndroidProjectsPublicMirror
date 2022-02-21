/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import java.util.*

/**
 * @author Vad Nik.
 * @version dated Jan 24, 2019.
 * @link http://github.com/vadniks
 */
internal data class ChBTranslated(var owner: String, var group: String, var permissions: IntArray, var file: String) {

    companion object {
        const val PERM_READ    = 4
        const val PERM_WRITE   = 2
        const val PERM_EXECUTE = 1
        const val PERM_NONE    = 0

        const val CHB_ARR_SIZE = 9
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChBTranslated

        if (owner != other.owner) return false
        if (group != other.group) return false
        if (!Arrays.equals(permissions, other.permissions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = owner.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + Arrays.hashCode(permissions)
        return result
    }
}
