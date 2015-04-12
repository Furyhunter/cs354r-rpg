# CS354R Bullet Hell RPG

A bullet hell action RPG with a pretty awesome replication framework.

## Build instructions

1. Install JDK 8
2. Set up your PATH to use JDK 8 (lab machine note: `export JAVA_HOME=/lusr/opt/jdk8`)
3. `./gradlew build run`

## Project structure

* **`core`** -- Core game code
* **`desktop`** -- Desktop launcher and specific code (we won't support any other platform except maybe HTML5?)
* **`server`** -- Headless server code
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

## Continuous integration

Not currently setup. Ideally when `master` gets pushed to, it will automatically update a testing server on a VPS.
