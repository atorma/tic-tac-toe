FROM maven:3.9.6-amazoncorretto-8 AS server-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM node:16-alpine AS client-build
WORKDIR /app/client
COPY client .
RUN npm ci --omit=optional
RUN npm run build

FROM amazoncorretto:8-alpine
WORKDIR /app
COPY --from=server-build /app/target/tic-tac-toe.jar ./
COPY --from=client-build /app/public ./public/
CMD ["java", "-jar", "tic-tac-toe.jar"]