package com.rymaruk.pandaexplorer.service;

import com.rymaruk.pandaexplorer.model.CollectionTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmosCollectionService {

    private static final Map<String, CollectionTO> collMap = new HashMap<>();
    private static Map<String, List<String>> dbMap;

    /**
     * Create flat collection structure from tree
     * tree structure:      dbName -> List<CollectionName>
     * collMap structure:   collectionName -> CollectionTO
     *
     * @param tree - Collection of dbName to list of Collection Names
     */
    public static void init(Map<String, List<String>> tree) {
        collMap.clear();
        dbMap = tree;
        tree.forEach((db, collections) -> {
            collections.forEach(collName -> {
                collMap.compute(collName, (key, prev) -> {
                    if (prev == null) {
                        return new CollectionTO(collName, db);
                    } else {
                        prev.addAltDatabaseName(db);
                        return prev;
                    }
                });
            });
        });
    }

    public static CollectionTO getCollection(String collectionName) {
        CollectionTO collection = collMap.get(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException("There is no Collection with name " + collectionName);
        }
        return collection;
    }

    public static Boolean isCollectionUnique(String collectionName) {
        return getCollection(collectionName).isUnique();
    }

    public static Boolean isDbNameExist(String dbName) {
        return dbMap.get(dbName) == null;
    }
}
