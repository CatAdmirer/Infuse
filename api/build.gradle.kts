plugins {
    id("build-logic")
    `maven-publish`
}

group = "com.catadmirer"

val javaVersion = (project.property("javaVersion") as String).toInt()
val minecraftVersion: String by project

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