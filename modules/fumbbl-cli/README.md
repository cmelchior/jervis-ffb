# FUMBBL CLI

The FUMBBL CLI tool exposes some helper commands for interacting with FUMBBL in order to more easily access and
work with the FUMBBL game data.

## Usage

First build the fumbblcli.jar using Gradle: `./gradlew buildTools`. This will build the CLI and place the 
`fumbblcli.jar` file in the `<root>/tools` folder. 

Call the JAR file using either be called directly or using the helper script that is placed at the root of this 
project. `./fumbbl-cli <commands>`

```
Usage: fumbbl-cli [<options>] <command> [<args>]...

Options:
-h, --help  Show this message and exit

Commands:
prepare-debug-client  Downloads and inject debug code into the FUMBBL client.
download-game         Download all the websocket data associated with a given game on FUMBBL. DO NOT use
                      this to download bulk games as it stresses the game server.

------
Usage: fumbbl-cli prepare-debug-client [<options>]

  Downloads and inject debug code into the FUMBBL client.

Options:
  --output=<value>  Optional path where JAR files should be saved
  -h, --help        Show this message and exit

------
Usage: fumbbl-cli download-game [<options>]

  Download all the websocket data associated with a given game on FUMBBL. DO NOT use this to download bulk games as it stresses the game server,

Options:
  --game-id=<text>  ID of the game to download
  -h, --help        Show this message and exit
```

## Debuggable FUMBBL Client

The debuggable FUMBBL Client is a game client that has been modified to save the JSON in- and output of the websocket
connection as well as print it to the console. Otherwise, the game client is completely identical to the normal 
FUMBBL client.

The modified client should only be used for testing and not when playing real games.

### Play a test game

Playing a test game, requires a valid login on FUMBBL. See https://fumbbl.com/help:Test+mode for how to 
run a test game. Then goto the directory containing the debug client and start it with this:

````
> java -cp "FantasyFootballClient.jar:*" com.fumbbl.ffb.client.FantasyFootballClient -player \ 
  -teamId <teamId> -teamName "<teamName>" -port 22223 \
  -coach <coach> -auth <authToken>
````
The values can be extracted from the JNLP file that is downloaded when pressing "Play" on FUMBBL but will 
look something like this:

```
teamId: 72961
teamName: "Greendale Ressurected"
coach: cmelchior
authToken: 85c88af58bc846e5ee67d9a87f86a2ec


</argument><argument>-teamId</argument><argument>1158751</argument><argument>-teamName</argument><argument>Jervis Cybernetic Crusaders</argument><argument>-port</argument><argument>22223</argument><argument>-coach</argument><argument>cmelchior</argument><argument>-auth</argument><argument>037194e7b7ac73bd6b46222646f5056f</argument></application-desc></jnlp>


> java -cp "FantasyFootballClient.jar:*" com.fumbbl.ffb.client.FantasyFootballClient -player \
-teamId 1158751 -teamName "Team 1" -port 22223 \
-coach cmelchior -auth 037194e7b7ac73bd6b46222646f5056f

> java -cp "FantasyFootballClient.jar:*" com.fumbbl.ffb.client.FantasyFootballClient -player \
-teamId 1158756 -teamName "Team 2" -port 22223 \
-coach cmelchior -auth 037194e7b7ac73bd6b46222646f5056f


````


```

Note, it is possible for the same coach to connect two of their own teams, giving full control over the
progress of the game to same coach. This can be helpful if you want to reproduce certain game situations.

### Replay a game

Replaying games can be done without logging in to FUMBBL, all you need to know is the game id. To start a 
replay, using the following command:

```
java -cp FantasyFootballClient.jar:* com.fumbbl.ffb.client.FantasyFootballClient -replay -gameId <gameId>
```

### Debug the Client while it is running

In order to most effectively debug the client while it is running, you need to extract the source code 
from the JARs, and place them in `<root>/Debug-FantasyFootballClient/src/main/java`. It can be done 
the following way:

1. Run the script to download all JAR files:
2. Use JD-GUI to decompile FantasyFootballClient.jar.
3. Move the source code to `<root>/Debug-FantasyFootballClient/src/main/java`.
4. Start the client using the follow command: 
   
   ```
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000 \
     -cp FantasyFootballClient.jar:* com.fumbbl.ffb.client.FantasyFootballClient \
     -replay -gameId <gameId>
   ```

   This will start the JAR, but will wait until a Java Debug Agent is connected on port 8000.

5. Open IntelliJ, create a Remote JVM debug configuration, attach the source code and run it.

6. You can now debug the client with source, including setting break points. Beware that the port 
   number must match (8000 as the default) for the debugger to attach correctly.

------

## FUMBBL Game Downloader

It is possible to use the CLI to download the full websocket traffic for any game using the following
command:

```
./fumbbl-cli download-game --gameId <gameId>
```

This will download all the websocket traffic and store it as a JSON array in a file name `game-<gameId>.json`

### Warning

It is not recommended to use this tool to bulk download games from FUMBBL. The server is not
designed with this use case in mind, and you might impact the service. Be nice to Christer.