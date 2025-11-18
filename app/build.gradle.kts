import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.apollo.graphql)
}

android {
    namespace = "com.miraimagiclab.novelreadingapp"
    compileSdk = 36

    defaultConfig {
        multiDexEnabled = true
        applicationId = "com.miraimagiclab.novelreadingapp"
        minSdk = 24
        targetSdk = 36
        // 版本号为x.y.z则versionCode为x*1000000+y*10000+z*1000+debug版本号(开发需要时迭代, 三位数)
        versionCode = 1_02_00_007
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        resourceConfigurations += listOf("vi")
    }

    @Suppress("UnstableApiUsage")
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            vcsInfo.include = false
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
            vcsInfo.include = false
            setProperty("archivesBaseName", "LightNovelReader-${defaultConfig.versionCode}")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
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
    implementation(libs.androidx.material.icons.core)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.compose.ui.test.junit4)
    implementation(libs.kotlin.compose.compiler.plugin)
    // Junit
    testImplementation(libs.junit)
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.common)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    // Navigation
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
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    // Splash API
    implementation(libs.core.splashscreen)
    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
    // Swipe
    implementation(libs.swipe)
    // Chart
    implementation(libs.vico.compose.m3)
    // Telephoto
    implementation(libs.zoomable.image.coil)
    // Shimmer
    implementation(libs.compose.shimmer)
    // LNR API
    implementation(project(":api"))
    implementation(libs.dom4j)
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)
    // apksig
    implementation(libs.apksig)
    // Ktor Client - HTTP client cho backend API (multiplatform, hỗ trợ iOS sau này)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    // Apollo GraphQL
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.api)
    implementation(libs.apollo.normalized.cache)
    implementation(libs.apollo.normalized.cache.sqlite)
}

configurations.implementation {
    exclude(group = "com.intellij", module = "annotations")
}

tasks.register("printVersion") {
    doFirst {
        println(android.defaultConfig.versionName)
    }
}

tasks.register("printVersionCode") {
    doFirst {
        println(android.defaultConfig.versionCode)
    }
}

// Apollo GraphQL Configuration
apollo {
    service("service") {
        packageName.set("com.miraimagiclab.novelreadingapp.graphql")
        schemaFile.set(file("src/main/graphql/schema.graphqls"))
        
        // Schema file đã có, không cần introspection
        // Nếu cần download schema mới, dùng lệnh:
        // ./gradlew downloadApolloSchema --endpoint=https://ranoku.com/graphql --schema=app/src/main/graphql/schema.graphqls
    }
}