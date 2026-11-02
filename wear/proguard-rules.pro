# Wear OS ProGuard rules
# Keep Retrofit annotations
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# Keep data DTOs
-keep class com.taplay.wear.data.** { *; }
# Keep NFC HCE service
-keep class com.taplay.wear.nfc.** { *; }
