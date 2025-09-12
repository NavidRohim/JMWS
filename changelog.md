# 1.1.6 Changelog

## Installation Changes (JMWS 1.21.8)

There have been changes regarding dependencies.
The server-side must have [CommonNetworking 1.0.21-1.21.7](https://www.curseforge.com/minecraft/mc-mods/common-network/files/all?page=1&pageSize=20) installed. 
As usual, the client-side must have JourneyMap 1.21.8-6.0.0-beta.52 or newer.

## New

This is mostly bug fixing and tweaks.

- Waypoint drag-and-drop now finally works (You MUST have JourneyMap Version 6 Beta 52 or newer for this to fix)
- JMWS will now sync as soon as JourneyMap is ready (There was a 3-second delay before, due to initialisation)

## Bug fixes / Changes

- Server will once again send handshake packet as soon as possible (There was a 3-second delay before because the clients map needed to initialise)
- Game can load without JourneyMap installed (JMWS will be disabled, before the game would send an error)
- CommonNetworking or JourneyMap is now sadly required on the server side. It has always been required, but I only realised now. I have no clue why it took me so long.
- Added unique text when death waypoint is made

## Persistent bugs that I am aware of

None. If there are any please do make a GitHub issue or join my discord. I will be happy to help



