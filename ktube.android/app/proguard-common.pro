# Begin: Common Proguard rules

# Don't note duplicate definition (Legacy Apche Http Client)
-dontnote android.net.http.*
-dontnote org.apache.http.**

# Add when compile with JDK 1.7
-keepattributes EnclosingMethod

-keep class android.support.v7.widget.SearchView { *; }

-keepclassmembers class com.appskimo.app.ktube.domain.** {
  *;
}

-dontwarn com.google.common.**
-keep class com.google.common.** { *; }
-keep interface com.google.common.** { *; }

# End: Common Proguard rules
