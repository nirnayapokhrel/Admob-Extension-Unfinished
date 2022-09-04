# Add any ProGuard configurations specific to this
# extension here.

-keep public class com.oseamiya.sdks.sdkmore.SdkMore {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'com/oseamiya/sdks/sdkmore/repack'
-flattenpackagehierarchy
-dontpreverify
