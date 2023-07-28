# Debuggable FUMBBL Client Downloader

Commandline tool for downloading all the files needed to run the FUMBBL Game 
Client and inject debug code into it.

# How to use 



See https://fumbbl.com/help:Test+mode for how to run a test game. Starting the debug client
from the commandline will look something like this:

````
> java -cp "FantasyFootballClient.jar:*" com.fumbbl.ffb.client.FantasyFootballClient -player \ 
  -teamId 72961 -teamName "Greendale Ressurected" -port 22223 \
  -coach cmelchior -auth 85c88af58bc846e5ee67d9a87f86a2ec
````

The values can be extracted from the JNLP file that is downloaded when pressing "Play" on FUMBBL.


# Source code

The FUMBBL Client source code is not public, but can be recreated from the downloaded JAR files.
This makes it possible to e.g. use IntelliJs debugger and debug step through the code.

This can be accomplished by using the following workflow:

1. 




## How to use

1. Run the script to download all JAR files
2. Use JD-GUI to decompile FantasyFootballClient.jar
3. Move the source code to a module and put it into the main source set.
4. Start the client using the debug line from the download.
5. Open IntelliJ, create a Remote JVM debug configuration, attach the source code and run it
6. You can now debug the client with source. Beware that the port number must match on both ends.