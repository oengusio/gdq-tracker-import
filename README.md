# GDQ tracker importer
Import your Oengus schedule into the GDQ tracker!

***DO NOT USE THIS TOOL TO CONTINUOUSLY SYNC YOUR SCHEDULE***

Stuff will seriously break when you do that, especially when you added incentives to the tracker.

## Requirements
- Java 21 (or newer)
  - Download here: https://www.azul.com/downloads/?version=java-21-lts&package=jre#zulu
- Knowledge how to operate the command line
- A basic understanding of URL structures
- (optional but preferred) the [BSG version](https://github.com/BSGmarathon/donation-tracker-toplevel) of the donation tracker

## Installation
1. Install java and add it to your PATH
2. Download `gdq-tracker-import-<version>-all.jar` from the releases
3. Open a command prompt/terminal in the folder where you downloaded the jar
4. Run `java -jar gdq-tracker-import-<version>-all.jar` and follow the instructions on screen

## Running locally
(welcome to hell) `./gradlew build ;; java -jar build/libs/gdq-tracker-import-1.0-SNAPSHOT-all.jar`
