package com.rymaruk.pandaexplorer.misc.config.model;

import java.util.Map;

public class ConfigEntity {

    private Boolean hideMetadata;
    private Map<String, ConnectionConfig> envs;

    public Boolean getHideMetadata() {
        return hideMetadata;
    }

    public void setHideMetadata(Boolean hideMetadata) {
        this.hideMetadata = hideMetadata;
    }

    public Map<String, ConnectionConfig> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, ConnectionConfig> envs) {
        this.envs = envs;
    }
}
