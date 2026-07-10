# ===========================================================================
#  HAVEN · R8 / ProGuard rules
# ===========================================================================

# --- Kotlinx Serialization -------------------------------------------------
# Keep @Serializable classes and their synthetic serializer companions so the
# world-model JSON round-trips correctly after shrinking.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keep,includedescriptorclasses class app.haven.**$$serializer { *; }
-keepclassmembers class app.haven.** {
    *** Companion;
}
-keepclasseswithmembers class app.haven.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room ------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# --- Health Connect --------------------------------------------------------
-keep class androidx.health.connect.client.** { *; }
-dontwarn androidx.health.connect.client.**

# --- Hilt / Dagger ---------------------------------------------------------
-keep,allowobfuscation @interface dagger.hilt.**

# --- Compose ---------------------------------------------------------------
# Compose is R8-friendly out of the box; keep only tooling-facing bits.
-dontwarn org.jetbrains.annotations.**
