#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
repo_path="${script_path}/.."

tmp_repo_dir="$script_path/tmp/tesseract_java"

cleanup() {
  rm -rf $tmp_dir
  rm -rf $script_path/files/jdk-8u221-linux-x64.tar.gz

  local exit_code=$1
  local previous_command=$BASH_COMMAND
  [[ $exit_code -ne 0 ]] && [[ ! $previous_command =~ exit* ]] && echo "ERROR: Script exited with code $exit_code from command $previous_command"
  exit $exit_code
}

trap 'cleanup $?' EXIT

mkdir -p $tmp_repo_dir

cd $repo_path

# Copy oracle jdk from 'deps' folder to 'files' folder
cp -f $repo_path/deps/jdk-8u221-linux-x64.tar.gz $script_path/files/jdk-8u221-linux-x64.tar.gz

# These are the important files/directories in the repo required for building the jar
cp -R \
  gradlew \
  build.gradle \
  gradle \
  lib \
  src \
  $tmp_repo_dir

docker build -t tesseractpixel/tesseract-java -f "${script_path}/Dockerfile" "${script_path}"

rm -rf $tmp_repo_dir
