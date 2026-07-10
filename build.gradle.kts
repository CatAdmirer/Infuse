import java.util.Objects

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.paperweight)
    alias(libs.plugins.run.paper)
}

group = "com.catadmirer"
version = "2.4.5-beta7"

var javaVersion = Integer.parseInt(Objects.requireNonNullElse(System.getenv("INFUSE_JVM"), "25"))
var minecraftVersion = Objects.requireNonNullElse(System.getenv("INFUSE_MINECRAFT"), "26.1.2")

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.placeholderapi)
    paperweight.paperDevBundle("${minecraftVersion}+")
}

tasks.runServer {
    // Configure the Minecraft version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    minecraftVersion(minecraftVersion)
    jvmArgs("-Dlog4j.configurationFile=log4j2.xml")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.processResources {
    val props = mapOf("version" to version,
                        "mcVersion" to minecraftVersion)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.register("resetAndRun") {
    delete("run/plugins/$rootProject.name")
    finalizedBy("runServer")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            version = "$version-$javaVersion"
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