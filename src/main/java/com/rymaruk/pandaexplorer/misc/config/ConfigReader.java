package com.rymaruk.pandaexplorer.misc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rymaruk.pandaexplorer.misc.config.model.ConfigEntity;
import com.rymaruk.pandaexplorer.misc.config.model.ConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigReader {

    private final static Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private final static ConfigEntity configEntity = readConfigFile();

    public static ConfigEntity getConfig() {
        return configEntity;
    }

    public static ConnectionConfig getConnectionConfig(String env) {
        return configEntity.getEnvs().get(env);
    }

    public static Set<String> getAllEnvNames() {
        return configEntity.getEnvs().keySet();
    }

    public static String getSuffix(String env) {
        return getConnectionConfig(env).getSuffix();
    }

    public static Set<String> getAllDbSuffix() {
        return configEntity.getEnvs().values().stream()
                .map(ConnectionConfig::getSuffix)
                .collect(Collectors.toSet());
    }


    private static ConfigEntity readConfigFile() {
        String fileName = "./config/config.yaml";
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(new File(fileName), ConfigEntity.class);
        } catch (IOException e) {
            log.error("File {} not found", fileName, e);
            throw new IllegalArgumentException(e);
        }
    }

}
