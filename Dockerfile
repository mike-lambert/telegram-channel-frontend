FROM ubuntu:bionic
RUN mkdir /app
RUN apt-get update && apt-get -y install openjdk-8-jre
COPY build/libs/telegram-channel-frontend-1.0.1.jar /app/app.jar
ENTRYPOINT java -jar /app/app.jar
