# Runner image
FROM ubuntu:18.04 AS runner

# Update packages
RUN apt-get update

# Install java 8
RUN apt-get install -y software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections && \
    apt-get install -y oracle-java8-installer

# Removing cache files
RUN apt-get autoclean -y && rm -rf /var/cache/*


# Builder image
FROM runner AS builder

# Update packages
RUN apt-get update

# Install scala 2.12.7
RUN apt-get install -y wget && \
    wget -q "http://www.scala-lang.org/files/archive/scala-2.12.8.deb" -O /tmp/scala.deb && dpkg -i /tmp/scala.deb && \
    scala -version && \
    rm /tmp/scala.deb

# Install sbt 1.2.8
RUN apt-get install -y wget && \
    wget -q "http://dl.bintray.com/sbt/debian/sbt-1.2.8.deb" -O /tmp/sbt.deb && dpkg -i /tmp/sbt.deb && \
    sbt sbtVersion && \
    rm /tmp/sbt.deb

# Copying project
COPY project /tmp/project
COPY src /tmp/src
COPY build.sbt /tmp/build.sbt

# Building project
RUN cd /tmp && sbt test pack


# Image with microservice
FROM runner

# Creation application directory
RUN mkdir -p /opt/app

# Copying compiled project
COPY --from=builder --chown=root:root /tmp/target/pack /opt/app

# Entrypoint
ENTRYPOINT /opt/app/bin/http-kafka-endpoint