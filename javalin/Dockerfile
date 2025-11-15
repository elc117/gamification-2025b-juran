# Use uma imagem oficial do OpenJDK / Temurin
FROM eclipse-temurin:17-jdk-alpine

# Defina diretório de trabalho dentro do container
WORKDIR /app

# Copie todos os arquivos do projeto para o container
COPY . .

# Assegure que o wrapper do Gradle existe (se você usar gradlew)
RUN chmod +x ./gradlew

# Execute o build do projeto (sem testes para acelerar)
RUN ./gradlew clean build -x test

# Depois do build, você pode copiar apenas o artefato jar (opcional: para reduzir tamanho)
# Exemplo supondo que build/libs/gamification*.jar é o jar gerado
RUN mkdir -p /app/dist
RUN cp build/libs/*.jar /app/dist/app.jar

# Expor a porta em que sua aplicação vai rodar
EXPOSE 3000

# Variável de ambiente para porta (caso plataforma hi-ja suporte)
ENV PORT=3000

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/dist/app.jar"]
