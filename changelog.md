# JMWS 1.2.5 for 1.21.1

*Server version 1.13*

## New

- No new features

## Bug fixes / changes

- Added logic to port synced JMWS waypoints and groups to JourneyMap. Once this is finished, JMWS should be uninstalled.

To transition synced JMWS waypoints and groups to JourneyMaps server waypoints, simply join a server with JMWS v1.2.6 and the latest version of JourneyMap.
The migration will happen automatically. JMWS will put itself in a disabled state for that server, and then you are free to uninstall it.

If you join a server with JMWS v1.2.6 on both sides but no JourneyMap on the server, JMWS will just function as normal.

- *What about my shared waypoints and groups?*

You will have to share them again manually using JourneyMap, which now supports shared waypoints and groups.

- *What about my global waypoints and groups?*

You will have to make them global manually using JourneyMap, which now supports global waypoints and groups.

- *What if I for some reason want to keep using JMWS?*

**You can continue to use it, but consider this a warning not to as I will not update JMWS for any new versions past 26.1.2
and JourneyMaps syncing system is far superior to JMWS. Including**
- Sharing UI
- Global object UI
- Seamless, native integration
- More stable, less buggy
- More frequent updates and guaranteed support
- Syncing will work on any server with JourneyMap v6 beta.71 or newer installed

## Pre-existing bugs

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

> ### Server
> Server version is `1.13` up from `1.12`
> 
> Very small server change where a new parameter was added to the sync packet to indicate to the client if JourneyMap is installed on the server.
> Despite the small change, this makes clients on 1.13 not able to join 1.12 servers, and if a 1.12 client joins a 1.13 server, there may be unexpected results.
> Please update both client and server immediately, at the same time.

Thank you for all your support.