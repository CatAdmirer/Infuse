#!/bin/bash

# Cleaning builds folder
rm -rf builds/*
mkdir builds 2>/dev/null

# Setting environment variables
export INFUSE_MINECRAFT="26.1.2"
export INFUSE_JVM=25

# Generating build 1
./gradlew clean
./gradlew build

# Saving the build
filename="$(basename "$(find build/libs -maxdepth 1 -name "*.jar" -type f -print -quit)")"
mv "build/libs/$filename" "builds/${filename%.jar}-26.1.2.jar"

# Adjusting environment variables
export INFUSE_MINECRAFT="1.21.11"
export INFUSE_JVM=21

# Generating build 2
./gradlew clean
./gradlew build

# Saving the build
mv "build/libs/$filename" "builds/${filename%.jar}-1.21.11.jar"