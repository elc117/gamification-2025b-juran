# Projeto de Gamificação -- Basquete

**Autor:** Juran Quesada Tavares\
**Curso:** Sistema de Informação

------------------------------------------------------------------------

## Proposta do Projeto

Este projeto tem como objetivo incentivar a prática de atividades
físicas relacionadas ao **basquete** utilizando conceitos de
**gamificação**.\
O sistema permite que o usuário registre seus treinos --- como
**arremessos, corridas e outras atividades** --- e receba **pontos,
níveis, conquistas e recompensas** conforme sua evolução.

### Funcionalidades planejadas:

-   Registro de atividades (ex.: acertos de arremesso, quilômetros
    corridos)\
-   Sistema de pontuação e progressão de níveis\
-   Desafios diários\
-   Exibição da evolução do usuário\
-   Inserção aleatória de alvos na quadra\
-   Persistência dos dados com SQLite

------------------------------------------------------------------------

## 🛠️ Processo de Desenvolvimento

comentários sobre etapas do desenvolvimento, incluindo detalhes técnicos sobre os recursos de orientação a objetos utilizados, sobre erros/dificuldades/soluções e sobre as contribuições de cada integrante (não usar IA para gerar esses comentários!)

Fui me baseando no exemplo SQLite fornecido, peguei a idea do codigo do trabalho que apresentei sobre o drop, para inserir um ponto aleatório na imagem

Problemas ao carregar as imagens, o caminho relativo não encontrava quando executava via Gradle, problemas com a versão do java e com a forma de codificar.

------------------------------------------------------------------------

## 📐 Diagrama de Classes

<img width="1873" height="1719" alt="Diagrama gamification" src="https://github.com/user-attachments/assets/a9f379e9-5cf6-4ace-84b8-c1bfa9365a55" />


------------------------------------------------------------------------

## Orientações para Execução

### Requisitos

-   Java 17+\
-   Gradle\
-   SQLite JDBC\
-   IDE (IntelliJ ou VS Code)

### Passo a Passo

``` bash
git clone https://github.com/.../seu-projeto.git
cd seu-projeto
./gradlew build
./gradlew run
```

### Dependências

-   org.xerial:sqlite-jdbc\
-   java.desktop\
-   Gradle application plugin

------------------------------------------------------------------------

## Resultado Final

Adicionar GIF ou vídeo aqui:

``` markdown
![Demonstração do Sistema](CAMINHO_DO_GIF.gif)
```

------------------------------------------------------------------------

## 📚 Referências e Créditos

-   Exemplo SQLite fornecido pelo professor\
-   Código do trabalho anterior (drop)\
-   Documentação oficial SQLite JDBC, Java AWT/Swing, Gradle\
-   Prompts utilizados (quando aplicável)

