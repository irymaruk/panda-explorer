package com.rymaruk.pandaexplorer.task;

import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;
import com.rymaruk.pandaexplorer.misc.CommonUtils;
import com.rymaruk.pandaexplorer.misc.CosmosDbUtils;
import com.rymaruk.pandaexplorer.misc.config.ConfigReader;
import com.rymaruk.pandaexplorer.model.CollectionTO;
import com.rymaruk.pandaexplorer.model.SqlResultTO;
import com.rymaruk.pandaexplorer.service.CosmosCollectionService;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class SqlExecutionTask extends Task<SqlResultTO> {

    private final String env;
    private final String rawSql;
    private final Logger log = LoggerFactory.getLogger(SqlExecutionTask.class);
    private BooleanProperty fetchAll;
    private String parsedSql;
    private String db = "";
    private String collName;


    public SqlExecutionTask(String env, String rawSql, BooleanProperty fetchAll) {
        this.env = env;
        this.rawSql = rawSql;
        this.fetchAll = fetchAll;
    }

    @Override
    protected SqlResultTO call() {
        try {
            long startTime = System.nanoTime();
            updateMessage("Establishing connection");
            log.info("Establishing connection");
            CosmosDbUtils provider = CosmosDbUtils.getInstance();
            parsedSql = parseSql(rawSql, provider);
            updateMessage("Query execution....");
            CosmosPagedIterable<JsonNode> response = provider.queryDocuments(env, parsedSql);
            ArrayList<JsonNode> documents = getRecords(response);
            long startTime2 = System.nanoTime();
            log.debug("queryDocuments time mls: {}", (startTime2 - startTime) / 1_000_000);
            CosmosDbUtils.getInstance().removeNotDataFields(documents);
            String formattedResponse = CommonUtils.convertToJsonString(documents);
            log.debug("convertToJsonString time mls: {}", (System.nanoTime() - startTime2) / 1_000_000);
            return new SqlResultTO(parsedSql, db, formattedResponse, true);
        } catch (Exception e) {
            updateMessage("Exception occurred...");
            e.printStackTrace();
            String sql = parsedSql == null ? rawSql : parsedSql;
            return new SqlResultTO(sql, db, e.getMessage(), false);
        }
    }

    // TODO: get total number of elements in response
    private ArrayList<JsonNode> getRecords(CosmosPagedIterable<JsonNode> response) {
        int documentSizeLimit = 100;
        ArrayList<JsonNode> documents = new ArrayList<>();
        Iterator<JsonNode> iterator = response.iterator();
        if (fetchAll.getValue()) {
            while (iterator.hasNext() && fetchAll.getValue()) {
                documents.add(iterator.next());
            }
            int size = documents.size();
            String count = fetchAll.getValue() ? "" + size : size + " out of ???";
            updateMessage("Record count: " + count);
        } else {
            while (documents.size() < documentSizeLimit && iterator.hasNext()) {
                documents.add(iterator.next());
            }
            int size = documents.size();
            String count = size == documentSizeLimit ? size + " out of ???" : "" + size;
            updateMessage("Record count: " + count);
        }
        return documents;
    }

    /**
     * Preserve cases of collection name
     * TODO: try to simplify using regexp
     *
     * @param rawSql
     * @return
     */
    private String parseSql(String rawSql, CosmosDbUtils provider) {
        log.info("Parsing rawSql: " + rawSql);
        String[] s = rawSql.split("\\s+");
        String wordAfterFrom = "";
        for (int i = 0; i < s.length; i++) {
            if (s[i].toLowerCase().equals("from")) {
                wordAfterFrom = s[i + 1];
                break;
            }
        }
        if (wordAfterFrom.isEmpty()) {
            throw new IllegalArgumentException("Incorrect SQL, there is no FROM keyword: " + rawSql);
        }
        String[] split = wordAfterFrom.split("\\.");
        if (split.length > 1) {
            db = split[0];
            collName = split[1];
        } else {
            collName = split[0];
        }
        if (db.isEmpty()) {
            CollectionTO collection = CosmosCollectionService.getCollection(collName);
            if (!collection.isUnique()) {
                throw new IllegalArgumentException("Please specify db name. Possible values are:\n" + collection.getAllDatabases().toString());
            } else {
                db = collection.getDatabaseName();
            }
        }
        log.info("DatabaseId = {}, collectionId = {}", db, collName);
        String newCollName = removeHyphenInCollectionName();
        parsedSql = StringUtils.remove(rawSql, db + ".");
        parsedSql = StringUtils.removeEnd(parsedSql, ";");
        parsedSql = parsedSql.replaceFirst("^\\W+", "");
        parsedSql = parsedSql.replace(collName, newCollName);
        log.info("Parsed SQL = {}", parsedSql);
        db = substituteEnvInDbId(db);
        checkCollectionExistInDb(db);
        provider.setDatabaseId(db);
        provider.setCollectionId(collName);
        return parsedSql;
    }

    private void checkCollectionExistInDb(String db) {
        if (CosmosCollectionService.isDbNameExist(db)) {
            throw new IllegalArgumentException("Exception: DB name doesn't exist in current environment \nDatabaseId = " + db);
        }
    }

    private String removeHyphenInCollectionName() {
        return collName.replaceAll("-", "");
    }

    private String substituteEnvInDbId(String db) {
        String newSuffix = ConfigReader.getSuffix(env);
        if (newSuffix == null) {
            return db;
        }
        Set<String> oldSuffixes = ConfigReader.getAllDbSuffix();
        for (String oldSuffix : oldSuffixes) {
            db = StringUtils.replaceOnce(db, oldSuffix, newSuffix);
        }
        log.info("Updating DB name with suffix = '{}'. New DB name = {}", newSuffix, db);
        return db;
    }
}
