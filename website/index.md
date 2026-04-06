# Jervis Fantasy Football

<video autoplay muted playsinline style="width: 100%; border-radius: 8px; margin-bottom: 1rem; cursor: pointer;" onclick="this.currentTime=0; this.play();">
  <source src="https://jervis.ilios.dk/jervis_intro.mp4" type="video/mp4">
</video>

Jervis Fantasy Football is a new free game client for the game of [Blood Bowl](https://start-warhammer.com/blood-bowl/),
a board game owned by Games Workshop. 

It is not intended as a replacement for neither [FUMBBL](https://fumbbl.com/) nor 
[Cyanide Blood Bowl 3](https://cyanide-studio.com/en/cyanide-games/blood-bowl-3/), but exists as an experiment to
develop a better game client and as a platform for developing a proper Blood Bowl AI.

Jervis focuses on the actual game-play part of Blood Bowl and not team building or league 
management. 


## Supported Platforms

===  ":fontawesome-brands-windows: Windows"

    The Windows client is self-signed, so depending on your security settings you might be prevented from opening the app.
    
    If blocked, go to __Settings > Apps > Advanced App Settings__ and set __Choose where to get apps__ to __Anywhere__.

    [Download .exe](https://github.com/cmelchior/jervis-ffb/releases/latest/download/jervis-fantasy-football-dev.exe){ .md-button .md-button--primary }


===  ":fontawesome-brands-apple: macOS"

    The macOS client is signed and notarized by Apple, but is distributed outside the App Store. Depending on your 
    security settings you might be prevented from opening the app.
    
    If blocked, go to __System Settings > Privacy & Security__, scroll to __Security__, and click __Open Anyway__.

    [Download .zip](https://github.com/cmelchior/jervis-ffb/releases/latest/download/jervis-fantasy-football-dev-0.0.0000-mac-aarch64.zip){ .md-button .md-button--primary }

===  ":fontawesome-brands-linux: Linux"

    The Linux client is distributed as a `.deb` package and should work on most Linux distributions without any issues.

    [Download .deb](https://github.com/cmelchior/jervis-ffb/releases/latest/download/christian-melchior-jervis-fantasy-football-dev_0.0.0000_amd64.deb){ .md-button .md-button--primary }

===  ":fontawesome-brands-chrome: Web"

    The web-based client is built using WASM and should work on the latest browser versions of Chrome, Firefox and 
    Safari. See [this page](https://kotlinlang.org/docs/wasm-configuration.html#browser-versions) for more information.

    [Play in the Browser](https://jervis.ilios.dk){ .md-button .md-button--primary }

===  ":fontawesome-brands-app-store-ios: iPad"

    The iPad client is currently not distributed anywhere, and must be built from source. 
    See the [Developer Documentation](https://github.com/cmelchior/jervis-ffb?tab=readme-ov-file#how-to-build) 
    for more information.

===  ":fontawesome-brands-android: Android"

    Android is currently not supported. There is no technical limitation that
    prevents Jervis from running on Android, but it is currently not a 
    priority. If you are interested in this, please add your support in the 
    [issue tracker](https://github.com/cmelchior/jervis-ffb/issues/44).

!!! info "Developer Clients"

    All Game Clients currently released are _Developer Versions_. A new version is released on any change to the
    codebase. No guarantees are given with regard to the stability of the game client or the UI.

    Desktop and Web-based clients will update themselves automatically whenever a new version is released.

## Features

The main features of Jervis are:

* __Scalable Client UI__ that runs on many different platforms.
*  __2025 Ruleset (Season 3)__, including Blood Bowl Sevens.
* __Hotseat__ and __Peer-to-Peer__ play modes.
* __Undo support__. All actions can be undone, even dice rolls if the game is configured to allow it.
* __God Mode__ making it possible to edit player stats and skills during play and fully control all game actions, including dice rolls.
* __AI Player support__. Only a Random Player is currently implemented.
* __Team Import__ from [FUMBBL](https://fumbbl.com) and [TourPlay](https://tourplay.net/).

!!! warning "Under Active Development"
    
    Not all rules and skills are implemented yet. The current state is being tracked
    in a number of files that loosely match their area: 
    [UI](https://github.com/cmelchior/jervis-ffb/blob/main/docs/todo-ui.md), 
    [Base Rules](https://github.com/cmelchior/jervis-ffb/blob/main/docs/bb2025/todo-base-rules-bb2025.md) and 
    [Skills](https://github.com/cmelchior/jervis-ffb/blob/main/docs/bb2025/todo-skills-bb2025.md).

    If you have other ideas for features, please create
    an issue in the [issue tracker](https://github.com/cmelchior/jervis-ffb/issues).

## Disclaimer

Blood Bowl is a trademark of Games Workshop Limited, used without permission,
used without intent to infringe, or in opposition to their copyright.
This project is in no way official and is not endorsed by Games Workshop Limited.
