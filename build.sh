#!/bin/bash

# Cleaning builds folder
rm -rf builds/*
mkdir builds 2>/dev/null

# Generating build 1
./gradlew clean
./gradlew build

# Saving the build
filename="$(basename "$(find build/libs -maxdepth 1 -name "*.jar" -type f -print -quit)")"
mv "build/libs/$filename" "builds/${filename%.jar}-jdk25.jar"

# Changing versions
sed 's/minecraft = "26.1.2/minecraft = "1.21.11/' gradle/libs.versions.toml | tee gradle/libs.versions.toml
sed 's/javaVersion = 21/javaVersion = 25/' build.gradle.kts | tee build.gradle.kts

# Generating build 2
./gradlew clean
./gradlew build

# Saving the build
ls "build/libs"
mv "build/libs/$filename" "builds/${filename%.jar}-jdk21.jar"

# Undoing version changes
sed 's/minecraft = "1.21.11/minecraft = "26.1.2/' gradle/libs.versions.toml | tee gradle/libs.versions.toml
sed 's/javaVersion = 21/javaVersion = 25/' build.gradle.kts | tee build.gradle.kts
