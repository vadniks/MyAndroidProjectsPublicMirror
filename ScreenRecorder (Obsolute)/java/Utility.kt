/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.os.Environment

/**
 * @author Vad Nik.
 * @version dated August 31, 2018.
 * @link http://github.com/vadniks
 */

internal const val  START_NOTIF                      =  ".START_NOTIF"

/** stop foreground service */
internal const val  END_NOTIF                        =  ".END_NOTIF"

internal const val  START_RECORD                     =  ".START_RECORD"
internal const val  STOP_RECORD                      =  ".STOP_RECORD"

/**
 * [android.hardware.display.VirtualDisplay]'s name
 */
internal const val  V_D_NAME                         =  "ScreenRecordingVD"

/**
 * directory's path
 * (in which recorded *.mp4 files are saved)
 */
internal val        DIR:          String             =  Environment.getExternalStorageDirectory().path + "/ScreenRecorder"

internal const val  EXTRA_SP                         =  "EXTRA_SP"

/** Notification id */
internal const val  EXTRA_NID                        =  "EXTRA_NID"

/** start activity */
internal const val  START_ACT                        =  ".START_ACTIVITY"

/** Notification channel id */
internal const val  CHID                             =  "Screen_recording"

/** is recording */
internal const val  EXTRA_IS_REC                     =  "EXTRA_IS_REC"

/**
 * update the [RecordActivity]'s button text
 */
internal const val  UPDATE_BT                        =  ".UPDATE_BT"

/**
 * if true 'start recording',
 * 'stop recording' otherwise.
 */
internal const val  EXTRA_UBT                        =  "EXTRA_UBT"

/**
 * notifies the Activity to call
 * [RecordActivity.startActivityForResult]
 * to request the screen recording permission
 */
internal const val REQUEST_PERM                      =  ".REQUEST_PERM"

/**
 * if true Activity will need to
 * request the screen recording permission
 */
internal const val EXTRA_IS_R_P                      =  "EXTRA_IS_R_P"

/** lets user send recording */
internal const val SHARE                             =  ".SHARE"

/** channel id for share notification */
internal const val CHID_2                            =  "Screen_recording_share"
