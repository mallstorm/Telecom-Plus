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
        versionCode = 32
        versionName = "0.2.25-restored"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
