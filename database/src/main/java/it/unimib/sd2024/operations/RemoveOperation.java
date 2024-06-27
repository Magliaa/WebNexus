package it.unimib.sd2024.operations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import it.unimib.sd2024.CommandException;

import java.io.*;
import java.util.List;

import static it.unimib.sd2024.operations.Common.getJsonObjFromPath;

public class RemoveOperation extends AbstractDatabaseOperation {

    public RemoveOperation(List<String> keys) {
        super(keys);
    }

    @Override
    public JsonElement execute(File file) throws CommandException, IOException {
        var remove = false;
        JsonObject jsonObject;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            var string = br.lines().reduce("", String::concat);
            if (!string.isBlank()) {
                jsonObject = JsonParser.parseString(string).getAsJsonObject();
                var temp = getJsonObjFromPath(keys, jsonObject);

                var key = keys.getLast();
                if (!temp.has(key))
                    throw new CommandException("Key " + key + " not found");
                temp.remove(key);

                if (jsonObject.isEmpty())
                    remove = true;
            } else
                throw new CommandException("Key " + keys.getFirst() + " not found");
        }

        if (remove)
            file.delete();
        else
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(jsonObject.toString());
            }

        return new JsonPrimitive(true);
    }
}
