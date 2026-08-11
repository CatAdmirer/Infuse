plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:${libs.versions.shadow.get()}")
}
