import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "indi.dmzz_yyhyy.lightnovelreader"
    compileSdk = 35

    defaultConfig {
        multiDexEnabled = true
        applicationId = "indi.dmzz_yyhyy.lightnovelreader"
        minSdk = 24
        targetSdk = 35
        // 版本号为x.y.z则versionCode为x*1000000+y*10000+z*100+debug版本号(开发需要时迭代, 三位数)
        versionCode = 1_01_00_011
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
        val hostname = System.getenv("HOSTNAME") ?: System.getenv("COMPUTERNAME") ?: try {
            InetAddress.getLocalHost().hostName
        } catch (_: Exception) {}
        resValue("string", "info_build_date", dateFormat.format(Date()))
        resValue("string", "info_build_host", System.getProperty("user.name") + "@" + hostname )
        resValue("string", "info_build_os", System.getProperty("os.name") + "/" + System.getProperty("os.arch"))
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            setProperty("archivesBaseName", "LightNovelReader-${defaultConfig.versionName}")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isJniDebuggable = true
            setProperty("archivesBaseName", "LightNovelReader-${defaultConfig.versionCode}")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    composeCompiler {
        includeSourceInformation = true

        featureFlags = setOf(
            ComposeFeatureFlag.StrongSkipping.disabled(),
            ComposeFeatureFlag.OptimizeNonSkippingGroups
        )
    }
}

dependencies {
    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // Android lib
    implementation(libs.androidx.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    // Compose
    implementation(libs.activity.compose)
    implementation(libs.compose.animation.graphics)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    androidTestImplementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)
    // Junit
    testImplementation(libs.junit)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.espresso.core)
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.compose)
    // coil
    implementation(libs.coil.compose)
    // jsoup
    implementation(libs.jsoup)
    // Gson
    implementation(libs.gson)
    // Markdown
    implementation(libs.markdown)
    // Ketch
    implementation(libs.ketch)
    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.rxjava2)
    implementation(libs.room.rxjava3)
    implementation(libs.room.guava)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)
    // Splash API
    implementation(libs.core.splashscreen)
    // AppCenter
    implementation(libs.appcenter.analytics)
    implementation(libs.appcenter.crashes)
    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.work.rxjava2)
    androidTestImplementation(libs.work.testing)
    implementation(libs.work.multiprocess)
    // Potato EPUB
    implementation(project(":epub"))
    implementation(libs.serialization.json)
    // Swipe
    implementation(libs.swipe)
}


configurations.implementation{
    exclude(group = "com.intellij", module = "annotations")
}

task("printVersion") {
    doFirst {
        println(android.defaultConfig.versionName)
    }
}

task("printVersionCode") {
    doFirst {
        println(android.defaultConfig.versionCode)
    }
}