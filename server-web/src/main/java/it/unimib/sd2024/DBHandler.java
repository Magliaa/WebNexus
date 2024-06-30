package it.unimib.sd2024;

import it.unimib.sd2024.objs.Order;
import it.unimib.sd2024.objs.User;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


public class DBHandler {
    /**
     * Porta del database.
     */
    private static final int DB_PORT = 3030;

    /**
     * Host del database.
     */
    private static final String DB_HOST = "localhost";

    /**
     * Configurazione per la serializzazione/deserializzazione di oggetti JSON.
     */
    private static final JsonbConfig config = new JsonbConfig().withNullValues(false);

    /**
     * Id dell'utente.
     */
    public static final AtomicLong userId;

    /**
     * Id dell'ordine.
     */
    public static final AtomicLong ordersId;

    /**
     * Comandi disponibili.
     */
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

    /**
     * Record per la connessione al database.
     */
    public record DBConnection(BufferedReader in, PrintWriter out, Socket socket) {
    }

    static {
        var connDB = connectToDatabase();

        if (connDB == null) {
            throw new IllegalStateException("Error while connecting to the database.");
        }

        try(Jsonb jsonb = JsonbBuilder.create(config)) {
            var response = sendRequest(Command.GET, "users", List.of("users"), null, connDB);

            if (response.get(0).equals("false")) {
                throw new IllegalStateException("Error while getting users from the database.");
            }
            int maxId = 0;
            Map<String, User> obj = jsonb.fromJson(response.get(1), new HashMap<String, User>(){}.getClass().getGenericSuperclass());
            for (var key : obj.keySet()) {
                maxId = Math.max(maxId, Integer.parseInt(key));
            }
            userId = new AtomicLong(maxId + 1);

            response = sendRequest(Command.GET, "orders", List.of("orders"), null, connDB);
            if (response.get(0).equals("false")) {
                throw new IllegalStateException("Error while getting orders from the database.");
            }
            int maxOrdersId = 0;
            Map<String, Order> objOrders = jsonb.fromJson(response.get(1), new HashMap<String, Order>(){}.getClass().getGenericSuperclass());
            for (var key : objOrders.keySet()) {
                maxOrdersId = Math.max(maxId, Integer.parseInt(key));
            }
            ordersId = new AtomicLong(maxOrdersId + 1);
        } catch (Exception e) {
            throw new IllegalStateException("Error while getting info from the database.");
        } finally {
            closeConnection(connDB);
        }
    }

    /**
     * Connessione al database.
     */
    public static DBConnection connectToDatabase() {
        try {
            var socket = new Socket(DB_HOST, DB_PORT);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new PrintWriter(socket.getOutputStream(), true);
            return new DBConnection(in, out, socket);
        } catch (IOException e) {
            System.err.println("Error while connecting to the database.");
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    /**
     * Invia una richiesta al database.
     */
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
                System.err.println("Error while parsing response from the database.");
                System.err.println(e.getLocalizedMessage());
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        } catch (IllegalStateException | IOException e) {
            System.err.println("Error while sending request to the database.");
        }
        throw new RuntimeException("Error while sending request to the database.");
    }

    /**
     * Chiude la connessione al database.
     */
    public static void closeConnection(DBConnection dbConn) {
        try {
            dbConn.out.println(Command.DISCONNECT.getValue());
            dbConn.in.readLine();
            dbConn.in.close();
            dbConn.out.close();
            dbConn.socket.close();
        } catch (IOException e) {
            System.err.println("Error while closing connection to the database.");
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
