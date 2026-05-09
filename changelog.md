# JMWS 1.2.6 for 26.1.x

*Server version 1.13*

## New

- No new features

## Bug fixes / changes

- Added logic to port synced JMWS waypoints and groups to JourneyMap. Once this is finished, JMWS should be uninstalled.

## Pre-existing bugs

Technically not a bug with JMWS, but I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

> ### Server
> Server version is `1.13` up from `1.12`
> 
> Very small server change where a new parameter was added to the sync packet to indicate to the client if JourneyMap is installed on the server.
> Despite the small change, this makes clients on 1.13 not able to join 1.12 servers, and if a 1.12 client joins a 1.13 server, there may be unexpected results.
> Please update both client and server immediately, at the same time.