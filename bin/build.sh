#!/bin/bash
echo "=== Instalando dependências ==="
chmod +x ./gradlew
./gradlew shadowJar

echo "=== Criando diretório de dados ==="
mkdir -p /var/data

echo "=== Build completo ==="