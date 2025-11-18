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

-optimizationpasses 5
-dontwarn javax.lang.model.**
-dontwarn sun.misc.**
-dontwarn org.xmlpull.v1.**
-dontwarn org.kxml2.io.**
-dontwarn android.content.res.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class com.miraimagiclab.novelreadingapp.data.json.** { *; }
-keep,allowobfuscation,allowshrinking class com.miraimagiclab.novelreadingapp.data.web.zaicomic.** { *; }
-keepclassmembers class com.miraimagiclab.novelreadingapp.data.web.zaicomic.** { *; }
-keepnames class com.miraimagiclab.novelreadingapp.data.web.zaicomic.** { *; }
-keep,allowobfuscation,allowshrinking class com.miraimagiclab.novelreadingapp.data.web.zaicomic.json.** { *; }
-keepclassmembers class com.miraimagiclab.novelreadingapp.data.web.zaicomic.json.** { *; }
-keepnames class com.miraimagiclab.novelreadingapp.data.web.zaicomic.json.** { *; }
-keep,allowobfuscation,allowshrinking class com.miraimagiclab.novelreadingapp.data.web.zaicomic.exploration.** { *; }
-keepclassmembers class com.miraimagiclab.novelreadingapp.data.web.zaicomic.exploration.** { *; }
-keepnames class com.miraimagiclab.novelreadingapp.data.web.zaicomic.exploration.** { *; }
-keepclassmembernames class com.miraimagiclab.novelreadingapp.data.web.exploration.** { *; }
-keep,allowobfuscation,allowshrinking class com.miraimagiclab.novelreadingapp.data.update.** { *; }
-keepnames class com.miraimagiclab.novelreadingapp.data.update.** { *; }
-keepclassmembernames class com.miraimagiclab.novelreadingapp.data.update.** { *; }

-keepattributes Signature, *Annotation*, InnerClasses
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.miraimagiclab.novelreadingapp.**$$serializer { *; }
-keepclassmembers class com.miraimagiclab.novelreadingapp.** {
    *** Companion;
}
-keep class com.miraimagiclab.novelreadingapp.data.** { *; }
-keep class com.miraimagiclab.novelreadingapp.utils.** { *; }
-keep class com.miraimagiclab.novelreadingapp.R$* { *; }
-keep class io.nightfish.** { *; }
-keepclasseswithmembers class com.miraimagiclab.novelreadingapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

-dontwarn org.dom4j.**
-keep class org.dom4j.**{*;}
-keep interface org.dom4j.** { *; }