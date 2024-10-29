plugins {
    alias(libs.plugins.tddy.ko.android.application)
    alias(libs.plugins.tddy.ko.compose.application)
}

android {
    namespace = "tddy.ko.cardstack"

    defaultConfig {
        applicationId = "tddy.ko.cardstack"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(projects.library.ui)
    implementation(projects.demo.designSystem)
    implementation(projects.demo.feature)
}