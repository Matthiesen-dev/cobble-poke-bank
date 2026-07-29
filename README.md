# Cobble Poke Bank

<div>
  <img src="https://mods.matthiesen.dev/badges/matthiesenCore.svg" alt="Matthiesen Core">
  <img src="https://mods.matthiesen.dev/badges/cobblemon.svg" alt="Cobblemon">
  <img src="https://mods.matthiesen.dev/badges/gooeylibs.svg" alt="GooeyLibs">
</div>

Cobblemon pokemon database backed storage, usable during world resets, or for multi-server setups where you can store and transfer pokemon between servers.

Cobble Poke Bank is a Server-Side mod for Cobblemon that allows players to store their Pokémon in a database-backed bank. Designed for use in single (SQLite) 
or multiserver (SQLite or MySQL) environments, this mod provides a persistent storage solution for Pokémon, ensuring that they are not lost during world resets or server transitions.

## Requirements

- [Matthiesen Core](https://modrinth.com/mod/matthiesen-core)
- [Cobblemon](https://modrinth.com/mod/cobblemon)
- [GooeyLibs](https://modrinth.com/mod/gooeylibs)

## Usage

- Run `/pokebank` in-game to open the bank menu.
- Configure player bank capacity in `config/cobble_poke_bank/config.json` (`bank.maxSlots`):
  - `<= 0` = unlimited
  - `> 0` = fixed slot cap
- Configure your database connection in `config/cobble_poke_bank/database.json`:
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
