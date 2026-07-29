plugins {
    `maven-publish`
}

dependencies {
    compileOnly(libs.guava)
    compileOnly(libs.gson)
    compileOnly(libs.slf4j)
    compileOnly(libs.log4j)
}

tasks.withType<Jar>().configureEach {
    archiveFileName.set("infuse-${project.version}.jar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
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