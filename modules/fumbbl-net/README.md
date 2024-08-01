# FUMBBL NET

This module contains the infrastructure to work with FUMBBL. This includes:

* Read team data and convert it into a Jervis compatible team.
* Parse replay files and convert them into Jervis equivalent actions, so the file can be 
  replayed inside Jervis.
* Expose an adapter that makes it possible for a Jervis game to play against a FUMBBL team
  using the FUMBBL server.
