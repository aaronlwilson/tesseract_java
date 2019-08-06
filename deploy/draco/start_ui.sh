#!/bin/bash -el

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Note: this must be run as root

docker run -p 80:80 -e "defaultServerAddr=dracobot.local" tesseractpixel/tesseract-ui:latest
