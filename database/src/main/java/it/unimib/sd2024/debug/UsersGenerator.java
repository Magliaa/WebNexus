package it.unimib.sd2024.debug;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UsersGenerator {
    public static Map<String, JsonObject> users = new HashMap<>();

    static {
        String content;
        try {
            content = Files.readString(Paths.get("database/src/main/java/it/unimib/sd2024/debug/Users"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Parse the content to a JsonArray
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

        // Iterate over the JsonArray
        for (var entry : jsonObject.entrySet()) {
            // Get the domain name and the associated data
            String domainName = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();
            users.put(domainName, data);
        }
    }
}
