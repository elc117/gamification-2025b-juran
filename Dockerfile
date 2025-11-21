FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar arquivos do projeto
COPY . .

# Instalar SQLite
RUN apt-get update && apt-get install -y sqlite3

# Fazer build da aplicação usando tarefa padrão
RUN gradle build

# Criar diretório para dados do SQLite
RUN mkdir -p /var/data

# Expor a porta
EXPOSE 10000

# Comando de inicialização - ajuste o nome do JAR se necessário
CMD ["java", "-jar", "build/libs/demo-javalin.jar"]