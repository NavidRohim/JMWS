# 1.2.1 Changelog

## New

No new features.

## Changes
- Can now change the sync interval to any number between 2 and 2,147,483,647 (64-bit int limit) Before was 2 - 120
- Added sync on disconnect. Thought there was before but apparently not, whoops.

## Bug fixes

- Fixed a bug where, if a local object was not synced with the server, an error would be thrown if deleted.


All changes here are fully thanks to "notdysthymia" on GitHub. Partially resolves issue 5: https://github.com/NavidRohim/JMWS/issues/5

## Bugs that I am aware of

- Unpredictable waystones waypoint behaviour.
  - This has been reported by notdysthymia. They had their waystone waypoints wiped. Personally when testing, the waypoints stay, but they duplicate.
  - There is definitely something wrong and there will be a fix in a future version.

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed, in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

### Server

New server version: 1.11

Very slight changes. Should cause no issues but still a good idea to update.
All clients who use JMWS 1.2.0 can join servers with 1.2.1 or vice versa.

### For Devs

1.2.1 Changes to Gradle 9.3.0, and also changes to ForgeGradle 7.

This introduces massive changes, mainly regarding the move from ForgeGradle 6 to ForgeGradle 7. 
FG7 is still in beta and still buggy in noticeable ways. If at all possible, check [here](https://files.minecraftforge.net/project_index.html) for new ForgeGradle 7 versions
which will make your life developing easier.

Also check [this repo](https://github.com/MinecraftForge/MDKExamples) for ForgeGradle 7 examples.

Thanks!