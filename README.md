# BannerBuilder

A Paper banner editor with a Loom+ GUI, additional pattern layers, optional material costs, and configurable pricing. Players can open Loom+ directly or from a loom.

| Feature | Behavior |
|---|---|
| Loom+ | Edit banner base colors and patterns through a GUI |
| Pattern limit | Defaults to 16 layers; can be unlimited |
| Materials | Optional dye consumption and pattern-item requirements |
| Pricing | Free vanilla layers, optional paid layers, rank discounts, and reset costs |
| Loom interaction | Sneak + right-click a loom to open Loom+ by default |
| Bedrock | Separate visible-layer cap for Geyser clients |

## Reference

- [Commands and permissions](docs/COMMANDS.md)
- [Configuration](docs/CONFIG.md)
- [Behavior and integrations](docs/FEATURES.md)
- [Builds](docs/BUILDS.md)

## Requirements

Paper 1.21.4 / Java 21. Vault plus an economy provider are needed only for money-based pricing.

## Build

```powershell
mvn clean package
```

Versioned downloads: [GitHub Releases](https://github.com/DB-ERRSA/BannerBuilder/releases). Development builds: [Actions](https://github.com/DB-ERRSA/BannerBuilder/actions), when a build workflow is configured.
