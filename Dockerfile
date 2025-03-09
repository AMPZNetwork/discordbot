FROM openjdk:21
WORKDIR /app
COPY "build/libs/discordbot-0.0.1-SNAPSHOT.war" .
ENTRYPOINT [ "java", "-jar", "discordbot-0.0.1-SNAPSHOT.war" ]
