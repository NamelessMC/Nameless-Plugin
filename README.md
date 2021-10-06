# Nameless-Plugin
The official Minecraft plugin for NamelessMC v2. For compiled files see the [spigot resource page](https://www.spigotmc.org/resources/nameless-plugin-for-v2.59032/)

## Features
* Register command
* Report command
* User information command
* Validate command
* Notifications command
* Permissions
* Mvdw and PlaceholderAPI placeholders for number of notifications
* Server data sender (the plugin can send detailed information about the minecraft servers and the players online to the website)
* Group sync
* Whitelist registered users

## Installation
1. Install the plugin jar file in the `plugins` folder
2. Restart the server
3. Modify `config.yml`: enter API URL and server id.
4. Run `/nameless reload`

## Translations
<a href="http://translate.namelessmc.com/engage/namelessmc/">
<img src="http://translate.namelessmc.com/widgets/namelessmc/-/spigot-plugin/multi-auto.svg" alt="Translation status" />
</a>

## Compiling

Requirements: Maven, JDK 8, git (any JDK >8 will also work as long as your server doesn't use a lower JDK version)

`apt install maven openjdk-8-jdk git`

```sh
git clone https://github.com/Derkades/Derkutils
cd Derkutils
git checkout legacy # important!
mvn install
cd ..

git clone https://github.com/kennytv/Maintenance
cd Maintenance
git checkout refs/tags/3.0.7
mvn install
cd ..

git clone https://github.com/NamelessMC/Nameless-Java-API
cd Nameless-Java-API
mvn install
cd ..

git clone https://github.com/NamelessMC/Nameless-Plugin
cd Nameless-Plugin
mvn package shade:shade
cd target
# find jar file here
```

## Discord
[<img src="https://discordapp.com/api/guilds/246705793066467328/widget.png?style=shield">](https://discord.gg/J6QsVaP)

## v1
The legacy NamelessPlugin for v1 is available for download [on spigot](https://www.spigotmc.org/resources/official-namelessplugin.42698/). The source is available in the v1-Pre-1.1 branch.
