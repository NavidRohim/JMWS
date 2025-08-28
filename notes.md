# Notes / Observations
- When using internal server, EntityLeaveLevelEvent is fired on the server
- On the client, when leaving a server / world EntityLeaveLevelEvent is not fired for the player. I did not realise this until now. (Possible fix with EntityJoinLevelEvent) for now shouldnt cause issues unless trying to break things