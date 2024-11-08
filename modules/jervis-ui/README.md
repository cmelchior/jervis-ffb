# Game Model

This module contains the Jervis Game UI.


## Gothas

- kotlin.uuid does not work out of the box on `localhost` as it relies on https://developer.mozilla.org/en-US/docs/Web/API/Crypto/randomUUID



## Creating releases

<TODO>
https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md

Running locally
e.g.: 0.1.0.dev.local

Maven snapshots: 0.1.0-SNAPSHOT

// Push to dev-releases, every commit:
Tag commit with : v0.1.0.dev.<hash>
create release

version.txt + ".dev." + git hash
e.g.: 0.1.0.dev.2294ad4

Uploaded Wasm to http://dev.jervisffb.ilios.dk
Uploaded files to http://dev.jervisffb.ilios.dk/downloads 

// Push to prod-releases, every commit
Check if tag already exists: v0.1.0
If it does, fail release 

Otherwise build with 
version.txt as version number

Push Wasm to http://jervisffb.ilios.dk
Create GitHub release
Push to Github release

## Test production build 

For Wasm:

```
./gradlew wasmJsBrowserDistribution
cd modules/jervis-ui/build/dist/wasmJs/productionExecutable
python3 -m http.server
```

Open a web browser at: `localhost:8000`

