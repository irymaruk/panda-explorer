import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.rymaruk.pandaexplorer.misc.ConvertJsonToCsv;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Json2csvTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String RESOURCE_BASE_PATH = "src/test/resources/data/json";
    private final String separator = "\t";
    private final String newLine = "\n";


    @Test
    public void testInitialImplementation() throws JsonProcessingException {
        ArrayNode docs = getDocuments("sample.json");
        HashSet<String> uniqueKeys = new HashSet<>();
        ArrayList<Map<String, Object>> items = new ArrayList<>();
        for (JsonNode doc : docs) {
            items.add(JsonFlattener.flattenAsMap(doc.toString()));
        }
        for (Map<String, Object> item : items) {
            uniqueKeys.addAll(item.keySet());
        }
        List<String> allKeys = uniqueKeys.stream().sorted().collect(Collectors.toList());
        System.out.println("uniqueKeys =" + allKeys);
        StringBuilder sb = new StringBuilder();
        for (String key : allKeys) {
            sb.append(key + separator);
        }
        sb.append(newLine);
        for (Map<String, Object> item : items) {
            List<String> itemKeys = item.keySet().stream().sorted().collect(Collectors.toList());
            for (String key : allKeys) {
                if (itemKeys.contains(key)) {
                    sb.append(item.get(key));
                }
                sb.append(separator);
            }
            sb.append(newLine);
        }
        System.out.println(sb);
    }

    @Test
    void testAppImplementation() {
        String convertedStr = ConvertJsonToCsv.convert(readFileContent("sample.json"));
        System.out.println(convertedStr);
    }

    private ArrayNode getDocuments(String fileName) throws JsonProcessingException {
        return mapper.readValue(readFileContent(fileName), ArrayNode.class);
    }

    private String readFileContent(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(RESOURCE_BASE_PATH, fileName)));
        } catch (IOException e) {
            return "Can not find " + fileName;
        }
    }
}
