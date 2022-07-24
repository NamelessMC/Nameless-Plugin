# Nameless-Plugin

[![build status](https://ci.rkslot.nl/buildStatus/icon?job=Nameless+Plugin)](https://ci.rkslot.nl/job/Nameless%20Plugin/)
[![translation status](https://translate.namelessmc.com/widgets/namelessmc/-/spigot-plugin/svg-badge.svg)](https://translate.namelessmc.com/engage/namelessmc/)
[![discord](https://discord.com/api/guilds/246705793066467328/widget.png?style=shield)](https://discord.gg/nameless)

The official Minecraft plugin for NamelessMC v2. For compiled files see the [spigot resource page](https://www.spigotmc.org/resources/nameless-plugin-for-v2.59032/)

## Features
* Multi-platform! Supports Spigot 1.8-1.19, BungeeCord, Velocity, Sponge 7-9.
* Commands to register or verify an account, report a player, read website notifications and more.
* Configurable command names to avoid conflicts
* Configurable messages with translation support
* Server data sender (the plugin can send detailed information about the minecraft servers and the players online to the website)
* Sync Minecraft groups to website groups
* Whitelist registered users
* Ban users on website when banned in-game
* PlaceholderAPI placeholder for number of notifications (bukkit only)
* Send placeholders to website for leaderboards (bukkit only)
* Integration with Websend module to view server logs in StaffCP and run commands on the server.
* Display website announcements in chat

## Installation
1. Install the plugin jar file in the `plugins` folder
2. Restart the server
3. Modify `config.yaml`: enter API URL and server id.
4. Run `/nlpl reload`

## Translations
<a href="http://translate.namelessmc.com/engage/namelessmc/">
<img src="http://translate.namelessmc.com/widgets/namelessmc/-/spigot-plugin/multi-auto.svg" alt="Translation status" />
</a>

## Compiling

Requirements: Maven, Git, JDK 11, JDK 17 (only required for paper and sponge9)

On Debian/Ubuntu: `apt install maven git openjdk-11-jdk openjdk-17-jdk`

```sh
git clone https://github.com/Derkades/Derkutils
cd Derkutils
git checkout spigot-1.13 # important!
mvn clean install # Uses JDK 11
cd ..

git clone https://github.com/NamelessMC/Nameless-Java-API
cd Nameless-Java-API
mvn clean install # Uses JDK 11
cd ..

git clone https://github.com/NamelessMC/Nameless-Plugin
cd Nameless-Plugin
mvn clean package # Uses JDK 11 and 17
# find jar in {bungeecord,paper,spigot,sponge7,sponge8,sponge9,velocity}/target/*
```

Building the entire project can take quite a long time. You might want to build a single module only:
```sh
mvn package -pl velocity -am
```
