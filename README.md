# CS354R Bullet Hell RPG

A bullet hell action RPG with a pretty awesome replication framework.

[![Build Status](http://ci.idolagames.com/buildStatus/icon?job=cs354r-rpg)](http://ci.idolagames.com/job/cs354r-rpg/)

## Build instructions

1. Install JDK 8
2. Set up your PATH to use JDK 8 (lab machine note: `export JAVA_HOME=/lusr/opt/jdk1.8`)
3. `./gradlew build test`
4. `./gradlew :desktop:run` -- this will connect to the testing server.

## Project structure

* **`core`** -- Core game code. Includes client code, replication framework, and most of the unit tests.
* **`desktop`** -- Desktop launcher code.
* **`server`** -- Headless server code. The server is separated out so the client isn't distributed with it.
* **`annotationProcessors`** -- dependency of `core`, used for appending replication information to methods and fields.

## Using an IDE

The `eclipse` and `idea` plugins are enabled on all projects, so simply call:

```
./gradlew idea
./gradlew eclipse
```

to generate the project files for them.

## Committing

Do not push to master. Do not commit class files or project metadata (the .gitignore will already handle this for you.)
Use topic branches to work on individual issues until they're ready to commit.

Before committing, it's recommended that you run the unit tests by starting the `test` gradle task. This will bail out
if the code itself won't compile, and will tell you what tests are currently failing.

## Continuous integration

The master branch is automatically pulled by a continuous integration server to handle builds and to start up an
instance of the game server. If you start the `:desktop:run` task in gradle, the client will start and automatically
connect to the game server.
