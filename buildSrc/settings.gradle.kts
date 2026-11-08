// GhostGram infra (PLAN.md 29.4): the Gradle Plugin Portal redirects
// org.jetbrains.kotlin:kotlin-compiler-embeddable to GitHub release assets,
// which returned HTTP 504 repeatedly on 2026-11-08; Maven Central hosts the
// same artifact directly, so resolve plugins with Central first.
pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
