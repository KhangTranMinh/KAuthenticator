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

rootProject.name = "KAuthenticator"
include(":app")
include(":core:otp")
include(":core:model")
include(":core:security")
include(":core:database")
include(":feature:addaccount")

include(":feature:accounts")
