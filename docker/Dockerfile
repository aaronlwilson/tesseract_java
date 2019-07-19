FROM ubuntu:16.04

# Set Environment Variables & Language Environment
ENV HOME /root
ENV LC_ALL en_US.UTF-8

# Update Base System
RUN apt-get update && apt-get -y upgrade \
      && apt-get install -y language-pack-en \
      && locale-gen en_US.UTF-8 \
      && dpkg-reconfigure locales

# Install Basic Packages
RUN apt-get install -y build-essential software-properties-common wget curl git man unzip nano tmux colord zsh emacs sudo
RUN apt-get install -y xvfb libxrender1

# Install gstreamer native library
RUN apt-get install -y libgstreamer1.0-dev

# RUN apt-get install -y libxext6 libxtst6 libxi6 libxxf86vm1 # these I just copied, maybe not needed

### Java

ENV JAVA_VERSION_MAJOR 8
ENV JAVA_VERSION_BUILD 221
ENV JAVA_VERSION_SHORT ${JAVA_VERSION_MAJOR}u${JAVA_VERSION_BUILD}
ENV JAVA_VERSION 1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_BUILD}

RUN mkdir -p /opt
COPY files/jdk-${JAVA_VERSION_SHORT}-linux-x64.tar.gz /opt/jdk-${JAVA_VERSION_SHORT}-linux-x64.tar.gz

RUN cd /opt && tar xzf jdk-${JAVA_VERSION_SHORT}-linux-x64.tar.gz \
  && update-alternatives --install /usr/bin/java java /opt/jdk${JAVA_VERSION}/bin/java 100 \
  && update-alternatives --install /usr/bin/javac javac /opt/jdk${JAVA_VERSION}/bin/javac 100

ENV JAVA_HOME /opt/jdk${JAVA_VERSION}

RUN mkdir -p /app

WORKDIR /app

# Build FatJar

COPY tmp/tesseract_java /app/tesseract_java

# Patched jogl lib
RUN mkdir -p /app/tesseract_java/lib
COPY files/jogamp-2.3.2-patched.jar /app/tesseract_java/lib/jogamp-2.3.2-patched.jar

WORKDIR /app/tesseract_java

RUN ./gradlew unzipProcessingVideoLibrary unzipProcessingUdpLibrary untarProcessingCoreLibrary fatJar \
  && cp ./build/libs/TesseractFatJar.jar /app/TesseractFatJar.jar

COPY files/start.sh /app

CMD ["/app/start.sh"]
