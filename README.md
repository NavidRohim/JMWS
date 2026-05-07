For people who want to compile JMWS and use it in a dev environment:

## Paper server adapter

The Paper build is a server-side plugin for Paper 26.1.2 servers. Players still need the matching JMWS client-side mod installed with JourneyMap; the Paper plugin speaks the same `jmws:action_command` and `jmws:jmws_handshake` protocol as the modded server builds.

Build it with Maven:

```shell
mvn -f paper/pom.xml package
```

The jar is created at `paper/target/JMWS-Paper-1.2.5-26.1.x.jar`.

On Paper, JMWS stores server config and synced data under the plugin data folder:

```text
plugins/JMWS/config.yml
plugins/JMWS/waypoints/
plugins/JMWS/groups/
plugins/JMWS/users/
```

Run `/jmws reload` from console or in game with `jmws.reload` permission after editing `plugins/JMWS/config.yml`.

Normal sync action bar alerts are disabled by default for the Paper adapter. Set `syncAlertsEnabled` to `true` in `plugins/JMWS/config.yml` if you want the client to show a message after routine syncs.

The Paper adapter also sends a best-effort sync request when a player quits. Set `syncOnQuitEnabled` to `false` if you want to disable that extra logout sync request.

## NeoForge dev note

if you get a rendering error with neoforge, go to neoforge/run/config/neoforge-client.toml and change `enableB3DValidationLayer = true` to `enableB3DValidationLayer = false`
