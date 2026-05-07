# JMWS 1.2.5 for 26.1.x

*Server version 1.12*

## New

- Added new server-side command: /jmws_handshake
  - Tries to resend handshake to client.
- Added a Maven-built Paper server adapter.
  - Paper servers can now run JMWS as a server plugin while players keep using the existing JMWS client-side mod.
  - The Paper plugin uses the existing `jmws:action_command` and `jmws:jmws_handshake` payload channels for client compatibility.
  - Paper config is stored at `plugins/JMWS/config.yml`; synced waypoint data is stored under `plugins/JMWS`.
  - Paper migrates values from the older `plugins/JMWS/jmws-server.json` file into `config.yml` when the YAML config does not exist yet.
  - Added `/jmws reload` with `jmws.reload` permission to reload Paper config changes and refresh online client handshakes.
  - Paper sends several delayed handshake attempts on player join and logs the first client payload it receives to make client communication visible in the server log.
  - Routine Paper sync alerts are disabled by default and can be re-enabled with `syncAlertsEnabled` in `plugins/JMWS/config.yml`.
  - Paper sends a best-effort client sync request on player quit, configurable with `syncOnQuitEnabled`.

## Bug fixes / changes

- Fixed issue where joining server with different mod-loader than the client or vice versa would not properly process JMWS handshake packet on the client and said client couldn't sync.

## Pre-existing bugs

Technically not a bug with JMWS, but I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

> ### Server
> Server version remains `1.12` up from `1.11`

- Added new command stated above.
- Changed how packets are sent from server to client.

1.12 is fully backwards compatible with 1.11, but it is recommended to update both client and server for QOL reasons. 
