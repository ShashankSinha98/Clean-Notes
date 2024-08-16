import dependencies.AndroidTestDependencies
import dependencies.Dependencies
import dependencies.Application
import dependencies.Versions
import dependencies.Java
import dependencies.SupportDependencies

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.cleannotes"
    compileSdk = 34

    defaultConfig {
        applicationId = Application.id
        minSdk = Versions.minsdk
        targetSdk = Versions.targetsdk
        versionCode = Application.version_code
        versionName = Application.version_name

        testInstrumentationRunner = AndroidTestDependencies.instrumentation_runner
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = Java.java_version
        targetCompatibility = Java.java_version
    }
    kotlinOptions {
        jvmTarget = Java.java_version_str
    }
}

dependencies {

    // kotlin
    implementation(Dependencies.kotlin_standard_library)
    implementation(Dependencies.kotlin_reflect)
    implementation(Dependencies.ktx)

    // support
    implementation(SupportDependencies.appcompat)
    implementation(SupportDependencies.constraintlayout)
    implementation(SupportDependencies.material_design)
    implementation(libs.material)
    implementation(SupportDependencies.swipe_refresh_layout)
}