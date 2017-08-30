FROM openjdk:8-jdk-alpine

# scala setup
ENV SCALA_VERSION=2.11.11 \
    SCALA_HOME=/usr/share/scala \
    SBT_VERSION=0.13.15 \
    SBT_HOME=/usr/share/sbt


RUN apk update \
 && apk add bash \
 && apk add git \
 && apk add ca-certificates wget \
 && update-ca-certificates

# install scala
RUN cd /tmp \
 && wget "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" \
 && tar xzf scala-${SCALA_VERSION}.tgz \
 && rm /tmp/scala-${SCALA_VERSION}/bin/*.bat \
 && mkdir ${SCALA_HOME} \
 && mv /tmp/scala-${SCALA_VERSION}/bin /tmp/scala-${SCALA_VERSION}/lib ${SCALA_HOME}/ \
 && ln -s ${SCALA_HOME}/bin/* /usr/bin/ \
 && rm -rf /tmp/*

# install sbt
RUN cd /tmp \
 && wget https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz \
 && tar xzf sbt-${SBT_VERSION}.tgz \
 && mkdir ${SBT_HOME} \
 && mv sbt/* ${SBT_HOME}/ \
 && ln -s ${SBT_HOME}/bin/* /usr/bin \
 && rm -rf /tmp/*

# install matterminder
RUN cd /usr/local \
 && git clone https://github.com/neuland/matterminder.git \
 && cd matterminder \
 && sbt
 
EXPOSE 9000

WORKDIR /usr/local/matterminder
ENTRYPOINT ["/bin/bash"]
CMD ["-c", "sbt run"]