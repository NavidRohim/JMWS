# 2.0 Changelog
*Server version 2.0*

## New

- New command
  - `remove_global_no_op waypoint|group <object_identifier>` --> Removes a global object that was made by an opped player who is no longer opped. 

## Bug fixes / changes

- Added text response if a global object is already global.
- Added server-side config option for handshake packet delay, before, some clients may not have been able to sync if on a lower-end computer. This can now be fixed by extending the handshake packet delay. Default is 250.
- Internal changes

## Pre-existing bugs

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

>### Server
>
>Server version is now `1.11` up from `1.103`

- Added new command stated above.
- Added new client text.

Due to the new client text and server changes, plus changes with JourneyMap, all clients connecting to a 1.11 server **MUST** also be on 1.11.

If a client isn't on the correct version, it could cause catastrophic side effects for said client.
