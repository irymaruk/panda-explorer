# Description

**Panda Explorer** is a Java-based UI application (JavaFX 8) that utilizes Microsoft java SDK for Azure Cosmos DB.

### Key features:

- ability to query data from the different collection and/or databases from the same window;
- easy switch between environments - choose environment from the dropdown and execute the same script;
- all changes to scripts are saved automatically and will be loaded with the next start;
- live collection/database search;
- json and sql syntax highlighting;
- convert unix timestamp in milliseconds to human-readable local or UTC date-time;
- copy json result to csv/excel file;

For a quick start, make a double click on the collection name to generate an SQL statement. Panda Explorer will find a
database name automatically based on your query. When the database name is ambiguous (same collection name in different
databases), then you will get a suggestion about which db name you can use. Append database name to the collection name
with a dot.

_Note_: Currently, supports only select statement

### Requirements

Oracle JRE/JDK 8 (required for JavaFx 8)

# Build and Test

1. Update uir and key in _config/config.yaml_
2. `mvn clean package`
3. Put _panda-jar-with-dependencies.jar_ (from _target_) and _/config_ under same folder
4. Start application `java -jar panda-jar-with-dependencies.jar`

# Overview

![](https://github.com/irymaruk/panda-explorer/blob/master/src/test/resources/PandaExplorer_074_overview.gif)
