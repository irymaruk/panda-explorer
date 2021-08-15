package com.rymaruk.pandaexplorer.misc;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rymaruk.pandaexplorer.misc.config.ConfigReader;
import com.rymaruk.pandaexplorer.misc.config.model.ConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CosmosDbUtils {

    private static final Map<String, CosmosClient> clientMap = new HashMap<>();
    private static CosmosDbUtils instance;
    private final CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
    private final Logger log = LoggerFactory.getLogger(CosmosDbUtils.class);
    private String databaseId;
    private String collectionId;


    private CosmosDbUtils() {
        queryRequestOptions.setQueryMetricsEnabled(false);
    }

    public static CosmosDbUtils getInstance() {
        if (instance == null) {
            instance = new CosmosDbUtils();
        }
        return instance;
    }

    public ArrayList<JsonNode> removeNotDataFields(ArrayList<JsonNode> documents) {
        if (ConfigReader.getConfig().getHideMetadata()) {
            for (JsonNode document : documents) {
                if (document.isObject()) {
                    ((ObjectNode) document).remove(Arrays.asList("_rid", "_self", "_etag", "_attachments", "_ts"));
                }
            }
        }
        return documents;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public CosmosClient getClient(String env) {
        CosmosClient cosmosClient = clientMap.get(env);
        if (cosmosClient == null) {
            ConnectionConfig connectionConfig = ConfigReader.getConnectionConfig(env);
            cosmosClient = new CosmosClientBuilder()
                    .endpoint(connectionConfig.getUri())
                    .key(connectionConfig.getKey())
                    .consistencyLevel(ConsistencyLevel.SESSION)
                    .directMode()
                    .buildClient();
            clientMap.put(env, cosmosClient);
        }
        return cosmosClient;
    }


    public CosmosPagedIterable<JsonNode> queryDocuments(String env, String query) {
        if (databaseId == null || collectionId == null)
            throw new IllegalArgumentException(String.format("Both databaseId=%s and collectionId=%s should be defined", databaseId, collectionId));
        return getClient(env).getDatabase(databaseId)
                .getContainer(collectionId)
                .queryItems(query, queryRequestOptions, JsonNode.class);
    }
}