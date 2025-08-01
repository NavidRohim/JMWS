This branch has been left because I couldnt continue work on it.

There were APIs present in version 2.0 of JourneyMap API that I need for this mod to work, namely;

- `Waypoint.toString()` does not return a Json formatable string (look at JsonStaticHelpers class file at line 49)
- `Waypoint.fromWaypointJsonString()` and `Group.fromGroupJsonString()` do not exist in this version.

There were many other issues as well, but they were fixable. Those two reasons above is why I stopped with the backport.