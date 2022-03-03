/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import .mvp.model.individual.ICommands;
import .mvp.model.individual.IndividualModelFactory;
import .processing.common.Note;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * @author Vad Nik
 * @version dated Dec 25, 2019.
 * @link https://github.com/vadniks
 */
final class NotificationsGetter {

    private NotificationsGetter() {}

    @NonNull
    static Notifications create(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String text,
            int mode,
            @NonNull Function2<Integer, long[], Unit> onInsert) {

        return new NotificationsImpl(
                context,
                title,
                text,
                mode,
                IndividualModelFactory.imf.getWrapped(ICommands.Companion.getSTUB()),
                onInsert);
    }

    @NonNull
    static Notifications create(
            @NonNull Context context,
            @NonNull Note n,
            int mode,
            @NonNull Function2<Integer, long[], Unit> onInser) {

        return new NotificationsImpl(
                context,
                n,
                mode,
                IndividualModelFactory.imf.getWrapped(ICommands.Companion.getSTUB()),
                onInser);
    }

    @NonNull
    static Notifications create(@NonNull Context context, @NonNull Intent intent) {
        return new NotificationsImpl(
                context,
                intent,
                IndividualModelFactory.imf.getWrapped(ICommands.Companion.getSTUB()));
    }

    @NonNull
    static Notifications create(@NonNull Context context) {
        return new NotificationsImpl(
                context,
                IndividualModelFactory.imf.getWrapped(ICommands.Companion.getSTUB()));
    }
}
