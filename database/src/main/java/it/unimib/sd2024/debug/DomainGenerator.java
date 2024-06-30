package it.unimib.sd2024.debug;

import com.google.gson.*;

import java.util.HashMap;
import java.util.Map;

public class DomainGenerator {
    static Map<String, JsonObject> domains = new HashMap<>();

    static {
        String content = Domains.domains;

        // Parse the content to a JsonArray
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

        // Iterate over the JsonArray
        for (var entry : jsonObject.entrySet()) {
            // Get the domain name and the associated data
            String domainName = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();
            domains.put(domainName, data);
        }
    }
}
