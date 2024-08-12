#!/bin/bash

# build JAR file
echo "Building JAR file..."
./gradlew clean build
echo "JAR file built successfully!"

# Run Docker Compose
echo "Running Docker Compose..."
docker-compose up --build
