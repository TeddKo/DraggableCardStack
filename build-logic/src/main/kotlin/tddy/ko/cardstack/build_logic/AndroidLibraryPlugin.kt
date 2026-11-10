import org.gradle.api.Plugin
import org.gradle.api.Project
import tddy.ko.cardstack.build_logic.extension.configureAndroidExtension
import tddy.ko.cardstack.build_logic.extension.libraryExtension
import tddy.ko.cardstack.build_logic.extension.libs
import tddy.ko.cardstack.build_logic.extension.plugin

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.plugin("android-library").pluginId)

            libraryExtension.apply {
                configureAndroidExtension(this)
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
