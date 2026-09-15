# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Gson model classes (parsed/serialized via reflection)
-keep class com.nima.app.imanage.data.model.** { *; }

# Room entities serialized via Gson reflection for backup/restore
# (field names must match the DB column names)
-keep class com.nima.app.imanage.data.db.entity.** { *; }

# WorkManager worker (instantiated via reflection)
-keep class com.nima.app.imanage.worker.** { *; }

# Gson / reflection support
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep line numbers for readable crash logs
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit service interfaces (safe even if unused)
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
