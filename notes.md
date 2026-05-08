# todo - 2.0-26.1

## -- Internals --
- ~~Fix sharing system. Was built for server-side command dispatching, but that will now move to the client.~~
- - ~~With the new sharing system, a packet needs to be sent to the server to notify that the object has been shared and to update the client.~~
- Convert waypoint / group creation to a more generic creation system.
- ~~Make sharing rules. Users can enable/disable certain rules for sharing. Like~~
  ~~- Receiving users can only get shared objects while the owner of the object is online.~~
- Make ServerObject more agnostic of incoming data. Currently, it relies too much on JM data like the name field and customDataMap.

### -- Sharing rules and settings
- Currently working on the client. But when entering the settings UI, whatever settings that were saved before are not loaded.
- Need to handle on the server and provide rule settings to `ShareRule::passed`. Will probably use the same `RuleSetting` class but will have to code a decoder for it.

The internals of RuleSetting and its wrapper are troubling me. It does technically work, but in which way they work does not feel right.

In `ShareSettingsScreen`, in the save method, JMWS iterates through each rule registered and then iterates through each setting for that rule.
This is needed to save the state of whatever setting is being displayed and save it to the actual RuleSetting. This is necessary, but the way the values are handled
afterwards is less than ideal, or at least feels so. The values are put in `ruleHashmap` to be serialised into a string when the request is sent. 
But what if the user goes back into the settings UI again? The settings saved beforehand will not be loaded back into the new instance of `ShareSettingScreen`

Once the end user hits save in `ShareSettingScreen` the values are immediately useless to the settings screen. I want them to be more versatile.
I believe I need to defer the serialisation into JSON until the request is sent. But I am unsure how to save them well in the HashMap.

After the `save()` method is called, I could store the RuleSetting AFTER `setValueForParent()` is called, then when sent, serialise into JSON after
it is confirmed those are the user’s true settings. This also has the plus side, where when the constuctor for `ShareSettingScreen` is called, I could make an
alternative constuctor that accepts those `RuleSetting`s instances and reconstucts the displayable element to whatever it's last saved state was.

## -- DatePicker UI --
- Set the date picker so the client cannot set the date to a date in the past. This has been implemented somewhat in the way that you cannot scroll to a month or year before the current one, but a day in the past can still be selected


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
- ~~clearAll is broken.~~

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
