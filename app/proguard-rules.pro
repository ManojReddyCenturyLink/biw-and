# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#noinspection ShrinkerUnresolvedReference

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Model/Data classes for parsing
-keep class com.centurylink.biwf.model.** { *; }
-keep class com.centurylink.biwf.service.auth$AuthServiceConfig
-keep class com.centurylink.biwf.service.impl.aasia$AssiaError
-keep class com.centurylink.biwf.service.impl.integration.** { *; }
-keep class com.centurylink.biwf.service.network.response$PasswordErrorResponse
-keep class com.centurylink.biwf.utility$PhoneNumber
-keep class com.centurylink.biwf.repos.** { *; }

# interface
-keep interface com.centurylink.biwf.coordinators$Coordinator
-keep interface com.centurylink.biwf.di.component$ApplicationComponent
-keep interface com.centurylink.biwf.service.auth$TokenStorage
-keep interface com.centurylink.biwf.service.auth$TokenService
-keep interface com.centurylink.biwf.service.auth$AuthServiceHost
-keep interface com.centurylink.biwf.service.auth$AuthService
-keep interface com.centurylink.biwf.service.auth$AccessTokenGenerator
-keep interface com.centurylink.biwf.service.integration$IntegrationServerService
-keep interface com.centurylink.biwf.service.network.** { *; }
-keep interface com.centurylink.biwf.utility.preferences$KeyValueStore

### Kotlin
#https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
#https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

#navigation-fragment-ktx
-keep class * extends android.support.v4.app.Fragment{}
-keep class * extends androidx.fragment.app.Fragment{}

-keep class com.google.crypto.tink.** { *; }

-keepclassmembernames class io.netty.** { *; }
-keepclassmembernames class org.jctools.** { *; }

##Retrofit
-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }
-keep interface retrofit2.** { *;}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotation
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

# Retain service method parameters when optimizing.
#-keepclassmembers allowshrinking,allowobfuscation * {
#    @retrofit2.http.* <methods>;
#}

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
-keepattributes InnerClasses, EnclosingMethod
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn retrofit2.adapter.rxjava.CompletableHelper$** # https://github.com/square/retrofit/issues/2034
# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>


###### OKHTTP
-keepattributes *Annotation*
#OKHttp Loggin Interceptor
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn rx.**

###GLIDE RULES
### Glide, Glide Okttp Module, Glide Transformations
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
-dontwarn jp.co.cyberagent.android.gpuimage.**


#### Rules for androidx.lifecycle:lifecycle-extensions
-keep class * extends android.arch.lifecycle.ViewModel {
    <init>();
}
-keep class * extends android.arch.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

### Retrolambda
# as per official recommendation: https://github.com/evant/gradle-retrolambda#proguard
-dontwarn java.lang.invoke.*

######  Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

###GSON
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

## keep classes and class members that implement java.io.Serializable from being removed or renamed
-keep class * implements java.io.Serializable {
    *;
}

# Hide warnings about references to newer platforms in the library
### Support v7, Design
-dontwarn android.support.v7.**
# don't process support library
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# https://github.com/Gericop/Android-Support-Preference-V7-Fix/blob/master/preference-v7/proguard-rules.pro
-keepclassmembers class android.support.v7.preference.PreferenceGroupAdapter {
    private ** mPreferenceLayouts;
}
-keepclassmembers class android.support.v7.preference.PreferenceGroupAdapter$PreferenceLayout {
    private int resId;
    private int widgetResId;
}


#####  Model class inplementation interfaces
-keep interface de.aok.digen.apis.interfaces.** { *; }


####  Material design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**


# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
### Other
-dontwarn com.google.errorprone.annotations.*

### Android Architecture Components
# Ref: https://issuetracker.google.com/issues/62113696
# LifecycleObserver's empty constructor is considered to be unused by proguard
#-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
#    <init>(...);
#}
-keep class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}

# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
    <init>(...);
}

# keep Lifecycle State and Event enums values
-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @android.arch.lifecycle.OnLifecycleEvent *;
}

## Databinding or library depends on databinding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

### Kotlin Coroutine
# https://github.com/Kotlin/kotlinx.coroutines/blob/master/README.md
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembernames class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
# https://github.com/Kotlin/kotlinx.atomicfu/issues/57
-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.coroutines.flow.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

### Kotlin
#https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
#https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
#-keepclassmembernames class kotlinx.** {
#volatile ;
#}
-dontwarn kotlin.**
-dontwarn kotlin.reflect.jvm.internal.**
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlinx.atomicfu.AtomicFU


#AndroidX
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.* { *; }

#navigation-fragment-ktx
-keep class * extends android.support.v4.app.Fragment{}
-keep class * extends androidx.fragment.app.Fragment{}

### Viewpager indicator
-dontwarn com.viewpagerindicator.**

### Junit
-dontwarn android.test.**
-ignorewarnings
-keepattributes *Annotation*
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter

#Mockito
-dontwarn org.mockito.**

# Remove logs
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

## Play services
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.*
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.internal.zzhu

##Supported internal libs
-keep class android.support.** { *; }
-keep interface android.support.* { *; }
-keep class androidx.* { *; }
-keep interface androidx.* { *; }

## Apache
-keep class org.apache.** { *; }

## Mockito
-dontwarn io.mockk.**
-keep class io.mockk.** { *; }

### Recycler View
-keep class android.support.v7.widget.RecyclerView { *; }

### Firebase
-keep class com.google.firebase.quickstart.database.java.viewholder.** {
    *;
}
-keepclassmembers class com.google.firebase.quickstart.database.java.models.** {
    *;
}
-dontwarn com.google.android.gms.measurement.AppMeasurement*
-keepclassmembers class * extends com.google.android.gms.internal.measurement.zzyv {
  <fields>;
}
-keepclassmembers class **.R$* {
 public static <fields>;
}

-keep  class androidx.security.** {
    *;
}
-keep class com.google.crypto.tink.** { *; }

-keepclassmembers class androidx.security.crypto.**{
*;
}

-keep class android.security.** { *;}

-keep class android.app.** { *;}

-dontwarn lombok.**
-keepclassmembers class * implements com.salesforce.android.sos.component.Component

-keepclassmembers class com.salesforce.android.sos.** {
    public void onEvent(...);
}
-keep class org.webrtc.** { *; }
-keepclassmembers enum com.salesforce.android.sos.** {
    **[] $VALUES;
    public *;
}
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembernames class com.salesforce.androidsdk.auth.SalesforceTLSSocketFactory { *; }


-keep class okio.** { *; }
-dontwarn okio.**
-keep class retrofit.** { *; }
-dontwarn retrofit.**
-keep class rx.** { *; }
-dontwarn rx.**
-keep class sdk.pendo.** { *; }
-dontwarn sdk.pendo.**
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-dontwarn external.sdk.pendo.io.mozilla.**
-dontwarn external.sdk.pendo.io.il.mozilla.**
-dontwarn org.slf4j.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn com.jakewharton.rxbinding.**
-keep class com.daimajia.easing.** { *; }
-keep interface com.daimajia.easing.** { *; }
-keep class external.sdk.pendo.io.mozilla.** { *; }
-keep class external.sdk.pendo.io.il.mozilla.** { *; }
-keep class org.apache.commons.lang3.** { *; }
-keep class io.reactivex.** { *; }
-dontwarn io.reactivex.**
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keep class com.trello.rxlifecycle3.** { *; }
-dontwarn com.trello.rxlifecycle3.**
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.jvm.internal.**
-keep class com.jayway.jsonpath.** { *; }
-dontwarn com.jayway.jsonpath.**
-keep class okhttp3.internal.platform.** { *; }
-dontwarn okhttp3.internal.platform.**
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepnames public class * extends android.support.v4.app.Fragment
-keepnames public class * extends android.app.Fragment
-keepnames public class * extends androidx.fragment.**
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }