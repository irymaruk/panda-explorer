# Description

**Panda Explorer** is a JavaFX 8 application for SQL queries in Azure Cosmos DB.

### Key features:

- query data from the different collection/databases from the same window;
- select environment from the dropdown list and execute the same script in a new context;
- all script changes are saved automatically and will be restored with the next application start;
- collection name live search;
- json and sql syntax highlighting;
- conversion unix timestamp (milliseconds) to human-readable local/UTC date-time format;
- copy json result to csv/excel file;
- by default fetch first 100 documents from result set (performance optimization);
- key binding (see program help for details);
- execute only selected part of sql or automatic script boundary detection based on caret position;

For a quick start, make a double-click on the collection name to generate an SQL statement. Panda Explorer will find a
database name automatically based on your query. When the database name is ambiguous (same collection name used in different
databases within the same namespace), then you will get a suggestion about which db name you can use. Append database name to the collection name
with a dot. Like `select * from dbname.collection`

_Note_: Currently, supports only select statement

### Requirements

Oracle JRE/JDK 8 (required for JavaFx 8)

# Build and Test

1. `mvn clean package`
2. Copy _panda-jar-with-dependencies.jar_ and _/config_ folder into one folder.
3. Update _uir_ and _key_ in _config/config.yaml_
4. Start application `java -jar panda-jar-with-dependencies.jar`

# Overview

![](https://github.com/irymaruk/panda-explorer/blob/master/src/test/resources/PandaExplorer_074_overview.gif)
