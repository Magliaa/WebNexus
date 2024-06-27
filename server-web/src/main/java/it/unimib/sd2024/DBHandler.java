package it.unimib.sd2024;

import jakarta.annotation.Nullable;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.validation.constraints.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class DBHandler {

    /**
     * Porta del database.
     */
    private static final int DB_PORT = 3030;

    /**
     *
     */
    private static final String DB_HOST = "localhost";

    private static final JsonbConfig config = new JsonbConfig().withNullValues(false);

    public static final AtomicLong id = new AtomicLong(10);
    public static final AtomicLong ordersId = new AtomicLong(10);

    public enum Command {
        SET("set"),
        GET("get"),
        SET_IF("setif"),
        DELETE("delete"),
        SET_IF_NOT_EXISTS("setifnotexists"),
        GET_IF("getif"),
        DISCONNECT("disconnect");

        private final String value;

        Command(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public record DBConnection(BufferedReader in, PrintWriter out, Socket socket) {
    }

    public static DBConnection connectToDatabase() {
        try {
            var socket = new Socket(DB_HOST, DB_PORT);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new PrintWriter(socket.getOutputStream(), true);
            return new DBConnection(in, out, socket);
        } catch (IOException e) {
            System.err.println("Error while connecting to the database.");
        }
        return null;
    }

    // send command to the database and read responses
    public static @NotNull List<String> sendRequest(Command cmd, String collection, List<String> keys, @Nullable String data, DBConnection dbConn) {
        try {
            String input = cmd.getValue() + " " + collection + " ; ";
            input += String.join(", ", keys) + " ;";
            if (data != null) {
                input += " " + data;
            }
            dbConn.out.println(input);
            String response = dbConn.in.readLine();

            if (response == null)
                return null;

            try(Jsonb jsonb = JsonbBuilder.create(config)) {
                System.out.println(response);
                List<String> result = jsonb.fromJson(response, new ArrayList<String>(){}.getClass().getGenericSuperclass());
                assert result != null;
                assert result.size() == 2;
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IllegalStateException | IOException e) {
            System.err.println("Error while sending request to the database.");
        }
        throw new RuntimeException("Error while sending request to the database.");
    }

    public static void closeConnection(DBConnection dbConn) {
        try {
            dbConn.out.println(Command.DISCONNECT.getValue());
            dbConn.in.readLine();
            dbConn.in.close();
            dbConn.out.close();
            dbConn.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
