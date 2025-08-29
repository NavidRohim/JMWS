For people who want to compile JMWS and use it in a dev environment;

if you get a rendering error with neoforge, go to neoforge/run/config/neoforge-client.toml and change `enableB3DValidationLayer = true` to `enableB3DValidationLayer = false`

Current bugs in this beta (rc4);
- ~~waypoints made while the server has JMWS disabled will not be added to the server once re-enabled.~~
- ~~Events are fucked, internal server getting client events and shit like that. Will have to fix the problem deeper~~
- fabric internal server events arent working (JMWS doesnt sync when joining, possibly a internal server event issue)