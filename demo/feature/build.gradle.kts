plugins {
    alias(libs.plugins.tddy.ko.android.library)
    alias(libs.plugins.tddy.ko.compose.library)
}

android {
    namespace = "tddy.ko.cardstack.feature"
}

dependencies {
    implementation(projects.demo.designSystem)
    implementation(projects.library.ui)
    implementation(libs.coil)
}