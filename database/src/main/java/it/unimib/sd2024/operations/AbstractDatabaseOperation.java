package it.unimib.sd2024.operations;

import com.google.gson.JsonElement;
import it.unimib.sd2024.CommandException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class AbstractDatabaseOperation {
    public final List<String> keys;

    public AbstractDatabaseOperation(List<String> keys) {
        this.keys = keys;
    }

    public abstract JsonElement execute(File file) throws CommandException, IOException;
}
