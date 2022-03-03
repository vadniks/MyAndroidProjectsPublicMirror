#
# Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
#
# This code is a part of proprietary software.
# Usage, distribution, redistribution, modifying 
# and/or commercial use of this code,
# without author's written permission, are strongly prohibited. 
#

-verbose
-printmapping mapping.txt
-forceprocessing

-dontoptimize

-keepclassmembers public class .processing.commo.Note {
    public <fields>;
}

-keep class androidx.appcompat.widget.SearchView { *; }
-keep class com.google.firebase.provider.FirebaseInitProvider
-keep class com.android.vending.billing.** { *; }

-keepclassmembers class net.sqlcipher.database.** {
    <fields>;
    native <methods>;
 }
-keepclassmembers class net.sqlcipher.** {
    <fields>;
    native <methods>;
 }
-keep public class net.sqlcipher.database.SQLiteException { *; }

-keep public class com.google.android.gms.ads.** { *; }
-keep public class com.google.ads.** { *; }
-keep public class com.google.android.gms.internal.ads.** { *; }
-keep public class com.google.android.gms.ads.internal.** { *; }
-keep public class com.google.android.gms.ads.identifier.** { *; }
-keep public class com.google.android.gms.ads.** { *; }
-keep public class com.google.android.gms.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-keep class com.google.android.gms.ads.internal.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.** { *; }

-keep public class com.google.android.gms.ads.AdActivity { *; }
-keep class com.google.android.gms.ads.AdActivity { *; }

-keep public class com.google.** { *; }
-keep class com.google.** { *; }

-keep public class com.google.android.gms.ads.internal.overlay.** { *; }
-keep class com.google.android.gms.ads.internal.overlay.** { *; }

-keep public class com.google.android.gms.internal.ads_identifier.** { *; }
-keep class com.google.android.gms.internal.ads_identifier.** { *; }

-keep public class com.google.ads.mediation.** { *; }
-keep class com.google.ads.mediation.** { *; }

-keep public class com.google.ads.mediation.admob.** { *; }
-keep class com.google.ads.mediation.admob.** { *; }

-keep public class com.google.ads.mediation.customevent.** { *; }
-keep class com.google.ads.mediation.customevent.** { *; }

-keep public class com.google.android.gms.ads.appopen.** { *; }
-keep class com.google.android.gms.ads.appopen.** { *; }

-keep public class com.google.android.gms.ads.consent.** { *; }
-keep class com.google.android.gms.ads.consent.** { *; }

-keep public class com.google.android.gms.ads.doubleclick.** { *; }
-keep class com.google.android.gms.ads.doubleclick.** { *; }

-keep public class com.google.android.gms.ads.formats.** { *; }
-keep class com.google.android.gms.ads.formats.** { *; }

-keep public class com.google.android.gms.ads.initialization.** { *; }
-keep class com.google.android.gms.ads.initialization.** { *; }

-keep public class com.google.android.gms.ads.internal.overlay.** { *; }
-keep class com.google.android.gms.ads.internal.overlay.** { *; }

-keep public class com.google.android.gms.ads.mediation.** { *; }
-keep class com.google.android.gms.ads.mediation.** { *; }

-keep public class com.google.android.gms.ads.mediation.admob.** { *; }
-keep class com.google.android.gms.ads.mediation.admob.** { *; }

-keep public class com.google.android.gms.ads.mediation.customevent.** { *; }
-keep class com.google.android.gms.ads.mediation.customevent.** { *; }

-keep public class com.google.android.gms.ads.mediation.rtb.** { *; }
-keep class com.google.android.gms.ads.mediation.rtb.** { *; }

-keep public class com.google.android.gms.dynamite.** { *; }
-keep class com.google.android.gms.dynamite.** { *; }

-keep class * extends java.util.ListResourceBundle {
   protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
   public static final ** CREATOR;
}

-keepattributes *Annotation*

-optimizations class/marking/final
#-optimizations code/merging
#-optimizations code/simplification/string
#-optimizations code/removal/advanced
#-optimizations code/simplification/arithmetic
#-optimizations code/simplification/advanced
#-optimizations method/inlining/short
#-optimizations method/inlining/unique

#-overloadaggressively
#-allowaccessmodification
#-mergeinterfacesaggressively

#-adaptclassstrings
#-adaptresourcefilenames
#-adaptresourcefilecontents

-android
#-optimizationpasses 5

-dontwarn kotlin.**
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void throwUninitializedProperty(java.lang.String);
    public static void throwUninitializedPropertyAccessException(java.lang.String);
    public static void throwIllegalArgument(java.lang.String);
    public static void throwIllegalState(java.lang.String);
    public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
}
-assumenosideeffects public @interface kotlin.Metadata {
    public <fields>;
    <fields>;
    public <methods>;
    <methods>;
}
-assumenosideeffects public @interface kotlin.coroutines.jvm.internal.DebugMetadata {
    public <fields>;
    <fields>;
    public <methods>;
    <methods>;
}
-assumenosideeffects public class kotlin.TypeCastException

# -assumenosideeffects assertNotMainThread
