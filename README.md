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

-   Registro de atividades
-   Sistema de pontuação e progressão de níveis
-   Desafios diários
-   Exibição da evolução do usuário
-   Inserção aleatória de alvos na quadra

------------------------------------------------------------------------

## Processo de Desenvolvimento

Iniciei o projeto me baseando no exemplo SQLite fornecido, no início tentei separar em pastas DAO, Model, View e Controller, mas acabei desistindo no meio do caminho e voltei para o jeito que estava conforme o exemplo SQLite. Fiz as classes Gamification, Treino, UsuarioStatus e Conquista e fui implementando. No inicio fiz apenas a parte de CRUD de treino com as conquistas para suprir a ideia base do projeto de gamificar. Posteriormente fui agregando novas funcionalidades conforme achei interessante de se ter, tais como o desafio diario, as quadras com geração de pontos aleatórios, peguei a idea do codigo do trabalho que apresentei sobre o drop, para inserir um ponto aleatório na imagem, e por último fiz as estatísticas dos treinos.

#### Detalhes técnicos

Encapsulamento: 
    As classes Treino, UsuarioStatus e Conquista usam campos privados com getters e setters públicos. Por exemplo, em Treino, os métodos getTipo() e setTipo(String tipo) encapsulam o atributo tipo.

Abstração:
    A classe Treino abstrai um treino genérico, com atributos comuns (id, tipo, quantidade, data), permitindo representar diferentes tipos sem duplicação de código. Métodos como isValidTreino abstraem validações, verificando se o tipo é válido via enum e se a quantidade é positiva.

Herança e Polimorfismo:
    Embora não haja herança explícita (todas as classes são independentes), o polimorfismo é usado no método calcularPontos, que utiliza um switch baseado no enum TipoTreino para comportamentos diferenciados.

Enums:
    TipoTreino (ARREMESSO, CORRIDA, SALTOS, ABDOMINAIS) define constantes para tipos de treino, garantindo type safety e evitando strings mágicas. É usado em validações (TipoTreino.valueOf(treino.getTipo().toUpperCase())) e cálculos, promovendo consistência e reduzindo erros de digitação.

Composição e Agregação:
    A classe UsuarioStatus compõe uma lista de Conquista (via List<Conquista>), representando uma relação "tem-muitos". Isso modela o status do usuário como um agregado de conquistas.


Tive problemas ao carregar as imagens, o caminho relativo não encontrava quando executava via Gradle, problemas com a versão do java e com a forma de codificar que as vezes não era compatível com a versão. Fora isso o mais complexo foi mudar o projeto pra colocar no render e no itch.io, mas fui fazendo as mudanças com o chatgpt até ter sucesso. No fim acredito que tenha partes que não sejam necessárias ter no código, devido a essa busca com o chatgpt, e muitos pontos a serem melhorados, tais como design do frontend, implementação de uma classe usuario para poder fazer login, transformar os km em float e mudar a forma de calcular os pontos, fazer cards para as conquistas e um botão para mostrar elas ao invés de deixar sempre aparecendo.

------------------------------------------------------------------------

## Diagrama de Classes

<img width="1873" height="1719" alt="Diagrama gamification" src="https://github.com/user-attachments/assets/a9f379e9-5cf6-4ace-84b8-c1bfa9365a55" />


------------------------------------------------------------------------

## Orientações para Execução

### Requisitos

-   Java 17+
-   Gradle

### Passo a Passo

``` bash
git clone https://github.com/.../seu-projeto.git](https://github.com/elc117/gamification-2025b-juran.git
cd gamification-2025b-juran
./gradlew build
./gradlew run
```
ou

```
git clone https://github.com/.../seu-projeto.git](https://github.com/elc117/gamification-2025b-juran.git
cd gamification-2025b-juran
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'"
.\gradlew.bat build
.\gradlew.bat run
```

### Dependências

-   org.xerial:sqlite-jdbc
-   java.desktop
-   Gradle application plugin

------------------------------------------------------------------------

## Resultado Final

Adicionar GIF ou vídeo aqui:

``` markdown
![Video Project](https://github.com/user-attachments/assets/d8f7e40e-0332-499f-b2b1-6994da44a6f8)

```

------------------------------------------------------------------------

## Referências e Créditos

-   Exemplo SQLite fornecido pela professora
-   Slides das aulas
-   Código do trabalho anterior (drop)
-   Documentação oficial SQLite JDBC, Java AWT/Swing, Gradle
-   https://chatgpt.com
-   https://www.blackbox.ai
-   https://javalin.io/tutorials/docker








