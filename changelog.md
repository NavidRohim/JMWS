# 1.1.7 for 1.12.2 Changelog

## New

- JMWS will now work on LAN servers.
- Auto-Syncing can be disabled in the config.
- JMWS syncs immediately instead of a 3 second delay.

## Bug fixes / Changes

- The servers JMWS configuration is now visible to the client in the forge config screen
    - This may be helpful if you find you cannot create waypoints.
- When JourneyMap is not installed on the client side, a screen is now shown instead of an overly-verbose crash report.
- Added server version checking. This is to make debugging easier.
- Added JavaDocs to GitHub
- Server now sends handshake instead of the client
- Fixed bug where getSyncInterval and nextSync commands wouldn't work.
- Fixed bug where coloured action bar text would glitch
- 
## Bugs that I am aware of

None!

### Other
*Server version is 1.00*