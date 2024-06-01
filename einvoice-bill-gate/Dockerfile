FROM openjdk:8-jdk
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar
COPY ./cacerts $JAVA_HOME/jre/lib/security
USER root
RUN echo "Asia/Ho_Chi_Minh" > /etc/timezone
ENTRYPOINT ["java","-jar","/app.jar"]