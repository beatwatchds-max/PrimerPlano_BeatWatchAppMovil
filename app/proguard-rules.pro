# Preserve Retrofit's reflective service declarations and Gson response models.
-keep,allowobfuscation,allowshrinking interface * { @retrofit2.http.* <methods>; }
-keep class com.beatwatch.app.data.model.** { *; }

# Do not retain diagnostic output, including request/response values, in release APKs.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}
