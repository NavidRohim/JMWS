# todo - 2.0-26.1

## -- Internals --
- Fix sharing system. Was built for server-side command dispatching, but that will now move to the client.

## -- Sharing UI --
- ~~Add UI colour indicator for object status (shared (green), awaiting share (orange), not shared (grey), cannot share (red))~~
- ~~Add selection boxes so you can send share requests for multiple players.~~
- ~~Add select all / deselect all buttons.~~
  ~~- Position these buttons in the top right corner of the scroll list. Make a part of the CheckableSelectionList class. Though, buttons cannot be natively added to ObjectSelectionList or children of it, so maybe make a generic abstract screen class for this behaviour (CheckableSelectionList will need to be passed to this generic screen class for button placement)~~

## -- General / Other UI -- 
- ~~Add a screen for waiting share requests.~~
- ~~Compartmentalise UI classes so they are more dynamic and can be used in more places.~~
- ~~Fix UI internals to how things are positioned (Use layouts and grids, much more dynamic I have heard.)~~
- ~~Refactor the ObjectSharePanel class (split it) so it can be used in more places. For example, the checkbox behaviour in Entries will need to rely on a subclass of it and I plan to use this Entry class in more areas.~~
- Generally build up the UI API.

## -- General bugs --
- ~~Sharing status label does not update when an object is successfully shared~~
- ~~Waypoint context menu breaks after updating an already existing object~~
- Timeout is not cancelled when an object is successfully shared.
- ~~Timeout alert is not sent after the first timeout.~~
- ~~When accepting a share request, the recieving client cannot display the object. A casting error is thrown.~~
- ~~If a client or server does not have a packet registered, the game will crash.~~
- 
## -- Todo tomorrow --

- Fix the timeout bug
- With the new sharing system, a packet needs to be sent to the server to notify that the object has been shared and to update the client.

*Already finished*

## -- Done --

- ~~Fix share request not being added~~
- ~~Add select / deselect all buttons~~
  ~~- Position these buttons in the top right corner of the scroll list. Make a part of the CheckableSelectionList class. Though, buttons cannot be natively added to ObjectSelectionList or children of it, so maybe make a generic abstract screen class for this behaviour (CheckableSelectionList will need to be passed to this generic screen class for button placement)~~
- ~~Should start with UI class refactoring. Will make everything else much easier. Then probably fixing layouts~~
- ~~Fix player list only updating when disconnecting and rejoining.~~
- ~~Add logic to check on client if a share request has already been sent out~~
- ~~Add indicator if player already has a pending share request from client~~
- ~~Fix UI bug where the scroll list shadow goes past the scroll bar~~
- ~~Share request doesn't get removed once accepted.~~
- ~~Ensure `OutgoingShareRequests` does NOT have duplicate entries.~~
- ~~Sharing labels not working. (Shows already shared text when pending)~~
