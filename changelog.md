# 1.2.2 Changelog

## New

No new features. Very minor bug fix.

## Bug fixes

- Fixed a bug where if you tried to update a local-only waypoint, game would crash.
    - Issue discovered by Kaac on discord. I owe them this as it caused trouble for them.
- Fixed an issue where Waystones waypoints would duplicate / not show at all.
    - Resolves https://github.com/NavidRohim/JMWS/issues/5
- JMWS no longer syncs objects belonging to other plugins. Only player-made objects.

## Bugs that I am aware of

- When using fullscreen map, you cannot delete synced objects with the dropdown menu.
  - This is a JourneyMap problem and will be fixed in the future.

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

### Server

Server version is still 1.101 (No change)