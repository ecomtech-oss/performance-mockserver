FROM gradle:7.4.0 as build

COPY . /opt/project/src
WORKDIR /opt/project/src

RUN ./gradlew clean build --no-daemon && \
 rm -rf /home/builder/.kotlin/ && \
 rm -rf /tmp/kotlin*

FROM openjdk:11.0
EXPOSE 1080
EXPOSE 587

COPY --from=build /opt/project/src/build/libs/*.jar /bin/runner/run.jar
WORKDIR /bin/runner
