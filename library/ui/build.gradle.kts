import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.tddy.ko.android.library)
    alias(libs.plugins.tddy.ko.compose.library)
    alias(libs.plugins.vanniktech.maven)
}

android {
    namespace = "tddy.ko.cardstack.ui"
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)

    signAllPublications()

    coordinates("io.github.teddko", "cardstack", "1.0.0")

    pom {
        name.set("CardStack")
        description.set("Jetpack Compose CardStack Library")
        url.set("https://github.com/TeddKo/CardStack")
        inceptionYear.set("2024")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("TeddKo")
                name.set("Tedd Ko")
                email.set("tddy.ko@kakao.com")
                url.set("https://github.com/TeddKo")
            }
        }

        scm {
            url.set("https://github.com/TeddKo/CardStack")
            connection.set("scm:git:git://github.com/TeddKo/CardStack.git")
            developerConnection.set("scm:git:git://github.com/TeddKo/CardStack.git")
        }
    }
}