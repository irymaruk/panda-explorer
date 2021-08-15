package com.rymaruk.pandaexplorer.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConvertJsonToCsv {

    private static final Logger log = LoggerFactory.getLogger(ConvertJsonToCsv.class);
    private static final String separator = "\t";
    private static final String newLine = "\n";
    private static final ObjectMapper mapper = CommonUtils.getObjectMapper();


    public static String convert(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            ArrayNode docs = mapper.readValue(jsonStr, ArrayNode.class);
            ArrayList<Map<String, Object>> items = new ArrayList<>();
            docs.forEach(doc -> items.add(JsonFlattener.flattenAsMap(doc.toString())));
            Set<String> allKeys = getAllUniqueKeys(items);
            allKeys.forEach(key -> sb.append(key).append(separator));
            sb.append(newLine);
            for (Map<String, Object> item : items) {
                Set<String> itemKeys = item.keySet();
                for (String key : allKeys) {
                    if (itemKeys.contains(key)) {
                        sb.append(item.get(key));
                    }
                    sb.append(separator);
                }
                sb.append(newLine);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return sb.toString();
    }


    private static Set<String> getAllUniqueKeys(ArrayList<Map<String, Object>> items) {
        Set<String> allKeys = items.stream()
                .flatMap(i -> i.keySet().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        log.info("uniqueKeys = " + allKeys);
        return allKeys;
    }
}