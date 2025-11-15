FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copia tudo do projeto para dentro do container
COPY . .

RUN chmod +x ./gradlew

# Build sem rodar testes
RUN ./gradlew clean build -x test

# Render define a variável PORT
ENV PORT=3000

EXPOSE 3000

# Inicia o JAR gerado pelo Gradle
CMD ["sh", "-c", "java -jar build/libs/demo-javalin.jar --server.port=$PORT"]
