#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
repo_path="$script_path/.."

cd $repo_path

echo "Building fat jar"

./gradlew unzipProcessingVideoLibrary unzipProcessingUdpLibrary untarProcessingCoreLibrary downloadJoglJar fatJar

echo "Successfully built fat jar"
