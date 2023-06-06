FROM mcr.microsoft.com/openjdk/jdk:17-mariner as build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY src src

RUN ./gradlew build

# A Spring Boot fat JAR naturally has “layers” because of the way that the JAR itself is packaged.
# If we unpack it first, it is already divided into external and internal dependencies.
RUN mkdir -p build/libs/dependency && (cd build/libs/dependency; jar -xf ../*.jar)


# If the application dependencies do not change, the first layer (from BOOT-INF/lib) need not change,
# so the build is faster, and the startup of the container at runtime if also faster,
# as long as the base layers are already cached.
FROM mcr.microsoft.com/openjdk/jdk:17-distroless
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/libs/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.azdaks.publicapiservice.PublicApiServiceApplication"]