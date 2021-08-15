package com.rymaruk.pandaexplorer.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SqlResultTO {

    private final String sql;
    private String db;
    private String response;
    private boolean isSuccess = true;
    private String executedOn;

    public SqlResultTO(String sql) {
        this.sql = sql;
    }

    public SqlResultTO(String sql, String db, String response, boolean isSuccess) {
        this.sql = sql;
        this.db = db;
        this.response = response;
        this.isSuccess = isSuccess;
        this.executedOn = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace("T", " ");
    }

    public String getFormattedString() {
        return String.format("Executed: %s\nDB: %s\nSQL: %s \n\nResult:\n%s", executedOn, db, sql, response);
    }

    public String getSql() {
        return sql;
    }

    public String getDb() {
        return db;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
