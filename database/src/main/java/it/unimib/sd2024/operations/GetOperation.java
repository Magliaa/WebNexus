package it.unimib.sd2024.operations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimib.sd2024.CommandException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static it.unimib.sd2024.operations.Common.getJsonObjFromPath;

public class GetOperation extends AbstractDatabaseOperation {

    public GetOperation(List<String> keys) {
        super(keys);
    }

    @Override
    public JsonElement execute(File file) throws IOException, CommandException {
        JsonObject jsonObject = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            var string = br.lines().reduce("", String::concat);
            if (!string.isBlank()) {
                jsonObject = JsonParser.parseString(string).getAsJsonObject();
            }
        }
        var temp = getJsonObjFromPath(keys, jsonObject);

        var key = keys.getLast();
        if (!temp.has(key))
            throw new CommandException("Key " + key + " not found");
        return temp.get(key);
    }
}
