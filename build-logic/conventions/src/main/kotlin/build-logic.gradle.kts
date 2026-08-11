import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the

plugins {
    `java-library`
}

val libs = the<VersionCatalogsExtension>().named("libs")
val javaVersion = libs.findVersion("java").get().toString().toInt()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly(libs.findLibrary("placeholderapi").get())
    compileOnly(libs.findLibrary("worldguard").get())
    compileOnly(libs.findLibrary("guava").get())
    compileOnly(libs.findLibrary("gson").get())
    if (project.name != "api") {
        implementation(project(":api"))
    } else {
        compileOnly(libs.findLibrary("paper").get())
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

