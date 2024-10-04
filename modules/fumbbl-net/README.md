# FUMBBL NET

This module contains the infrastructure to work with FUMBBL. This includes:

* Read team data and convert it into a Jervis compatible team.
* Parse replay files and convert them into Jervis equivalent actions, so the file can be 
  replayed inside Jervis.
* Expose an adapter that makes it possible for a Jervis game to play against a FUMBBL team
  using the FUMBBL server.


## Tips for using the FUMBBL API

- See match details: https://fumbbl.com/p/match?id=<match_id>

- REST API: An OAuth id can be created at https://fumbbl.com/p/oauth.
  It requires you to login at FUMBBL first. With The credentials
  there, you can call the API described in https://fumbbl.com/apidoc/
