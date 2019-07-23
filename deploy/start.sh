#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set +e
sudo Xvfb :1 -screen 0 1024x768x24 </dev/null  &
export DISPLAY=":1"
set -e
:
cd /home/nukabot/app

java -jar TesseractFatJar.jar
