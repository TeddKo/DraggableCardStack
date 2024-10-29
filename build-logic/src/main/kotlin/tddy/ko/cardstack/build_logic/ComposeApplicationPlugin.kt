import org.gradle.api.Plugin
import org.gradle.api.Project
import tddy.ko.cardstack.build_logic.extension.applicationExtension
import tddy.ko.cardstack.build_logic.extension.configureComposeExtension
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
        }
    }
}