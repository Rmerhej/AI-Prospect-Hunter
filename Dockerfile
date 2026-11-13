# syntax=docker/dockerfile:1

# ============================================================
# Étape 1 : compilation (image Maven + JDK 21)
# On utilise "mvn" de l'image plutôt que ./mvnw : pas de souci
# de fins de ligne Windows, et pas de téléchargement de Maven.
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Le pom.xml d'abord : tant qu'il ne change pas, Docker réutilise cette couche
COPY pom.xml .

# Les sources ensuite, puis la compilation.
# --mount=type=cache garde le dossier .m2 entre deux builds : les dépendances
# ne sont téléchargées qu'une seule fois. Les tests sont lancés à part (./mvnw test).
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests

# ============================================================
# Étape 2 : exécution (JRE seul, image plus légère, sans Maven ni sources)
# ============================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Ne jamais lancer l'application en root dans un conteneur
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring

COPY --from=build /build/target/*.jar app.jar
USER spring

EXPOSE 8080

# La JVM utilise au plus 75 % de la mémoire accordée au conteneur
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
