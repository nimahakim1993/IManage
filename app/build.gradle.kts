plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.nima.app.imanage"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nima.app.imanage"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 37
        versionCode = 5
        versionName = "1.2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("boolean", "HIDE_CARD_SENSITIVE", "false")
    }

    flavorDimensions += "market"

    productFlavors {
        create("cafebazaar") {
            dimension = "market"
            buildConfigField("boolean", "HIDE_CARD_SENSITIVE", "true")
        }
        create("myket") {
            dimension = "market"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

androidComponents {
    onVariants { variant ->
        // Custom APK naming can be added here if needed
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.4.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.4.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.4.0")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation.layout.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.coroutine)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.com.google.gson)
    implementation(libs.com.squareup.okhttp)
    implementation(libs.com.squareup.okhttp3.logging.interceptor)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.vico)

}
