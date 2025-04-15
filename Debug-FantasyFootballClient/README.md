# Debug - FantasyFootballClient

This project is a dummy project that is used to make it easier to debug the 
Fantasy Football Client.

It is only useful in combination with the FUMBBL ClI that can generate a debug client as a JAR:

```
./fumbbl-cli prepare-download-client
```

See [this doc](../modules/fumbbl-cli/README.md) for more information on how to call the 
JAR. It is a direct clone of the FFB client, with the only change that it prints out all
websocket communication to the console.

Note: With the FUMBBL Client now being open source, this module has little use and will probably
go away soon. Instead, clone https://github.com/christerk/ffb and use the information in 
[these docs](../docs/working-with-ffb.md) to run FFB with a standalone server.
