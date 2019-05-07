# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn kotlin.reflect.jvm.internal.**
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontnote kotlin.internal.**
-dontwarn org.jetbrains.annotations.**

-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlin.Metadata { *; }

-dontwarn java.lang.ClassValue
-keepattributes EnclosingMethod,Deprecated,InnerClasses,AnnotationDefault,Signature,Exceptions,*Annotation*
-keepattributes SourceFile,LineNumberTable,MethodParameters

# Remove logging code
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int w(...);
    public static int d(...);
}

# Remove logging code
-assumenosideeffects class com.thomascook.instructorpricecomparison.application.EventReporter {
    public static int v(...);
    public static int w(...);
    public static int d(...);
}

-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

#Keep serializable derivatives
-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

#Google databinding
-keep class androidx.** { *; }
-keep class androidx.fragment.app.Fragment { *; }

# rxjava
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

-dontnote rx.internal.util.unsafe.**
-dontnote rx.internal.util.atomic.**

# Rules for OkHttp3 library
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontnote okhttp3.internal.platform.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Rules for retrofit library
-dontwarn retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keep,includedescriptorclasses class retrofit2.** { *; }

# Okio rules
-dontwarn org.codehaus.mojo.animal_sniffer.*

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep @com.squareup.moshi.JsonQualifier interface *

# Enum field names are used by the integrated EnumJsonAdapter.
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
}

# The name of @JsonClass types is used to look up the generated adapter.
-keepnames @com.squareup.moshi.JsonClass class *

# Retain generated JsonAdapters if annotated type is retained.
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}

-keep class kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoaderImpl

# ThirtyInch
-keep public class * implements net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctComparator
-dontwarn net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils*
-dontwarn net.grandcentrix.thirtyinch.rx.OperatorSemaphore*
-dontwarn net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler*

-keepclassmembers enum android.arch.lifecycle.Lifecycle$Event {
    <fields>;
}

-keep class android.arch.lifecycle.** {
    *;
}

-dontwarn com.google.common.**
-dontnote com.google.common.**

-keep class com.google.common.base.Joiner {
    public static com.google.common.base.Joiner on(java.lang.String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

-dontwarn jnr.posix.**
-dontwarn org.bouncycastle.mail.smime.**
-dontwarn com.kenai.jffi.**
-dontwarn com.microsoft.azure.storage.**
-dontwarn org.bouncycastle.**
-dontwarn org.slf4j.**
-keep class sun.misc.**