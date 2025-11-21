FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar arquivos do projeto (incluindo gradle wrapper)
COPY . .

# Instalar SQLite
RUN apt-get update && apt-get install -y sqlite3

# Dar permissão e verificar gradlew
RUN chmod +x ./gradlew
RUN ls -la gradlew
RUN ls -la gradle/wrapper/

# Fazer build da aplicação
RUN ./gradlew shadowJar

# Criar diretório para dados
RUN mkdir -p /var/data

EXPOSE 10000

CMD ["java", "-jar", "build/libs/gamification.jar"]