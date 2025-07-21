# Stage 1: Cache Gradle dependencies
FROM gradle:latest AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY composeApp/build.gradle.kts /home/gradle/app/composeApp
COPY build.gradle.* gradle.properties /home/gradle/app/
COPY gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle clean build -i --stacktrace

# Stage 2: Build Application
FROM gradle:latest AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the App
RUN gradle wasmJsBrowserDistribution

# Stage 3: Create the Runtime Image
FROM nginx:alpine AS runtime

RUN rm -rf /usr/share/nginx/html/*

# copy built static files from builder
COPY --from=build /home/gradle/src/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]