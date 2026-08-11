plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.paperweight)
    alias(libs.plugins.run.paper)
}

group = "com.catadmirer"

val javaVersion = (project.property("javaVersion") as String).toInt()
val minecraftVersion: String by project

dependencies {
    paperweight.paperDevBundle("${minecraftVersion}+")
}

tasks.runServer {
    // Configure the Minecraft version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
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

tasks.register("resetAndRun") {
    description = "Resets the server files and reruns it"
    delete("run/plugins/$rootProject.name")
    finalizedBy("runServer")
}
