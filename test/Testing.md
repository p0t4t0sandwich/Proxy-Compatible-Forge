# Testing

## Configuration

If you have any ideas on how to automate the following, please contribute!

Run `./gradlew :test:headlessmc` to configure the following.

Note: This command must be re-run if you use one of the HeadlessMC setup tasks.

### Version Specific Mods

Use: `server list` or `versions` to get the indexed list of installed servers/versions.
- The server command to add a mod is: `server mod add <index> <mod>`
- The client command to add a mod is: `mod add <index> <mod>`

Required to function server-side:
- 1.7.10: `unimixins`
- 1.12.2: `mixinbooter`
- 1.13.2: `modernmixins`
- 1.14.4: `mixinbootstrap`

Good for testing edge cases:

Forge 1.20.1:
- `forgified-fabric-api` (server)

NeoForge 1.21.1:
- `forgified-fabric-api` (both)
