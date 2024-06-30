package it.unimib.sd2024.operations;

import com.google.gson.*;
import it.unimib.sd2024.CommandException;

import java.io.*;
import java.util.List;

import static it.unimib.sd2024.operations.Common.getJsonObjFromPath;

/**
 * GetIfOperation class to handle the GetIf operation.
 */
public class GetIfOperation extends AbstractDatabaseOperation {
    public final String key_check;
    public final JsonElement value_check;

    public GetIfOperation(List<String> keys, String key_check, JsonElement value_check) {
        super(keys);
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
    public JsonObject execute(File file) throws IOException, IllegalStateException {
        var result = new JsonObject();
        var jsonObject = new JsonObject();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            var string = br.lines().reduce("", String::concat);

            if (string.isBlank())
                throw new CommandException("Key " + keys.getFirst() + " not found");

            jsonObject = JsonParser.parseString(string).getAsJsonObject();

            var temp = getJsonObjFromPath(keys, jsonObject);

            var key = keys.getLast();
            if (temp.has(key)) {
                if (temp.get(key).isJsonObject())
                    for (var entry : temp.get(key).getAsJsonObject().entrySet()) {
                        if (key_check.equals(entry.getKey()) && entry.getValue().equals(value_check) || checkNestedObjects(entry.getValue())) {
                            result.add(entry.getKey(), entry.getValue());
                        }
                    }
            } else
                throw new CommandException("Key " + key + " not found");
        }

        return result;
    }
}
