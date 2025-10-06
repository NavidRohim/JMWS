For people who want to compile JMWS and use it in a dev environment;

if you get a rendering error with neoforge, go to neoforge/run/config/neoforge-client.toml and change `enableB3DValidationLayer = true` to `enableB3DValidationLayer = false`

notes for this branch;

this branch exists because I want features added that I would use but would be inconvenient and not worth implementing to the public release. 
Basically, this is my own branch that has features I want. You can build this branch and have those features but there is a good chance is breaks something with some other mod
or has mixin errors.