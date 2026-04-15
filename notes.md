# todo - 2.0-26.1

## -- Internals --
- Fix sharing system. Was built for server-side command dispatching but that will now move to the client.

## -- Sharing UI --
- ~~Add UI colour indicator for object status (shared (green), awaiting share (orange), not shared (grey), cannot share (red))~~
- Add selection box so you can send share requests for multiple players.

## -- General / other UI -- 
- Add screen for waiting share requests.
- ~~Compartmentalise UI classes so they are more dynamic and can be used in more places.~~
- Fix UI internals to how things are positioned (Use layouts and grids, much more dynamic I have heard.)

## -- General bugs --
- Sharing status label does not update when an object is successfully shared
- Waypoint context menu breaks after updating already existing object

## -- Todo tomorrow --
- Find out if I can change the focus / highlight outline colour from white to whatever the current colour is for an object.

Should start with UI class refactoring. Will make everything else much easier. Then probably fixing layouts

*Already finished*

- ~~Fix player list only updating when disconnecting and rejoining.~~
- ~~Add logic to check on client if a share request has already been sent out~~
- ~~Add indicator if player already has a pending share request from client~~
- ~~Fix UI bug where the scroll list shadow goes past the scroll bar~~
- ~~Share request doesn't get removed once accepted.~~
- ~~Ensure `OutgoingShareRequests` does NOT have duplicate entries.~~
- ~~Sharing labels not working. (Shows already shared text when pending)~~
