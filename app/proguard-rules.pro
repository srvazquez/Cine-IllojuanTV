-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class com.adrian.cineillojuan.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
