# Features and integrations

## Loom+

`/bannerbuilder` opens the editor. With `loom-link.enabled: true`, sneak + right-clicking a loom opens Loom+; ordinary right-click continues to use the vanilla loom.

## Costs

Pattern creation can require dye, optional banner-pattern items, and Vault money. These are separate controls: setting `pricing.enabled: true` does not itself enable money charging (`pricing.money.enabled` defaults to `false`).

`VANILLA_PLUS` leaves the first six total layers free. `FREE_UNTIL_LIMIT` uses `pricing.free-layers` instead. Reset costs are configured independently.

## Guilds

Guilds has an optional BannerBuilder integration for guild banners. BannerBuilder remains usable without Guilds.

## Geyser / Bedrock

Java patterns may exceed six layers. Bedrock's default visible-layer cap is six, configured separately from the Java editor limit.
