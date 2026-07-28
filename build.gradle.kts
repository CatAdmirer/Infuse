subprojects {
    apply(plugin = "java")

    group = "com.catadmirer"
    version = "2.4.5"

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
    }

    tasks.withType<Jar>().configureEach {
        from("../LICENSE.txt")

        archiveFileName.set("InfuseSMP-${project.name}-${project.version}.jar")
    }

    repositories {
        mavenCentral()
    }
}