/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .processing.audios;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import .mvp.model.individual.ICommands;
import .mvp.model.individual.IndividualModelFactory;
import .processing.common.Note;

/**
 * @author Vad Nik
 * @version dated Dec 25, 2019.
 * @link https://github.com/vadniks
 */
final class AudiosGetter {

    private AudiosGetter() {}

    @NonNull
    static Audios getAudios(@NonNull Context context, @Nullable Note note) {
        return new AudiosImpl(context, note, IndividualModelFactory.imf.getWrapped(ICommands.Companion.getSTUB()));
    }
}
