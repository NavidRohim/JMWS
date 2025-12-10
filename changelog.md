# 1.2.1 Changelog

## New

No new features.

## Changes / Fixes
- Can now change the sync interval to any number between 2 and 2,147,483,647 (64-bit int limit) Before was 2 - 120
- Added sync on disconnect. Thought there was before but apparently not, whoops.

All changes / fixes here are fully thanks to "notdysthymia" on GitHub. Resolves: https://github.com/NavidRohim/JMWS/issues/5

## Bugs that I am aware of

- Unpredictable waystones waypoint behaviour.
  - This has been reported by notdysthymia. They had their waystone waypoints wiped. Personally when testing, the waypoints stay, but they duplicate.
  - There is definitely something wrong and there will be a fix in a future version.

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed, in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

### Server

No new server version. Still 1.1

All clients who use JMWS 1.2.0 can join servers with 1.2.1 or vice versa.

Thanks!