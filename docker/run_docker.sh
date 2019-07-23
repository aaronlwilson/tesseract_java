#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

data_dir="${script_path}/../data"

docker run -ti \
    --rm \
    --name p5 \
    -p 8883:8883 \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -v "${data_dir}:/app/tesseract_java/data" \
    --entrypoint bash \
    --privileged \
  tesseractpixel/tesseract-java
