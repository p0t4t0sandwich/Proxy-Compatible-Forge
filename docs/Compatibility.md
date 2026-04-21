# Compatibility

## Incompatible Mods

| Mod Name                     | Issue                                                                                             | Solution                                                                                          |
|------------------------------|---------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| Better Compatibility Checker | Alters the serverlist ping packet in a way that causes Velocity to be unable to read the response | Remove the mod from the backend server, or use an alternate method of querying the backend server |

## Modern Forwarding and CrossStitch Command Wrapping

| MC Version | Forge | NeoForge | Modern Forwarding | CrossStitch | Notes                                                                                                                                 |
|------------|-------|----------|-------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------|
| 26.1.x     | ✅     | ✅        | ✅                 | ✅           |                                                                                                                                       |
| 1.21.x     | ✅     | ✅        | ✅                 | ✅           |                                                                                                                                       |
| 1.20.x     | ✅     | ✅        | ✅                 | ✅           | NeoForge 1.20.2 is incompatible with Ambassador, and client cannot read the LoginQueryPacket when connecting directly (non-issue)     |
| 1.19.x     | ✅     | N/A      | ✅                 | ✅           |                                                                                                                                       |
| 1.18.x     | ✅     | N/A      | ✅                 | ✅           |                                                                                                                                       |
| 1.17.x     | ✅     | N/A      | ✅                 | ✅           |                                                                                                                                       |
| 1.16.x     | ✅     | N/A      | ✅                 | ✅           | Requires [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap) on 1.16 - 1.16.1                                                   |
| 1.15.x     | ✅     | N/A      | ✅                 | ✅           | Requires [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap) on 1.15 - 1.15.1                                                   |
| 1.14.x     | ✅     | N/A      | ✅                 | ✅           | Requires [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap)                                                                    |
| 1.13.x     | ✅     | N/A      | ✅                 | ✅           | Requires [ModernMixins](https://modrinth.com/mod/modernmixins) on 1.13 - 1.13.2                                                       |
| 1.12.x     | ✅     | N/A      | ✅                 | N/A         | Requires [MixinBooter](https://modrinth.com/mod/mixinbooter) and a modified Velocity proxy                                            |
| 1.11.x     | ✅     | N/A      | ✅                 | N/A         | Requires [MixinBooter](https://modrinth.com/mod/mixinbooter) and a modified Velocity proxy                                            |
| 1.10.x     | ✅     | N/A      | ✅                 | N/A         | Requires [MixinBooter](https://modrinth.com/mod/mixinbooter) and a modified Velocity proxy                                            |
| 1.9.x      | ✅     | N/A      | ✅                 | N/A         | Requires [MixinBooter](https://modrinth.com/mod/mixinbooter) and a modified Velocity proxy                                            |
| 1.8.x      | ✅     | N/A      | ✅                 | N/A         | Requires [MixinBooter](https://modrinth.com/mod/mixinbooter) and a modified Velocity proxy                                            |
| 1.7.x      | ✅     | N/A      | ✅                 | N/A         | Requires [UniMixins](https://modrinth.com/mod/unimixins) and a modified Velocity proxy                                                |

## SpongeForge and SpongeNeo Compatibility

In most cases PCF shouldn't be needed, as Sponge supports legacy+modern forwarding and command argument wrapping. Known exceptions to this rule are listed below.

| MC Version | SpongeAPI Version | SpongeForge | SpongeNeo | Notes                                                                                                         |
|------------|-------------------|-------------|-----------|---------------------------------------------------------------------------------------------------------------|
| 1.16.5     | 8                 | ✅           | N/A       | SF's modern forwarding doesn't work                                                                           |
| 1.18.2     | 9                 | TBD         | N/A       |                                                                                                               |
| 1.19.4     | 10                | TBD         | N/A       |                                                                                                               |
| 1.20.6     | 11                | TBD         | TBD       |                                                                                                               |
| 1.21.1     | 12                | TBD         | ✅         | SN's modern forwarding doesn't work if Connector/FFAPI is installed (specifically `fabric_networking_api_v1`) |
| 1.21.3     | 13                | TBD         | TBD       |                                                                                                               |
| 1.21.4     | 14                | TBD         | TBD       |                                                                                                               |
| 1.21.5     | 15                | TBD         | TBD       |                                                                                                               |
| 1.21.8     | 16                | TBD         | TBD       |                                                                                                               |
| 1.21.10    | 17                | TBD         | TBD       |                                                                                                               |
| 1.21.11    | 18                | TBD         | TBD       |                                                                                                               |
| 26.1.x     | 19                | TBD         | TBD       |                                                                                                               |

## Modpack Compatibility

Try adding [BeQuietNegotiator](<https://modrinth.com/mod/be-quiet-negotiator>) to your NeoForge client if you're trying to join a Vanilla server.

| Modpack Name                                                                                       | Modpack Version | MC Version | Modloader Version  | As Primary/Forced-Host | Server Switching | Can Join Vanilla Servers | Notes                                                                                      |
|----------------------------------------------------------------------------------------------------|-----------------|------------|--------------------|------------------------|------------------|--------------------------|--------------------------------------------------------------------------------------------|
| [ATM10](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10)                             | 5.4             | 1.21.1     | NeoForge 21.1.215  | ✅                      | ✅                | ❌                        |                                                                                            |
| [ATM10 to Sky](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10-sky)                  | 1.5.1           | 1.21.1     | NeoForge 21.1.206  | ✅                      | ?                | ❌                        |                                                                                            |
| [FTB Presents Direwolf20](https://www.feed-the-beast.com/modpacks/126-ftb-presents-direwolf20-121) | 1.14.2          | 1.21.1     | NeoForge 21.1.172  | ✅                      | ?                | ❌                        |                                                                                            |
| [FTB Evolution](https://www.feed-the-beast.com/modpacks/125-ftb-evolution)                         | ?               | 1.21.1     | NeoForge 21.1.203  | ✅                      | ?                | ❌                        |                                                                                            |
| [FTB OceanBlock 2](https://www.feed-the-beast.com/modpacks/128-ftb-oceanblock-2)                   | 1.13.2          | 1.21.1     | NeoForge 21.1.194  | ✅                      | ?                | ❌                        |                                                                                            |
| [FTB Skies 2](https://www.feed-the-beast.com/modpacks/129-ftb-skies-2)                             | 1.9.2           | 1.21.1     | NeoForge 21.1.209  | ✅                      | ?                | ❌                        |                                                                                            |
| [Age of Fate](https://www.curseforge.com/minecraft/modpacks/age-of-fate)                           | 4.3.2           | 1.20.1     | Forge 47.4.0       | ?                      | ?                | ?                        |                                                                                            |
| [ATM9](https://www.curseforge.com/minecraft/modpacks/all-the-mods-9)                               | 1.1.0           | 1.20.1     | Forge 47.4.0       | ✅                      | ?                | ?                        |                                                                                            |
| [ATM9 to Sky](https://www.curseforge.com/minecraft/modpacks/all-the-mods-9-to-the-sky)             | 1.1.8           | 1.20.1     | Forge 47.4.0       | ✅                      | ?                | ?                        |                                                                                            |
| [Better MC 4](https://www.curseforge.com/minecraft/modpacks/better-mc-forge-bmc4)                  | 55              | 1.20.1     | Forge 47.4.13      | ✅                      | ?                | ?                        |                                                                                            |
| [Ozone Skyblock Reloaded](https://www.curseforge.com/minecraft/modpacks/ozone-skyblock-reborn)     | 1.19.1          | 1.20.1     | Forge 47.4.10      | ✅                      | ?                | ?                        |                                                                                            |
| [Workload](https://www.curseforge.com/minecraft/modpacks/workload)                                 | 1.1.0           | 1.20.1     | Forge 47.3.22      | ✅                      | ✅                | ?                        | FFCRP mod incompatible with VS                                                             |
| [Raspberry Flavoured](https://www.curseforge.com/minecraft/modpacks/raspberry-flavoured)           | 3.2.1           | 1.19.2     | Forge 43.4.20      | ✅                      | ?                | ?                        |                                                                                            |
| [FTB Presents Stoneblock 3](https://www.feed-the-beast.com/modpacks/100-ftb-stoneblock-3)          | 1.11.5          | 1.18.2     | Forge 40.2.34      | ✅                      | ?                | ?                        |                                                                                            |
| [TechBlock - SkyTech 3](https://modrinth.com/project/techblock-skytech-3)                          | 1.0             | 1.16.5     | Forge 36.2.35      | ✅                      | ?                | ?                        |                                                                                            |
| [The Pixelmon Modpack](https://www.curseforge.com/minecraft/modpacks/the-pixelmon-modpack)         | 9.1.13          | 1.16.5     | Forge 36.2.35      | ✅                      | ?                | ?                        |                                                                                            |
| [SkyFactory 4](https://www.curseforge.com/minecraft/modpacks/skyfactory-4)                         | 4.2.4           | 1.12.2     | Forge 14.23.5.2860 | ✅                      | ✅                | ✅                        | Requires [MixinBooter](https://modrinth.com/mod/mixinbooter) and a modified Velocity proxy |
| [FTB Infinity Evolved](https://www.curseforge.com/minecraft/modpacks/ftb-infinity-evolved)         | 3.1.0           | 1.7.10     | Forge 10.13.4.1614 | ✅                      | ?                | ✅                        | Requires [UniMixins](https://modrinth.com/mod/unimixins) and a modified Velocity proxy     |

## Hybrid Server Software Compatibility

| Server Software  | MC Version | Modloader | Modern Forwarding | CrossStitch | Notes                                                                                                                                                   |
|------------------|------------|-----------|-------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| Arclight         | 1.14.4     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.15.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.16.5     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| CatServer        | 1.16.5     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Magma            | 1.16.5     | Forge     | ✅                 | ✅           | Magma 1.16.5 is unstable in general and shouldn't be used                                                                                               |
| Mohist           | 1.16.5     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.17.1     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.18.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Catserver        | 1.18.2     | Forge     | ?                 | ?           | Wouldn't launch, even without PCF                                                                                                                       |
| Magma            | 1.18.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Magma Maintained | 1.18.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Mohist           | 1.18.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.19.2     | Forge     | ✅                 | ✅           | Arclight chat handling incompatibility requires a reconnect. Resolved by setting `advanced.modernForwardingVersion = "MODERN_DEFAULT"` in PCF's config. |
| GoldenForge      | 1.19.2     | Forge     | ✅                 | ✅           | Technically a fork of Forge, just implements perf patches, but doesn't support plugins.                                                                 |
| Mohist           | 1.19.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.19.3     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Magma            | 1.19.3     | Forge     | ✅                 | ✅           | PCF's pre-login hooks don't work. Magma's built-in modern forwarding works.                                                                             |
| Arclight         | 1.19.4     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Mohist           | 1.19.4     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.20.1     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Ketting          | 1.20.1     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Magma            | 1.20.1     | Forge     | ❌                 | ?           | "Bad VarInt decoded" when attempting to connect via Velocity                                                                                            |
| Magma Maintained | 1.20.1     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Mohist           | 1.20.1     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.20.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Ketting          | 1.20.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Mohist           | 1.20.2     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.20.4     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.20.4     | NeoForge  | ?                 | ?           | Wouldn't finish starting, even without PCF.                                                                                                             |
| Ketting          | 1.20.4     | Forge     | ✅                 | ✅           |                                                                                                                                                         |
| Arclight         | 1.21.1     | Forge     | ?                 | ?           | Wouldn't start, even without PCF.                                                                                                                       |
| Arclight         | 1.21.1     | NeoForge  | ✅                 | ✅           |                                                                                                                                                         |
| Magma Neo        | 1.21.1     | NeoForge  | ✅                 | ✅           |                                                                                                                                                         |
| TenetNeo         | 1.21.1     | NeoForge  | ?                 | ?           | Wouldn't start, even without PCF.                                                                                                                       |
| Youer            | 1.21.1     | NeoForge  | ?                 | ?           | Youer automatically removes PCF unless disabled in the config                                                                                           |
