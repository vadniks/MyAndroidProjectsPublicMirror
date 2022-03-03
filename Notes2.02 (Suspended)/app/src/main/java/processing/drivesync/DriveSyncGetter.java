/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.drivesync;

import android.content.Context;

import androidx.annotation.NonNull;

import .mvp.model.individual.ICommands;
import .mvp.model.individual.IndividualModelFactory;

/**
 * @author Vad Nik
 * @version dated Dec 25, 2019.
 * @link https://github.com/vadniks
 */
final class DriveSyncGetter {

    private DriveSyncGetter() {}

    @NonNull
    static DriveSync getDriveSync(@NonNull Context context) {
        return new DriveSyncImpl(context, IndividualModelFactory.imf.getWrapped(ICommands.Companion.getSTUB()));
    }
}
