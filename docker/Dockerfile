FROM ubuntu:22.04

# Set Environment Variables & Language Environment
ENV HOME /root
ENV LC_ALL en_US.UTF-8
ENV DEBIAN_FRONTEND=noninteractive

# Update Base System
RUN apt-get update && apt-get -y upgrade \
      && apt-get install -y language-pack-en \
      && locale-gen en_US.UTF-8 \
      && dpkg-reconfigure --frontend=noninteractive locales

# Install Basic Packages
RUN apt-get install -y build-essential software-properties-common wget curl git man unzip nano tmux colord zsh emacs sudo
RUN apt-get install -y xvfb libxrender1

# Install gstreamer native library
RUN apt-get install -y libgstreamer1.0-dev

### Java 21

# Install OpenJDK 21 (available in Ubuntu 22.04 repositories)
RUN apt-get install -y openjdk-21-jdk

ENV JAVA_HOME /usr/lib/jvm/java-21-openjdk-amd64

RUN mkdir -p /app

WORKDIR /app

# Build FatJar

COPY tmp/tesseract_java /app/tesseract_java

WORKDIR /app/tesseract_java

# Build with only the UDP library task (Processing libraries removed)
RUN ./gradlew unzipProcessingUdpLibrary fatJar \
  && cp ./build/libs/TesseractFatJar.jar /app/TesseractFatJar.jar

COPY files/start.sh /app

CMD ["/app/start.sh"]
