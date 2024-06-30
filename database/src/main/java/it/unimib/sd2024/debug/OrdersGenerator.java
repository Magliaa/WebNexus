package it.unimib.sd2024.debug;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class OrdersGenerator {
    public static Map<String, JsonObject> orders = new HashMap<>();

    static {
        String content = Orders.orders;

        // Parse the content to a JsonArray
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

        // Iterate over the JsonArray
        for (var entry : jsonObject.entrySet()) {
            // Get the domain name and the associated data
            String domainName = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();
            orders.put(domainName, data);
        }
    }
}
