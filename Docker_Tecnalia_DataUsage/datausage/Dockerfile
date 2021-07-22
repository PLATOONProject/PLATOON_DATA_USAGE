FROM openjdk:11-jdk-slim
WORKDIR tmp
COPY *.jar datausage.jar
COPY etc/platoon_datausage_cape.properties /etc/platoon_datausage_cape.properties
ENTRYPOINT ["java","-jar","datausage.jar"]
