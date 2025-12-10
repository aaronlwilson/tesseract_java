# Justfile for tesseract_java backend

# Default recipe
default:
    @just --list

# Run locally on macOS with display (requires Java 17 via SDKMAN)
# Uses LibGDX for rendering (no JOGL dependency)
run-local:
    java -XstartOnFirstThread -jar build/libs/TesseractFatJar.jar

# Run locally in headless mode (no display, for Raspberry Pi / servers)
run-headless:
    java -jar build/libs/TesseractFatJar.jar --headless

# Build fat JAR (requires Gradle 7.6.4 and Java 17)
build-local:
    ./gradlew fatJar

# Docker image name
image := "tesseractpixel/tesseract-java"

# Build the docker image
build:
    ./build.sh

# Run the backend (headless mode with virtual framebuffer)
run:
    docker run -ti \
        --rm \
        --name tesseract \
        --platform linux/amd64 \
        -p 8883:8883 \
        -v "{{justfile_directory()}}/../data:/app/tesseract_java/data" \
        {{image}}

# Run with shell access (for debugging)
shell:
    docker run -ti \
        --rm \
        --name tesseract \
        --platform linux/amd64 \
        -p 8883:8883 \
        -v "{{justfile_directory()}}/../data:/app/tesseract_java/data" \
        --entrypoint bash \
        {{image}}

# Run detached (background)
run-detached:
    docker run -d \
        --rm \
        --name tesseract \
        --platform linux/amd64 \
        -p 8883:8883 \
        -v "{{justfile_directory()}}/../data:/app/tesseract_java/data" \
        {{image}}

# Stop the running container
stop:
    docker stop tesseract

# View logs of running container
logs:
    docker logs -f tesseract

# Check if container is running
status:
    @docker ps --filter name=tesseract --format "table {{{{.Names}}}}\t{{{{.Status}}}}\t{{{{.Ports}}}}" || echo "Container not running"

# Test websocket connection
test-websocket:
    ./test_websocket.sh

# Clean up docker resources
clean:
    -docker stop tesseract 2>/dev/null
    -docker rm tesseract 2>/dev/null
    docker image prune -f
