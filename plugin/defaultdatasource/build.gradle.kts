plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.nightfish.defaultdatasource"
    defaultConfig {
        multiDexEnabled = true
        minSdk = 24
    }
    compileSdk = 36

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":app"))
    implementation(project(":proxy"))
    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
}

tasks.register<Exec>("jarToDexWithD8") {
    dependsOn(":plugin:defaultdatasource:assemble")

    val sdkDir = project.android.sdkDirectory
    val buildToolsVersion by extra { "${project.android.compileSdk}.0.0" }
    val buildToolsDir by extra { "$sdkDir\\build-tools\\$buildToolsVersion" }
    val compileSdkVersion = project.android.compileSdk

    workingDir(File(buildToolsDir))
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        commandLine("cmd", "/c", "d8")
    } else {
        commandLine("sh", "-c", "./d8")
    }
    println(layout.buildDirectory.get())
    args(
        "--lib", "$sdkDir\\platforms\\android-$compileSdkVersion\\android.jar",
        "--output", "${layout.buildDirectory.get()}\\outputs\\dex",
        "${layout.buildDirectory.get()}\\intermediates\\aar_main_jar\\release\\syncReleaseLibJars\\classes.jar"
    )

    doFirst {
        File("${layout.buildDirectory.get()}\\outputs\\dex").mkdirs()
    }
}

tasks.register<Delete>("cleanAssets") {
    delete(project(":app").layout.buildDirectory.dir("generated/assets"))
}

tasks.register<Copy>("copyDexToAssets") {
    dependsOn(":plugin:defaultdatasource:cleanAssets")
    dependsOn(":plugin:defaultdatasource:jarToDexWithD8")
    from(layout.buildDirectory.dir("outputs/dex"))
    into(project(":app").layout.buildDirectory.dir("generated/assets"))
    rename("classes", "defaultdatasource")
}