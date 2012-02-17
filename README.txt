[CHAT] SimpleAlias v1.0.2 Chat name aliases [cb612]

Simple Alias

[url=http://dl.dropbox.com/u/1660511/SimpleAlias/SimpleAlias.zip]Download[/url]
[url=https://github.com/madcap/SimpleAlias]Source[/url]
[url=http://dl.dropbox.com/u/1660511/SimpleAlias/SimpleAlias.jar]Latest Jar Only[/url]


SimpleAlias will allow players to give themselves an alias (nickname) that displays when they use chat.

For Example, my login name is madcap_magician. By using the command `/alias Madcap` now whenever I use chat I will appear as Madcap.

You can clear your alias by issuing the `/alias` command with no arguments. The command /nickname behaves the same way and you should use that command instead if you have other plugins that use the /alias command. 

[b]Features[/b]
* Persistance - Aliases are saved to a file between server restarts.
* Command Replacement - Player issued commands containing asliases are automatically replaced with full names.
* Alias Blacklist - Server admins can specify names players can't use such as "Notch" and "Admin".
* Permissions Support - Only Players you allow can use this the /alias command.
* Security - players can not use the name of another player as their alias.

Aliases are currently limited to 12 characters, must be only 1 word and must be alphanumeric. All alias activity is logged in case of miss-use.

The plugin now uses alias_config.yml in the plugin's data folder. It will work without this file but the blacklist feature won't work. In the YML file you can specify a list of names which are not allowed for aliases. I've provided a few already. The config file will be automatically updated when it is revised but your changes will not be over-written.

Sample config file:
[CODE]## list any names here that you do not want your players using (not case sensitive)
banned-aliases:
  - "notch"
  - "admin"
  - "administrator"
  - "server"
  - "console"[/CODE]
  
  
[b]Permissions Support[/b]
This plugin now supports the Permissions plugin (http://forums.bukkit.org/threads/5974). If the permissions plugin exists and is enabled then the node 'SimpleAlias.*' is required for access to the /alias command, otherwise all players will have access to the /alias command.
Here is an example granting permission to use /alias to the 'Players' group:
[CODE]
    Players:
        default: false
        info:
            prefix: ''
            suffix: ''
            build: true
        inheritance:
        permissions:
            - 'SimpleAlias.*'[/CODE]

  
Changelog:
1.0.2
* Updated for CB733
1.0.1
* Changed PLAYER_JOIN priority to prevent conflicts with Essentials plugin
* Added command /nickname to deal with command name conflict on CommandHelper plugin
* updates for CB612
1.0.0
* Added support for permissions
* Player issued commands containing aliases are automatically translated into full login names
0.0.3
* Changed yml file to no longer require a world directory (now it comes from server.properties).
0.0.2
* Added checking alias name against player login names.
* Added list of aliases players aren't allowed to use (such as Notch, Admin etc).
* Added .yml file for plugin configuration.
* Fixed stupidly long constructor warning.
0.0.1
* Release
