plugins {
    `java`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.guava)
    compileOnly(libs.gson)
    compileOnly(libs.slf4j)
    compileOnly(libs.log4j)
    compileOnly(libs.jetbrains.annotations)
}

tasks.withType<Jar>().configureEach {
    from("../LICENSE.txt")
    
    archiveFileName.set("infuse-${project.version}.jar")
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