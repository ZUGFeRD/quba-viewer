rootProject.name = "Quba"

//enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            @Suppress("UnstableApiUsage")
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()

        // Required by compose-webview-multiplatform
        // https://github.com/KevinnZou/compose-webview-multiplatform#multiplatform
        //maven("https://jogamp.org/deployment/maven")

        // Required by jogl & gluegen
        // https://jogamp.org/deployment/maven/org/jogamp/gluegen/gluegen-rt/2.5.0/gluegen-rt-2.5.0.jar
        maven("https://jogamp.org/deployment/maven")
    }
}

include(":manager")
project(":manager").name = "Quba"
