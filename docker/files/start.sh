#!/bin/bash -el

echo "Creating fake framebuffer"

sudo Xvfb :1 -screen 0 1024x768x24 &</dev/null &
export DISPLAY=":1"

echo "Running Tesseract jar"

java -jar /app/TesseractFatJar.jar
