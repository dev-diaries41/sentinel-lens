####################################
# ONNXRuntime
####################################
-keep class ai.onnxruntime.** { *; }
-keepclassmembers class ai.onnxruntime.** {
    native <methods>;
}

####################################
# Generic native methods rule
####################################
-keepclasseswithmembernames class * {
    native <methods>;
}
