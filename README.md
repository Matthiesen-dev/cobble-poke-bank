# Cobble Poke Bank

<div>
  <img src="https://mods.matthiesen.dev/badges/matthiesenCore.svg" alt="Matthiesen Core">
  <img src="https://mods.matthiesen.dev/badges/cobblemon.svg" alt="Cobblemon">
  <img src="https://mods.matthiesen.dev/badges/gooeylibs.svg" alt="GooeyLibs">
</div>

Cobble Poke Bank is a Server-Side mod for Cobblemon that allows players to store their Pokémon in a database-backed bank. Designed for use in single (SQLite) 
or multiserver (MySQL) environments, this mod provides a persistent storage solution for Pokémon, ensuring that they are not lost during world resets or server transitions.

## Requirements

- [Matthiesen Core](https://modrinth.com/mod/matthiesen-core)
- [Cobblemon](https://modrinth.com/mod/cobblemon)
- [GooeyLibs](https://modrinth.com/mod/gooeylibs)
- [Minecraft SQLite JDBC](https://modrinth.com/plugin/minecraft-sqlite-jdbc)
- [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric only)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (Fabric only)

## Optional Dependencies

- [Minecraft MySQL JDBC](https://modrinth.com/plugin/minecraft-mysql-jdbc) (Required for Multi-Server setups)

## Usage

- Run `/pokebank` in-game to open the bank menu.
- Run `/pokebank status` to view basic system status details (admin permission required).
- Configure player bank capacity and other restrictions in `config/cobble_poke_bank/server.toml` (`bank.maxSlots`):
  - `<= 0` = unlimited
  - `> 0` = fixed slot cap
- Configure your database connection in `config/cobble_poke_bank/database.toml`:
  - `useMySQL` = `true` for MySQL, `false` for SQLite

## Docs

Documentation for this mod can be found at [mods.matthiesen.dev](https://mods.matthiesen.dev/cobble-poke-bank/)

## Version Compatibility

| Minecraft Version | Cobblemon Version | Mod Version |
|-------------------|-------------------|-------------|
| 1.21.1            | 1.7.3             | 1.x.x       |

## FastStats Metrics

This mod uses [FastStats](https://faststats.dev) to collect anonymous usage statistics. This helps the developer understand
how this mod is being used and improve it over time. You can learn more about the data collected and how it is used by visiting
[FastStats: Information](https://faststats.dev/info).

You can also view the data collected by this mod on the [FastStats: Cobble Poke Bank](https://faststats.dev/project/cobble-poke-bank) page.

To opt out of this data collection, set the `enabled` property to `false` in the `<game_directory>/config/matthiesen_core/metrics.properties` file.

## License

MIT - see `LICENSE`.
