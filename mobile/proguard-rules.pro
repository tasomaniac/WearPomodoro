# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/taso/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes *Annotation*

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Picasso
-dontwarn com.squareup.okhttp.**

# OkHttp
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

#retrofit
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}


-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

-keepattributes Signature
-keep class sun.misc.Unsafe { *; }

# Jodatime
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# ButterKnife
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}
