plugins {
    `java`
    `maven-publish`
    alias(libs.plugins.paperweight)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

val javaVersion = (project.property("javaVersion") as String).toInt()
val minecraftVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    implementation(project(":common"))

    compileOnly(libs.placeholderapi)
    compileOnly(libs.worldguard)
    compileOnly(libs.guava)
    compileOnly(libs.gson)

    paperweight.paperDevBundle("${minecraftVersion}+")
}

tasks.runServer {
    minecraftVersion(minecraftVersion)
    jvmArgs("-Dlog4j.configurationFile=log4j2.xml")
}

tasks.processResources {
    val props = mapOf("version" to version,
        "mcVersion" to minecraftVersion)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.withType<Jar>().configureEach {
    from("../LICENSE.txt")

    archiveFileName.set("InfuseSMP-${project.name}-${project.version}.jar")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "infuse"
        }
    }
    repositories {
        maven {
            name = "turbo-maven"
            url = uri("https://maven.turbojax.org/releases/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}