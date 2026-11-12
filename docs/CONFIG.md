# Configuration

File: `plugins/BannerBuilder/config.yml`.

| Key | Default | Behavior |
|---|---|---|
| `banner.infinite-patterns` | `false` | Removes the plugin-defined layer limit |
| `banner.max-patterns` | `16` | Maximum layers when unlimited patterns are disabled |
| `pricing.enabled` | `true` | Enables configured layer pricing |
| `pricing.mode` | `VANILLA_PLUS` | Six total layers are free; only new layers above six are paid |
| `pricing.free-layers` | `3` | Free layers in `FREE_UNTIL_LIMIT` mode |
| `pricing.money.enabled` | `false` | Charges money for paid layers through Vault |
| `pricing.money.cost-per-paid-layer` | `50.0` | Price of each paid layer |
| `materials.enabled` | `true` | Enables material requirements |
| `materials.consume-dyes` | `true` | Consumes dye when adding a layer |
| `materials.consume-pattern-items` | `false` | Whether special pattern items are consumed |
| `reset.enabled` | `true` | Enables banner reset |
| `reset.money.enabled` | `false` | Enables money-based reset cost |
| `reset.money.cost` | `25.0` | Reset charge when enabled |
| `reset.materials.require-base-color-dye` | `true` | Requires a dye for resetting base color |
| `reset.materials.consume-base-color-dye` | `true` | Consumes the required dye |
| `loom-link.enabled` | `true` | Enables loom interaction |
| `loom-link.require-sneak` | `true` | Sneak + right-click opens Loom+; ordinary right-click stays vanilla |
| `loom-link.notify-on-place` | `true` | Shows Loom+ notice on loom placement |
| `bedrock.max-visible-layers` | `6` | Separate visible-layer cap for Bedrock clients |
| `messages.prefix` | Loom+ prefix | Chat message prefix |

Rank discount entries live under `discounts.ranks`. For the exact current YAML, see [bundled defaults](../src/main/resources/config.yml).
