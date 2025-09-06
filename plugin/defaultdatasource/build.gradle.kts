

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.gradleup.shadow") version "8.3.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compileOptions {
            jvmToolchain(17)
        }
    }
}

val shadow by configurations.register("shadowRuntime")

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    compileOnly(project(":proxy"))
    compileOnly(project(":app"))
    compileOnly(libs.androidx.navigation.runtime.ktx)
    compileOnly(libs.gson)
    compileOnly(libs.jsoup)
    compileOnly(libs.kotlinx.coroutines.core)
}

/*
tasks.register<ShadowJar>("margeShadowJar") {
    dependsOn(":plugin:defaultdatasource:assemble")
    dependsOn(":plugin:defaultdatasource:resolveRuntimeClasspath")
    configurations = listOf(shadow)
    project.configurations.asMap["implementation"]?.dependencies?.let { implementation ->
        val api = project.configurations.asMap["api"]?.dependencies
        implementation.forEach {
            if (it is DefaultProjectDependency) return@forEach
            api?.add(it)
        }
    }
    exclude("org.jetbrains.annotations.*")
    from(layout.buildDirectory.dir("tmp/kotlin-classes/release"))
    minimize()
}

tasks.register("resolveRuntimeClasspath") {
    dependsOn(":plugin:defaultdatasource:assemble")
    rootProject.allprojects {
        if (!name.contains("app") && !name.contains("defaultdatasource")) return@allprojects
        val releaseRuntimeClasspath = configurations.findByName("releaseRuntimeClasspath")
        configurations.maybeRegister("runtimeClasspath") {
            extendsFrom(releaseRuntimeClasspath)
        }
    }
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
*/