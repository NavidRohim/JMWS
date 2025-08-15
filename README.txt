beta 5 bug fixes (from beta 3)

Beta 3 bugs

- ~~when editing a waypoint and enable it in another dimension, it creates a duplicate waypoint
   -> this may be due to the fact that waypoint identifiers on the server side use the primary dimensions ID to differentiate waypoints~~ (fixed in beta 4)
- ~~when editing a waypoint and disable usage in all dimensions, the game crashes on the client side due to a server side packet error (exc traceback listed below)~~ (fixed in beta 4)
- ~~when making a waypoint available in the overworld and the nether at the same time, the location will become "corrupted" (overworld location will shift 8 fold in the x and z axis due to nether coordinate calculation)
      -> This bug I believe occurs when making the displayable waypoint when syncing, a method called Waypoint.getPosition() is used which is made into a BlockPos, plus, getting the primaryDimension accurately is impossible as a record of it is never kept.
      If primaryDimension is -1 which is the nether, the coordinates will be nether dimensions, not be translated per dimension. For example this is why the overworld coordinate is increased 8 fold~~

Beta 4 bug fixes

In respective order of the bug list:
 - Fixed by clearing by clearing WaypointStore during waypoint sync (WaypointStore.INSTANCE.reset())
Happened because if you disabled usage of all dimensions in a waypoint, the list of dimensioms that the waypoint operated in would be empty. On the server side, the first dimension ID is used as an identifier for the waypoints file. An index error would be caused if the list is empty (trying to access the first dimensions ID in the list, I.E. JsonArray.get(0) ) Fix > check if the list is empty, if it is, assign a default ID of 999 which has no effect on the waypoint when synced to the client
This bug occured to due the fact a waypoint displayable object was created then added to jmAPI.show() and during this, the waypoints coordinates are messed up internally. This is because the displayable object was originally an internal waypoint object then a displayable was made. The solution was to use the internal object directly and add it via WaypointStore.save() instead (which is a lower level method and not part of the public API)

Beta 5 -- Fixes bugs

- Fixed bug where death waypoints would not be deleted via the "Auto remove death waypoints" option
- Fixed bug where death waypoints would not appear until the next sync after death
- Fixed bug where the game would crash after creating a corrupted waypoint (attempting to delete it would result in a crash and softlock the world until deleting the waypoint server side)