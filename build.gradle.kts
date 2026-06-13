plugins {
    `java-library`
    alias(libs.plugins.paperweight)
    alias(libs.plugins.run.paper)
}

group = "com.catadmirer"
version = "2.4.4"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.placeholderapi)
    paperweight.paperDevBundle("${libs.versions.minecraft.get()}+")
    compileOnly(libs.h2)
}

tasks.runServer {
    // Configure the Minecraft version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    minecraftVersion(libs.versions.minecraft.get())
    jvmArgs("-Dlog4j.configurationFile=log4j2.xml")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    sourceCompatibility = JavaVersion.toVersion(25)
    targetCompatibility = JavaVersion.toVersion(21)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    val props = mapOf("version" to version,
                        "mcVersion" to libs.versions.minecraft.get(),
                        "h2Version" to libs.versions.h2.get())
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.register("resetAndRun") {
    delete("run/plugins/$rootProject.name")
    finalizedBy("runServer")
    description = "Resets the plugin's data directory and then runs a server"
}
