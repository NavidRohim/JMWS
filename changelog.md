# 1.1.7 Changelog

## New

No new features.

## Bug fixes / Changes

- When JourneyMap is not installed on the client side, a screen is now shown instead of an overly-verbose crash report.
- Fixed problem where a waypoint in the nether wouldn't function properly unless you were in the nether.
- Added server version checking. This is to make debugging easier.

## Bugs that I am aware of

- Coordinates in nether relative to other dimensions is wrong (will teleport you to wrong location)
- Couldn't delete waypoints in nether when in dimension other than nether (and vice versa)

Server version is 1.01