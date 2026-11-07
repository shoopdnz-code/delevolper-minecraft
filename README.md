# EssentialsLeve

**Minecraft Paper plugin | Java 21 | Maven**

EssentialsLeve is a Java plugin prototype for Minecraft Paper servers. It brings together basic teleport requests, a random teleport GUI, and player home management in a single project.

> **Development status:** Prototype. The source code has not yet been verified against the official Paper API in a real server environment. This repository is provided for code review and development tracking, not as a production-ready release.

## Features

- Random teleport GUI with Overworld, Nether, and End options
- Teleport requests between players
- One persistent home per player
- Configurable RTP countdown and cooldown
- Permission nodes for the main command groups

## Commands

| Command | Purpose |
| --- | --- |
| `/rtpgui` | Open the random teleport menu |
| `/tpa <player>` | Request to teleport to a player |
| `/tpahere <player>` | Request that a player teleport to you |
| `/tpaccept` | Accept the latest teleport request |
| `/sethome` | Save your current location |
| `/home` | Teleport to your saved home |
| `/delhome` | Delete your saved home |

## Technologies

- Java 21
- Paper API (POM currently targets `1.21.1-R0.1-SNAPSHOT`)
- Apache Maven
- YAML configuration files

## Build (not yet validated)

Requires JDK 21 and Maven with access to the official PaperMC Maven repository.

```bash
mvn clean package
```

The resulting JAR, if compilation succeeds, will be created under `target/`. Testing on a disposable Paper server is required before distribution.

## Project structure

```text
pom.xml
src/
  main/
    java/br/com/elomid/essentialsleve/EssentialsLeve.java
    resources/
      plugin.yml
      config.yml
```

## Permissions

- `essentialsleve.rtp`
- `essentialsleve.rtp.bypass`
- `essentialsleve.tpa`
- `essentialsleve.home`

## Roadmap

- [ ] Compile against the official Paper API
- [ ] Run integration tests in a local Paper server
- [ ] Review safe RTP placement across all dimensions
- [ ] Improve command feedback and configuration
- [ ] Publish a tested release

## Author

Developed by **DNZ** as a Minecraft plugin development project.

## License

No open-source license has been selected for this repository. The code is currently available for review only; please contact the author before reuse or redistribution.
