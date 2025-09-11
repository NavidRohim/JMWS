# 1.1.6 Changelog

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

None. If there are any please do make a GitHub issue or join my discord.



