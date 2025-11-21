FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar arquivos do projeto
COPY . .

# Instalar SQLite (necessário para o banco)
RUN apt-get update && apt-get install -y sqlite3

# Dar permissão ao gradlew
RUN chmod +x ./gradlew

# Fazer build da aplicação
RUN ./gradlew shadowJar

# Criar diretório para dados do SQLite
RUN mkdir -p /var/data

# Expor a porta que o Render usa
EXPOSE 10000

# Comando de inicialização
CMD ["java", "-jar", "build/libs/gamification.jar"]