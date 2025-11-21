FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar arquivos do projeto (incluindo o gradle wrapper completo)
COPY . .

# Instalar SQLite
RUN apt-get update && apt-get install -y sqlite3

# Dar permissão ao gradlew
RUN chmod +x ./gradlew

# Fazer build usando o wrapper (não precisa do shadowJar)
RUN ./gradlew build

# Criar diretório para dados do SQLite
RUN mkdir -p /var/data

# Expor a porta
EXPOSE 10000

# Comando de inicialização - use o JAR que foi gerado
CMD ["java", "-jar", "build/libs/demo-javalin.jar"]