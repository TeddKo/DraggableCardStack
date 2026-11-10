package tddy.ko.cardstack.build_logic.extension

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureComposeExtension(commonExtension: CommonExtension) {
    commonExtension.buildFeatures.compose = true
    extensions.getByType<ComposeCompilerGradlePluginExtension>().includeSourceInformation.set(true)
    dependencies {
        add("implementation", libs.library("androidx-compose-ui"))
        add("implementation", libs.library("androidx-compose-foundation"))
    }
}
