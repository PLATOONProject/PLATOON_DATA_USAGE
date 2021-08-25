FROM openjdk:11-jdk-slim
WORKDIR tmp
COPY *.jar datausage.jar
COPY etc/platoon_datausage_cape.properties /etc/platoon_datausage_cape.properties
COPY etc/keycloak/keystore.jks /etc/keycloak/keystore.jks
ENTRYPOINT ["java","-jar","datausage.jar"]
