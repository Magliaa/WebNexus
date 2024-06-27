package it.unimib.sd2024.operations;

import com.google.gson.JsonObject;
import it.unimib.sd2024.CommandException;

import java.util.List;

public class Common {

    public static JsonObject getJsonObjFromPath(List<String> keys, JsonObject jsonObject) throws CommandException {
        if (jsonObject == null)
            throw new CommandException("Key " + keys.getFirst() + " not found");
        for (int i = 0; i < keys.size() - 1; i++) {
            var key = keys.get(i);

            if (!jsonObject.has(key))
                throw new CommandException("Key " + key + " not found");

            var value = jsonObject.get(key);
            if (!value.isJsonObject())
                throw new CommandException("Key " + key + " does not represent a valid path");

            jsonObject = jsonObject.getAsJsonObject(key);
        }

        return jsonObject;
    }
}
