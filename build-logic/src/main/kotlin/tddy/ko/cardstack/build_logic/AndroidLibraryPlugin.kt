import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import tddy.ko.cardstack.build_logic.extension.configureAndroidExtension
import tddy.ko.cardstack.build_logic.extension.library
import tddy.ko.cardstack.build_logic.extension.libraryExtension
import tddy.ko.cardstack.build_logic.extension.libs
import tddy.ko.cardstack.build_logic.extension.plugin

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(libs.plugin("android-library").pluginId)
                apply(libs.plugin("kotlin-android").pluginId)
            }

            dependencies {
                add("implementation", libs.library("androidx-core-ktx"))
                add("implementation", libs.library("androidx-appcompat"))
            }

            @Suppress("UnstableApiUsage")
            libraryExtension.apply {
                configureAndroidExtension(this)
                testOptions.targetSdk = 35
            }
        }
    }
}