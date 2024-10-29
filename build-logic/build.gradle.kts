plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("AndroidApplication") {
            id = "tddy.ko.cardstack.application"
            implementationClass = "AndroidApplicationPlugin"
        }

        register("AndroidLibrary") {
            id = "tddy.ko.cardstack.library"
            implementationClass = "AndroidLibraryPlugin"
        }

        register("ComposeApplication") {
            id = "tddy.ko.cardstack.compose.application"
            implementationClass = "ComposeApplicationPlugin"
        }

        register("ComposeLibrary") {
            id = "tddy.ko.cardstack.compose.library"
            implementationClass = "ComposeLibraryPlugin"
        }
    }
}