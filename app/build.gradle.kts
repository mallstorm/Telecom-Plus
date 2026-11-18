plugins {
    id("com.android.application")
}

android {
    namespace = "ua.telecomplus.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "ua.telecomplus.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 60
        versionName = "1.0.0"
    }

    signingConfigs {
        create("fixed") {
            storeFile = file("release.jks")
            storePassword = "tpstore2026"
            keyAlias = "telecomplus"
            keyPassword = "tpstore2026"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("fixed")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("fixed")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }
}
