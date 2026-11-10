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
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil)
}
