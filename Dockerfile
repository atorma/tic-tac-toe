FROM eclipse-temurin:21-alpine
WORKDIR /app/server
COPY ./server/target/tic-tac-toe-server.jar .
COPY ./server/public ./public
CMD ["java", "-jar", "tic-tac-toe-server.jar"]
EXPOSE 8080/tcp