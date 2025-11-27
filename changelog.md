# 1.2.0 Changelog

## New

- New feature: Shared objects
- New feature: Global objects

Read below for what they are and do.

### What are objects?

"Objects" is simply a generalised term I use for waypoints or groups.

## Bug fixes / Changes

- Fixed possible bug where waypoints / groups added by 3rd party plugins of JM would be persistent when created (would reappear after being deleted and joining back)

### What are shared objects?
Shared objects are just waypoints or groups that have been shared by one player to others.
These objects update live (changing name, location, etc) which includes deletion.

#### How do they work?
Create a waypoint or group like normal. Then do `/share_waypoint <share_to> <waypoint>` where `share_to` is the user you want to share with,
and `waypoint` is the name of the waypoint you want to share. This is the same for groups but using `/share_group`

The user who receives the object will have to accept the share request, there is a 20 second timeout. If this time passes, the request will automatically
be declined.

#### How do I stop sharing an object?
Simply do `/share_waypoint_stop <waypoint> <remove_from>` where `waypoint` is the name of the waypoint you want to stop sharing with.
`remove_from` is the name of the user you want to stop sharing with. You can leave this blank if you want to stop sharing with everything.
This is the same with waypoints but you do `/share_group_stop`

### What are global objects?

Global objects are like shared objects, but they are shared with everyone at the same time! These can only be made by administrators.

#### How do they work?
Create a waypoint or group like normal. Then do `/jmws_admin create_global_waypoint <waypoint>` where `<waypoint>` is the name of waypoint
that will become global. This is the same for groups but using `/jmws_admin create_global_group`

#### How do I stop sharing a global object?
Do `/jmws_admin remove_global_waypoint <waypoint>` where `<waypoint>` is the name of the global waypoint you want to remove.
This is the same for groups but using `/jmws_admin remove_global_group`. Also, "remove" means to just stop sharing it with everyone.
The player who originally made it will regain it as normal waypoint

#### I want to know more.
When a shared / global object is created, users will gain access to it but they will not be able to modify it. Only the original creator can.
And, if in a LAN world, the host cannot share nor create global objects.

Also, if an object is global, you'll see a little `(G)` at the end of the name. You can turn this off in the config (Uncheck "Global Label?")
This is the same with shared objects, but instead you'll see the name of the user who owns the object. And if they are offline, it'll just be `(S)`
like global objects.

## Bugs that I am aware of

None (with my mod)

I realise the config screen inside JourneyMap is very messy. JourneyMap is supposed to have categories so it looks much cleaner, but currently they are broken.
When they are fixed, in JourneyMap, so will the config screen in my mod (E.g. I won't have to release a bug fix)


### Other
*Server version is 1.1, massive server changes from 1.0. Update server and client immediately.*