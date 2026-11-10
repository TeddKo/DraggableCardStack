import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import tddy.ko.cardstack.build_logic.extension.applicationExtension
import tddy.ko.cardstack.build_logic.extension.configureComposeExtension
import tddy.ko.cardstack.build_logic.extension.library
import tddy.ko.cardstack.build_logic.extension.libs
import tddy.ko.cardstack.build_logic.extension.plugin

class ComposeApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(libs.plugin("android-application").pluginId)
                apply(libs.plugin("compose-compiler").pluginId)
            }
            configureComposeExtension(commonExtension = applicationExtension)
            dependencies {
                add("implementation", libs.library("androidx-compose-ui"))
                add("implementation", libs.library("androidx-compose-foundation"))
                add("implementation", libs.library("androidx-compose-material3"))
                add("implementation", libs.library("androidx-compose-activity"))
                add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
                add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
            }
        }
    }
}
