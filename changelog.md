# 1.2.4 Changelog

## New

- New command
  - `remove_global_no_op waypoint|group <object_identifier>` --> Removes a global object that was made by an opped player who is no longer opped. 

## Bug fixes

- Added text response if a global object is already global.

## Pre-existing bugs

- When using fullscreen map, you cannot delete synced objects with the dropdown menu.
  - This is a JourneyMap problem and will be fixed in the future.

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

### Server

Server version is 1.103

- Added new command stated above.
- Added new client text.

Due to the new client text, servers will **expect** clients to be
compatible with 1.103. 

Technically a client can join without issue (no crashes or errors) but it will produce unexpected text feedback results.
