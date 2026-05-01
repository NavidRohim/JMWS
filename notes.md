# todo - 2.0-26.1

## -- Internals --
- ~~Fix sharing system. Was built for server-side command dispatching, but that will now move to the client.~~
- - ~~With the new sharing system, a packet needs to be sent to the server to notify that the object has been shared and to update the client.~~
- Convert waypoint / group creation to a more generic creation system.
- Make sharing rules. Users can enable/disable certain rules for sharing. Like
  - Receiving users can only get shared objects while the owner of the object is online.

## -- Sharing UI --
- ~~Add UI colour indicator for object status (shared (green), awaiting share (orange), not shared (grey), cannot share (red))~~
- ~~Add selection boxes so you can send share requests for multiple players.~~
- ~~Add select all / deselect all buttons.~~
  ~~- Position these buttons in the top right corner of the scroll list. Make a part of the CheckableSelectionList class. Though, buttons cannot be natively added to ObjectSelectionList or children of it, so maybe make a generic abstract screen class for this behaviour (CheckableSelectionList will need to be passed to this generic screen class for button placement)~~

## -- General / Other UI --
- ~~Disable the share request list button when JMWS is off. Straightforward to do, but I am exhausted.~~
- ~~Add a screen for waiting share requests.~~
- ~~Compartmentalise UI classes so they are more dynamic and can be used in more places.~~
- ~~Fix UI internals to how things are positioned (Use layouts and grids, much more dynamic I have heard.)~~
- ~~Refactor the ObjectSharePanel class (split it) so it can be used in more places. For example, the checkbox behaviour in Entries will need to rely on a subclass of it and I plan to use this Entry class in more areas.~~
- ~~Make a custom asset for the incoming share request button.~~
- ~~Test on Windows. Been coding all this on macOS.~~
- ~~Remake JMWS on-off toggle button and sync button icons.~~
- Generally build up the UI API.
- ~~Make an unshare screen.~~
- Make a screen for admins / owner so they can manage global objects. (maybe also make a CLI tool?)
- ~~Make the refresh button on all screens actually functional. Doesn't seem to do much at the moment.~~
- ~~Make select / deselect all buttons into checkboxes.~~~~
- Make the sync manager screen.
  - This will be quite complex. Any registered sync type in ClientSyncRegistry will have to have its own interface which will be used to fetch all objects from that extension.
  - I am yet to even think of the design of the screen, but I may add a sidebar with all registered types, you click that, and it will show another screen with all objects.

## -- General bugs --
- ~~Sharing status label does not update when an object is successfully shared~~
- ~~Waypoint context menu breaks after updating an already existing object~~
- ~~Timeout is not cancelled when an object is successfully shared.~~
- ~~Timeout alert is not sent after the first timeout.~~
- ~~When accepting a share request, the receiving client cannot display the object. A casting error is thrown.~~
- ~~If a client or server does not have a packet registered, the game will crash.~~
- ~~When accepting a share request with a user, the server wipes the objects syncInfo field.~~
- ~~Fix the timeout bug~~
- ~~When the share screen is initially drawn, offline users appear skewed. Draw the screen's initial state correctly or immediately redraw.~~
- clearAll is broken.

## -- Todo tomorrow --

- Ensure on the server side that any given user has permissions to make a global object.

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
