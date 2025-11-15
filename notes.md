# todo
- ~~Get prototype of actual shared waypoint on client~~
- ~~Add tamper-proofing (validate that the clients requested shared waypoints are in the SUI file)~~ *Resolved due to rework*
- ~~Get user shared objs file on server to work.~~
- ~~Make transitioning from old objs to new objs~~ Implemented, need to test more. 

- ~~Sync waypoint when updated~~
- ~~Delete waypoint when deleted by owner~~
- Make command object selection argument type
- ~~Make temp strings into translation strings~~
- ~~Add sharing request timeout (20 seconds)~~
- Add command hooks to both Forges
- Check shared user permissions when deleting, modifying, etc (can user delete this? can the user update this?)
    - Currently, an error is thrown.
- Quilt?
- Option to disable share requests in config

# bugs
- ~~/decline_share doesnt give text feedback~~ Resolved
- ~~Deleting a shared waypoint doesn't delete the shared user index file~~ Issue no longer valid. Resolved
- ~~Syncing just doesn't work.~~ Resolved
- ~~Updates only work once then shared user list is erased~~ Resolved
- ~~Object UUID changes when user leaves.~~ Resolved
- Game crashes when player deletes all waypoints from in-built group