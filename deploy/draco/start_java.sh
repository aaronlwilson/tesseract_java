#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set +e
sudo Xvfb :1 -screen 0 1024x768x24 </dev/null  &
export DISPLAY=":1"
set -e
:
cd $HOME/app/tesseract_java

export XDG_RUNTIME_DIR="/run/user/1000"

export TESSERACT_STAGE_TYPE="draco"
export TESSERACT_INITIAL_PLAY_STATE="playing"
export TESSERACT_INITIAL_PLAYLIST="All Videos"

java -jar ./build/libs/TesseractFatJar.jar
