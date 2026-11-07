# EssentialsLeve

Plugin Java para servidores **Minecraft Paper**, com um conjunto enxuto de comandos de teleporte e home.

> **Status: protótipo / em validação.** O código-fonte está disponível para análise, mas ainda **não foi compilado contra a API oficial do Paper nem testado em servidor real**. Não trate o projeto como uma versão estável ou pronta para produção.

## Funcionalidades

| Comando | Descrição |
| --- | --- |
| `/rtpgui` | Abre menu GUI com opções de RTP para mundo normal, Nether e End. |
| `/tpa <jogador>` | Solicita teleporte até outro jogador. |
| `/tpahere <jogador>` | Solicita que outro jogador venha até você. |
| `/tpaccept` | Aceita o último pedido de teleporte recebido. |
| `/sethome` | Define a home do jogador. |
| `/home` | Volta para a home salva. |
| `/delhome` | Remove a home. |

### RTP
- Interface de 27 slots com três dimensões.
- Tempo de espera configurável (padrão de 3 segundos).
- Cooldown configurável (padrão de 30 segundos).
- Pesquisa de locais com blocos sólidos e espaço livre.
- O servidor precisa ter os respectivos mundos carregados.

### Persistência e permissões
- Cada jogador possui uma home, armazenada em `config.yml` por UUID.
- Permissões: `essentialsleve.rtp`, `essentialsleve.tpa`, `essentialsleve.home` e `essentialsleve.rtp.bypass`.

## Estrutura

```text
src/main/java/br/com/elomid/essentialsleve/EssentialsLeve.java
src/main/resources/plugin.yml
src/main/resources/config.yml
pom.xml
```

## Compilação (a verificar)

Requisitos previstos: JDK 21 e Apache Maven com acesso ao repositório da API Paper.

```bash
mvn clean package
```

O POM utiliza `paper-api:1.21.1-R0.1-SNAPSHOT` como referência. A compilação e a execução **ainda precisam ser verificadas** em ambiente com as dependências oficiais. Antes de distribuir um JAR, valide os eventos, materiais, teleportes, menus e geração de chunks em servidor de testes.

## Ideias para próximas versões

- Testes de integração em servidor Paper.
- Limite de tentativas e proteção adicional para RTP em todos os biomas e dimensões.
- Internacionalização das mensagens.
- Permissões e mensagens configuráveis.

## Licença

O código é disponibilizado para visualização e avaliação. Nenhuma licença de redistribuição foi definida nesta versão.
