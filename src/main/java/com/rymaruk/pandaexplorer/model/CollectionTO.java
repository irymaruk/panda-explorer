package com.rymaruk.pandaexplorer.model;

import java.util.ArrayList;
import java.util.List;

public class CollectionTO {

    private final String name;
    private final List<String> allDatabases = new ArrayList<>();

    public CollectionTO(String name, String dbName) {
        this.name = name;
        this.allDatabases.add(dbName);
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return allDatabases.size() <= 1;
    }

    public String getDatabaseName() {
        return allDatabases.get(0);
    }

    public List<String> getAllDatabases() {
        return allDatabases;
    }

    public void addAltDatabaseName(String databaseName) {
        allDatabases.add(databaseName);
    }
}
