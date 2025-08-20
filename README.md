For people who want to compile JMWS and use it in a dev environment;

if you get a rendering error with neoforge, go to neoforge/run/config/neoforge-client.toml and change `enableB3DValidationLayer = true` to `enableB3DValidationLayer = false`

Current bugs in this beta;
- ~~waypoints made while the server has JMWS disabled will not be added to the server once re-enabled.~~