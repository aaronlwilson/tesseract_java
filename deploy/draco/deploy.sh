#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $script_path/..

./gradlew fatJar

scp ./build/libs/TesseractFatJar.jar nukabot@nukamini.local:/home/nukabot/app
scp ./deploy/start.sh nukabot@nukamini.local:/home/nukabot/app
