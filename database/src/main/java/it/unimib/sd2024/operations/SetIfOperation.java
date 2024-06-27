package it.unimib.sd2024.operations;

import com.google.gson.*;
import it.unimib.sd2024.CommandException;

import java.io.*;
import java.util.List;

import static it.unimib.sd2024.operations.Common.getJsonObjFromPath;

public class SetIfOperation extends AbstractDatabaseOperation {
    public final JsonElement newDocument;
    public final String key_check;
    public final JsonElement value_check;

    public SetIfOperation(List<String> keys, JsonElement newDocument, String key_check, JsonElement value_check) {
        super(keys);
        this.newDocument = newDocument;
        this.key_check = key_check;
        this.value_check = value_check;
    }

    private boolean checkNestedObjects(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (var entry : jsonObject.entrySet()) {
                if (key_check.equals(entry.getKey()) && entry.getValue().equals(value_check)) {
                    return true;
                }
                if (checkNestedObjects(entry.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public JsonElement execute(File file) throws IOException, IllegalStateException {
        var jsonObject = new JsonObject();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            var string = br.lines().reduce("", String::concat);

            if (string.isBlank())
                throw new CommandException("Key " + keys.getFirst() + " not found");

            jsonObject = JsonParser.parseString(string).getAsJsonObject();

            var temp = getJsonObjFromPath(keys, jsonObject);

            var key = keys.getLast();
            if (temp.has(key)) {
                if (checkNestedObjects(temp.get(key))) {
                    temp.add(key, newDocument);
                } else
                    throw new CommandException("Key " + key_check + " combined with " + value_check  + " not found");
            } else
                throw new CommandException("Key " + key + " not found");

        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(jsonObject.toString());
        }

        return new JsonPrimitive(true);
    }
}
