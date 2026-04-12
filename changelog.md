# JMWS 1.2.5 for 1.21.11

*Server version 1.12*

## New# JMWS 1.2.5 for 1.21.11

*Server version 1.12*

## New

- Added new server-side command: /jmws_handshake
  - Tries to resend handshake to client.

## Bug fixes / changes

- Fixed issue where joining server with different mod-loader than the client or vice versa would not properly process JMWS handshake packet on the client and said client couldn't sync.

## Pre-existing bugs

Technically not a bug with JMWS, but I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

> ### Server
> Server version remains `1.12` up from `1.11`

- Added new command stated above.
- Changed how packets are sent from server to client.

1.12 is fully backwards compatible with 1.11, but it is recommended to update both client and server for QOL reasons.

- Added new server-side command: /jmws_handshake
  - Tries to resend handshake to client.

## Bug fixes / changes

- Fixed issue where joining server with different mod-loader than the client or vice versa would not properly process JMWS handshake packet on the client and said client couldn't sync.

## Pre-existing bugs

Technically not a bug with JMWS, but I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)

> ### Server
> Server version remains `1.12` up from `1.11`

- Added new command stated above.
- Changed how packets are sent from server to client.

1.12 is fully backwards compatible with 1.11, but it is recommended to update both client and server for QOL reasons. 