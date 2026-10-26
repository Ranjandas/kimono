pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kimono"

include(":app")
include(":core:design")
include(":core:data")
include(":feature:home")
include(":feature:drawer")
include(":feature:settings")
include(":data:apps")
include(":data:media")
