# Working with the FFB codebase

This document contains information about how to set up the 
[FFB](https://github.com/christerk/ffb) codebase in a way that allows us run it
in "Standalone mode". This way we use a local server for testing and development 
of the `fumbbl-net` module.

The documentation in this document assumes a Mac as developer machine. It has
not been tested on Windows and Linux.


## Requirements

- Homebrew
- Java 21 (FBB just requires Java 8, but Jervis itself requires 21).


## Database

### Installation

FFB needs a MySQL compatible database services running. We are using MariaDB
10.4 (see https://github.com/christerk/ffb?tab=readme-ov-file#server)

Unfortunately, this version has been disabled by Homebrew, but we can still install 
it that way using this guide: 
https://stackoverflow.com/questions/73586208/can-you-install-disabled-homebrew-packages

You need to run `HOMEBREW_NO_INSTALL_FROM_API=1 brew install mariadb@10.4` to 
download the source before following the guide.

Setup MariaDB by:

```
cd /opt/homebrew/opt/mariadb@10.4/scripts
./mysql_install_db
```

Start the database (if you are not running it as a service that starts automatically)

```
cd '/opt/homebrew/Cellar/mariadb@10.4/10.4.34' ; /opt/homebrew/Cellar/mariadb@10.4/10.4.34/bin/mysqld_safe --datadir='/opt/homebrew/var/mysql'
```

### Create server database
Note, the database itself is not created by the FFB `initDb` command, so create this manually

```shell
mysql
CREATE DATABASE ffblocal;
```

### Setup FUMBBL User

This can be done by adding an entry in `com.fumbbl.ffb.server.db.DbInitializer`.

Use any coach name. What you set the password to, depends on how you login. 

But if you are using the login dialog inside the FFB client, you need to store
the MD5-encoded password. You can use https://www.md5hashgenerator.com/ to 
calculate the value.

If you login using the `-auth` client option, you can use any non-empty string,
and just reuse that in the commandline

### Custom Teams and Rosters

In order to make teams available to the coach, there needs
to be a file in `/ffb-server/teams` and a matching roster in `/ffb-server/teams`

Requirements are that the `<coach></coach>` entry matches the coach name. The 
file name does not matter.

You can add new teams by copying the output of 

```
https://fumbbl.com/xml:roster?team=1158751
```

Replace `123456` with the team id found in the URL of the team landing page.

But you need to make sure that `<rosterId></rosterId>` has a matching file in `/ffb-server/rosters`

You can find the roster data using this id:

```shell
curl https://fumbbl.com/xml:roster?team=284314
```

TODO: 
The current implementation of Standalone mode seems to be broken when it comes to
the roster XML format. It looks like the definition of icons has changed.

## Development Environment

- If launching from IntelliJ, set working directory for the server to `ffb-server`, 
  otherwise rosters/teams/setups will not be loaded correctly

- You can start a `test:X` match using the same coach login, but it requires 
  two different teams.




