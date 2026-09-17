# Proguard rules for PayTrack
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keep class com.paytrack.app.data.model.** { *; }
