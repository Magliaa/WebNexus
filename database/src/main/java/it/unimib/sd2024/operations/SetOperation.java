package it.unimib.sd2024.operations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.*;
import java.util.List;

/**
 * SetOperation class to handle the Set operation.
 */
public class SetOperation extends AbstractDatabaseOperation {
    JsonElement document;

    public SetOperation(List<String> keys, JsonElement document) {
        super(keys);
        this.document = document;
    }

    @Override
    public JsonElement execute(File file) throws IOException, IllegalStateException {
        JsonObject jsonObject;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            var string = br.lines().reduce("", String::concat);
            if (!string.isBlank()) {
                jsonObject = JsonParser.parseString(string).getAsJsonObject();
            } else {
                jsonObject = new JsonObject();
            }

            var temp = jsonObject;
            for (int i = 0; i < keys.size() - 1; i++) {
                var key = keys.get(i);
                if (!temp.has(key)) {
                    temp.add(key, new JsonObject());
                }
                var value = temp.get(key);
                if (!value.isJsonObject()) {
                    throw new IllegalStateException("Key " + key + " does not represent a valid path");
                }
                temp = temp.getAsJsonObject(key);
            }

            var key = keys.getLast();
            temp.add(key, document);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(jsonObject.toString());
        }

        return new JsonPrimitive(true);
    }
}
