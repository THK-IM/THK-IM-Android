
-keep class com.luck.picture.lib.** { *; }

# use Camerax
-keep class com.luck.lib.camerax.** { *; }

# use uCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-keep public class * extends kotlinx.parcelize.Parcelize { *; }
-keep class com.thk.im.android.preview.** { *; }
-dontwarn com.thk.im.android.preview.**