# Production Proguard & R8 configuration for Caps Learning App

# Keep Android custom views used in XML layouts
-keep public class com.eastonlearning.CapybaraView { *; }
-keep public class com.eastonlearning.SpeechBubbleView { *; }
-keep public class com.eastonlearning.OnScreenKeyboard { *; }

# Keep Activity and entry points
-keep public class com.eastonlearning.MainActivity { *; }
-keep public class com.eastonlearning.KittenTTSClient { *; }

# Keep enum types for serialization / reflection
-keepclassmembers enum com.eastonlearning.** { *; }

# Suppress annotations and attributes needed at runtime
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
