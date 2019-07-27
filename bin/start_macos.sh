#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
repo_path="$script_path/.."

jar_path="$repo_path/build/libs/TesseractFatJar.jar"

echo "Checking for jar at path: ${jar_path}"

[[ ! -f $jar_path ]] && {
  echo "Did not find jar at path ${jar_path}.  Running ./build_macos.sh"
  ${repo_path}/bin/build_macos.sh
}

cd $repo_path

echo "Running ${jar_path}"

java \
  "-Dgstreamer.library.path=lib/video/library/macosx" \
  "-Dgstreamer.plugin.path=lib/video/library/macosx/gstreamer-1.0" \
  -jar ./build/libs/TesseractFatJar.jar
