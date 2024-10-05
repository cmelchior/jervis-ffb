# Working with FUMBBL

Some notes about connecting to FUMBBL

## Database

Install MariaDB 10.4. This is no longer supported by Brew.
See https://stackoverflow.com/questions/73586208/can-you-install-disabled-homebrew-packages

You need to run `HOMEBREW_NO_INSTALL_FROM_API=1 brew install mariadb@10.4` to 
download the source before following the guide.

Setup MariaDB by:

```
cd /opt/homebrew/opt/mariadb@10.4/scripts
./mysql_install_db
```

Start the database:
```
cd '/opt/homebrew/Cellar/mariadb@10.4/10.4.34' ; /opt/homebrew/Cellar/mariadb@10.4/10.4.34/bin/mysqld_safe --datadir='/opt/homebrew/var/mysql'
```

Database must be created manually:
```shell
CREATE DATABASE ffblocal;
```

1. Add an entry to `com.fumbbl.ffb.server.db.DbInitializer`.

Use any coach name, use `e10adc3949ba59abbe56e057f20f883e` as password (which is equal to "123456")

If launching from IntelliJ, set working directory for the server to `ffb-server`, otherwise
rosters/teams/setups will not be loaded correctly

In order to make teams available to the coach, there needs
to be a file in `/ffb-server/teams`.

Requirements are that the `<coach></coach>` entry matches the coach name. The file name does not matter.


You can add new teams by copying the output of https://fumbbl.com/xml:roster?team=<teamId> e.g.,

```shell
curl https://fumbbl.com/xml:team\?id\=284314
```

But you need to make sure that `<rosterId></rosterId>` has a matching file in `/ffb-server/rosters`

You can find the roster data using this id:

```shell
curl https://fumbbl.com/xml:roster?team=284314
```

You can start a `test:X` match using the same coach, but it requires two different teams.