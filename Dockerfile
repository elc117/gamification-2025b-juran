FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar arquivos do projeto
COPY . .

# Instalar SQLite e Gradle
RUN apt-get update && apt-get install -y sqlite3 gradle

# Fazer build da aplicação usando Gradle do sistema
RUN gradle shadowJar

# Criar diretório para dados do SQLite
RUN mkdir -p /var/data

# Expor a porta
EXPOSE 10000

# Comando de inicialização
CMD ["java", "-jar", "build/libs/gamification.jar"]