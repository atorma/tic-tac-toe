FROM eclipse-temurin:21-alpine
WORKDIR /app
COPY ./target/tic-tac-toe.jar .
COPY ./public ./public/
CMD ["java", "-jar", "tic-tac-toe.jar"]
EXPOSE 8080/tcp