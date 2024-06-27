package it.unimib.sd2024;

import com.google.gson.JsonElement;
import it.unimib.sd2024.operations.AbstractDatabaseOperation;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to handle the Database.
 */
public class DatabaseHandler {
    private static final String folder;

    private static final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public static final String SET = "set";
    public static final String GET = "get";
    public static final String GET_IF = "getif";
    public static final String SET_IF = "setif";
    public static final String DELETE = "delete";
    public static final String SET_IF_NOT_EXISTS = "setifnotexists";
    public static final String DISCONNECT = "disconnect";

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            folder = System.getenv("APPDATA") + File.separator + "WebNexusDB";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            folder = System.getenv("HOME") + File.separator + "WebNexusDB";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }

        var file = new File(folder);

        if (!file.exists())
            file.mkdir();

        if (!file.isDirectory())
            throw new IllegalArgumentException("Error: Folder is not a directory");

        if (!file.canRead())
            throw new IllegalArgumentException("Error: Folder is not readable");

        if (!file.canWrite())
            throw new IllegalArgumentException("Error: Folder is not writable");
    }

    public static JsonElement handleRequest(String collection, AbstractDatabaseOperation operation) throws CommandException, IOException {
        Object lock = locks.computeIfAbsent(collection, k -> new Object());
        synchronized (lock) {
            File file = new File(folder + File.separator + collection);
            file.createNewFile();
            if (!file.isFile())
                throw new CommandException("Error: Unexpected error occurred while processing the request.");
            return operation.execute(file);
        }
    }
}