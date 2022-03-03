/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.common

import androidx.room.Entity
import androidx.room.PrimaryKey
import .common.DEF_NUM
import java.io.Serializable

/**
 * @author Vad Nik
 * @version dated Jul 12, 2019.
 * @link https://github.com/vadniks
 */
@Suppress("SERIAL")
@Entity(tableName = DB_NAME)
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = DEF_NUM,
    var title: String,
    var text: String,
    var color: Int,
    var addDate: Long,
    var editDate: Long,
    var wid: Long,
    var nid: Long,
    var rid: Long,
    var sid: Long,
    var sid2: Long,
    var span: String?,
    var audio: Boolean,
    var drawn: Boolean) : Serializable
