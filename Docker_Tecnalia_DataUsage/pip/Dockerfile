FROM openjdk:11-jdk-slim
WORKDIR tmp
COPY *.jar pip.jar
COPY etc/pip.properties /etc/pip.properties
ENTRYPOINT ["java","-jar","pip.jar"]




