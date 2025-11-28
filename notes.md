# todo
- ~~Get prototype of actual shared waypoint on client~~
- ~~Add tamper-proofing (validate that the clients requested shared waypoints are in the SUI file)~~ *Resolved due to rework*
- ~~Get user shared objs file on server to work.~~
- ~~Sync waypoint when updated~~
- ~~Delete waypoint when deleted by owner~~
- ~~Make command object selection argument type~~
- ~~Make temp strings into translation strings~~
- ~~Add sharing request timeout (20 seconds)~~
- ~~Check shared user permissions when deleting, modifying, etc (can user delete this? can the user update this?)~~
    - ~~Currently, an error is thrown.~~
- ~~Quilt?~~ No. I have a feeling it will die soon.
- ~~Option to disable share requests in config~~
- ~~Implement server config for sharing~~
- ~~Add group syncing~~
- ~~Change name of /share as it is quite common and to avoid confusion~~
- ~~Add server-wide object sharing (Admins can set up waypoints that are added to all clients)~~
- ~~Add specific player request accepting, since player can receive multiple requests.~~
- ~~Test on LAN~~
- ~~Add command hooks to both Forges~~
- ~~Make transitioning from old objs to new objs (reminder for tomorrow, do server TRANSITION packet tomorrow)~~ 

# bugs
- ~~/decline_share doesn't give text feedback~~ Resolved
- ~~Deleting a shared waypoint doesn't delete the shared user index file~~ Issue no longer valid. Resolved
- ~~Syncing just doesn't work.~~ Resolved
- ~~Updates only work once then shared user list is erased~~ Resolved
- ~~Object UUID changes when user leaves.~~ Resolved
- ~~Game crashes when player deletes all waypoints from in-built group~~ Resolved
- ~~Bug where outgoing share request just stays forever~~ Resolved
- ~~When making group, client crashes.~~ Resolved
- ~~When deleting group and not deleting child waypoints, if its shared with anyone they will get a notification saying they cannot edit the group. (Nothing should be sent)~~ Resolved?
- ~~When the owner of a global waypoint (admin) tries to delete one, the check for owner fails.~~
- ~~Another elusive bug where another NullPointerException is thrown. Not sure of cause maybe something to do with global objects~~ Was due to share request list using wrong UUIDs
- ~~Elusive bug where a NullPointerException is thrown while trying to delete shared waypoint on shared user client.~~ Have not had for a while when trying to recreate. Resolved but keep an eye out
- ~~No space when creating global group (Test)~~ Resolved
- ~~Global group doesnt remove when removed from server~~ Resolved
- ~~Global group stays locked after being localised.~~ Resolve
- ~~Global command suggestions don't work. (waypoints and groups)~~
- ~~Offline users name doesn't appear when sharing an object~~ Resolved
- ~~Offline users shared objects don't say who they are shared from when syncing~~ Wontfix
    - This is because the user is offline and their name cannot be resolved. (grabbed from server)
    - This might be fixable, but I will likely leave it
    - Could fix by accessing Mojang servers but this may not age well.
- ~~If objects haven't been transitioned to 1.2.0 system and player tries to use any new commands (global or sharing) a UUID error is thrown.~~
- ~~Waypoints / groups with duplicate names aren't shown in the object suggestion (differentiate by adding coordinates and dimension)~~
- ~~deleteAll throws an error on client~~
- ~~Rejection text doesn't work and timeout doesn't cancel. Exception thrown~~
- ~~Busy dialogue text doesnt work~~
- ~~when a Global / shared object owner edits the name, or the waypoint is deleted from a group, the object tag will be duplicated~~

### LAN bugs
- ~~Handshake fails due to new config value~~ Resolved. Update old config with new
- ~~Host cant receive share requests.~~ Resolved. 

### Bug testing
- ~~Test if user can receive multiple requests~~ Yes
- ~~Test if clients aside from host can receive sharing requests~~ Yes
- ~~Test if host can share waypoints to clients~~ No. 
  - Due to the way waypoints are stored on the server.
  - Hosts waypoints are not handled by JMWS
  - I.E. Hosts waypoints arent stored on server which means cannot make global or share.

### Fixed bugs
- Fixed bug where a shared users waypoints would not be deleted if the owner deleted the waypoint via /jmws clearAll

# Forges
- ~~Add server and client commands~~
  - ~~This includes command argument suggestions for waypoints and groups~~
- ~~Server started listener~~

# Testing
- Global waypoints (adding, removing)
- Sharing (Sharing, unsharing(?), adding share requests with multiple people)
    - How do shared objects work when owner is offline?
    - How does an already shared object behave when it's turned into a global object and vice versa?
- Test `/share_waypoint_stop` and group eq.
- Test server config

# RC Checklist
- Creating and deleting ✅ 
- Editing ✅ 
- Deleting in-build group waypoints ✅ 
- Delete all for groups ✅

User Commands

- clearAll ✅
- sync ✅
- getSyncInterval ✅
- nextSync ✅

Share Testing

- Follows server config rules ✅
- Can share to multiple people ✅
- Can decline ✅
- Is rejected by config ✅
- Can timeout ✅
- Tags working ✅
- Shared clients cannot edit objects ✅
- Cannot share to server owner player ✅

Global Testing

- Shared clients cannot edit globals ✅
- Tags working ✅
- Shares to everyone ✅
- Does not share to server owner player ✅

LAN 

- Owner cant use commands ✅
- Everything else works fine for clients ✅

Other

- Legacy version handling ✅
- Legacy object transitioning ✅




