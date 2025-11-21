FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar arquivos do projeto
COPY . .

# Instalar SQLite e Gradle manualmente
RUN apt-get update && apt-get install -y sqlite3 wget unzip

# Instalar Gradle manualmente
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle.zip && \
    unzip -d /opt /tmp/gradle.zip && \
    ln -s /opt/gradle-8.5/bin/gradle /usr/bin/gradle && \
    rm /tmp/gradle.zip

# Fazer build usando Gradle instalado
RUN gradle build

# Criar diretório para dados do SQLite
RUN mkdir -p /var/data

# Expor a porta
EXPOSE 10000

# Comando de inicialização
CMD ["java", "-jar", "build/libs/demo-javalin.jar"]