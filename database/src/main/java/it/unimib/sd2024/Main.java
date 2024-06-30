package it.unimib.sd2024;

import com.google.gson.*;
import it.unimib.sd2024.debug.InitDB;
import it.unimib.sd2024.operations.*;
import org.jetbrains.annotations.Nullable;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static it.unimib.sd2024.DatabaseHandler.*;

/**
 * Classe principale in cui parte il database.
 */
public class Main {
    /**
     * Porta di ascolto.
     */
    public static final int PORT = 3030;

    /**
     * Avvia il database e l'ascolto di nuove connessioni.
     */
    public static void startServer() throws IOException {

        try (var server = new ServerSocket(PORT)) {
            System.out.println("Database listening at localhost:" + PORT);
            while (true)
                new Handler(server.accept()).start();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Handler di una connessione del client.
     */
    private static class Handler extends Thread {
        private final Socket client;

        public Handler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                var out = new PrintWriter(client.getOutputStream(), true);
                var in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String inputLine;
                JsonArray result;

                while ((inputLine = in.readLine()) != null) {
                    result = new JsonArray();
                    result.add("true");
                    if (DISCONNECT.equalsIgnoreCase(inputLine)) {
                        result.add("Bye");
                        out.println(result);
                        break;
                    }

                    try {
                        JsonElement obj = executeCommand(inputLine.strip());
                        System.out.println("Result: " + obj);
                        if (obj != null)
                            result.add(obj.toString());
                    } catch (CommandException e) {
                        result.set(0, new JsonPrimitive("false"));
                        result.add(e.getLocalizedMessage());
                        System.err.println("Error : " + result);
                    } catch (IOException e) {
                        result.set(0, new JsonPrimitive("false"));
                        result.add("Error: Unknown error");
                        System.err.println("Error : " + e.getLocalizedMessage());
                        System.err.println(Arrays.toString(e.getStackTrace()));
                        break;
                    } catch (JsonSyntaxException e) {
                        result.set(0, new JsonPrimitive("false"));
                        result.add("Error: Invalid JSON");
                        System.err.println("Error : " + e.getLocalizedMessage());
                        System.err.println(Arrays.toString(e.getStackTrace()));
                    } catch (Exception e) {
                        result.set(0, new JsonPrimitive("false"));
                        result.add("Error: Unknown error");
                        System.err.println("Error : " + e.getLocalizedMessage());
                        System.err.println(Arrays.toString(e.getStackTrace()));
                    }

                    out.println(result);
                }

                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }

        /**
         * Esegue il comando passato dal client.
         *
         * @param inputLine comando passato dal client.
         *
         * @return risultato del comando.
         *
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         * @throws JsonSyntaxException in caso di errore di sintassi JSON.
         */
        @Nullable
        public JsonElement executeCommand(String inputLine) throws CommandException, IOException, JsonSyntaxException {
            Pattern pattern = Pattern.compile("\\A[a-z]+", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(inputLine);
            if (!matcher.find())
                throw new CommandException("Command not recognized");

            var command = matcher.group();
            inputLine = inputLine.substring(command.length()).strip();

            ParseResult parseResult = parseCommand(inputLine);

            return switch (command.toLowerCase()) {
                case SET -> set(parseResult);
                case GET -> get(parseResult);
                case GET_IF -> getIf(parseResult);
                case SET_IF -> setIf(parseResult);
                case SET_IF_NOT_EXISTS -> setIfNotExist(parseResult);
                case DELETE -> delete(parseResult);
                default -> throw new CommandException("Command not recognized");
            };
        }

        /**
         * Esegue il comando set.
         *
         * @param inputLine comando passato dal client.
         * @return null se ha l'operazione ha avuto successo.
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         * @throws JsonSyntaxException in caso di errore di sintassi JSON.
         */
        private JsonElement set(ParseResult inputLine) throws CommandException, IOException, JsonSyntaxException {
            JsonElement json = JsonParser.parseString(inputLine.stringLeft());

            var operation = new SetOperation(inputLine.key(), json);
            DatabaseHandler.handleRequest(inputLine.collection(), operation);
            return null;
        }

        /**
         * Esegue il comando get.
         *
         * @param inputLine comando passato dal client.
         * @return il risultato dell'operazione.
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         */
        private JsonElement get(ParseResult inputLine) throws CommandException, IOException {
            if (!inputLine.stringLeft().isBlank())
                throw new CommandException("Invalid command");

            var operation = new GetOperation(inputLine.key());

            return DatabaseHandler.handleRequest(inputLine.collection(), operation);
        }

        /**
         * Esegue il comando getif.
         *
         * @param inputLine comando passato dal client.
         * @return il risultato dell'operazione.
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         * @throws JsonSyntaxException in caso di errore di sintassi JSON.
         */
        private JsonElement getIf(ParseResult inputLine) throws CommandException, IOException, JsonSyntaxException {
            JsonArray array = JsonParser.parseString(inputLine.stringLeft()).getAsJsonArray();

            if (array.size() != 2)
                throw new CommandException("Invalid command");

            if (!(array.get(0).isJsonPrimitive() && array.get(0).getAsJsonPrimitive().isString()))
                throw new CommandException("Invalid command");

            var operation = new GetIfOperation(inputLine.key(), array.get(0).getAsString(), array.get(1));

            return DatabaseHandler.handleRequest(inputLine.collection(), operation);
        }

        /**
         * Esegue il comando setif.
         *
         * @param inputLine comando passato dal client.
         * @return null se ha l'operazione ha avuto successo.
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         * @throws JsonSyntaxException in caso di errore di sintassi JSON.
         */
        private JsonElement setIf(ParseResult inputLine) throws CommandException, IOException, JsonSyntaxException {
            JsonArray array = JsonParser.parseString(inputLine.stringLeft()).getAsJsonArray();

            if (array.size() != 3)
                throw new CommandException("Invalid command");

            if (!(array.get(1).isJsonPrimitive() && array.get(1).getAsJsonPrimitive().isString()))
                throw new CommandException("Invalid command");

            var operation = new SetIfOperation(inputLine.key(), array.get(0), array.get(1).getAsString(), array.get(2));

            DatabaseHandler.handleRequest(inputLine.collection(), operation);
            return null;
        }

        /**
         * Esegue il comando setifnotexists.
         *
         * @param inputLine comando passato dal client.
         * @return null se ha l'operazione ha avuto successo.
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         * @throws JsonSyntaxException in caso di errore di sintassi JSON.
         */
        private JsonElement setIfNotExist(ParseResult inputLine) throws CommandException, IOException, JsonSyntaxException {
            JsonElement json = JsonParser.parseString(inputLine.stringLeft());

            var operation = new SetIfNotExist(inputLine.key(), json);

            DatabaseHandler.handleRequest(inputLine.collection(), operation);
            return null;
        }

        /**
         * Esegue il comando delete.
         *
         * @param inputLine comando passato dal client.
         * @return null se ha l'operazione ha avuto successo.
         * @throws CommandException in caso di errore nel comando.
         * @throws IOException in caso di errore di I/O.
         */
        private JsonElement delete(ParseResult inputLine) throws CommandException, IOException {
            if (!inputLine.stringLeft().isBlank())
                throw new CommandException("Invalid command");

            var operation = new RemoveOperation(inputLine.key());

            DatabaseHandler.handleRequest(inputLine.collection(), operation);
            return null;
        }

        /**
         * Parsa il comando passato dal client.
         *
         * @param inputLine comando passato dal client.
         * @return risultato del parsing.
         */
        private ParseResult parseCommand(String inputLine) {
            int index = inputLine.indexOf(';');
            String collection = inputLine.substring(0, index).strip();
            if (collection.isEmpty() || collection.length() > 100)
                throw new CommandException("Invalid collection name: " + collection);

            boolean isValid = collection.matches("^[a-zA-Z0-9 ]*$");
            if (!isValid)
                throw new CommandException("Invalid collection name: " + collection);


            inputLine = inputLine.substring(index + 1);
            index = inputLine.indexOf(';');
            String keys = inputLine.substring(0, index).strip();
            var keyListTemp = Arrays.asList(keys.split(","));

            var keyList = keyListTemp.stream()
                    .map(String::trim)
                    .collect(Collectors.toList());

            for (String key : keyList) {
                if (key.isEmpty() || key.length() > 100)
                    throw new CommandException("Invalid key name: " + key);

                isValid = key.matches("^[a-zA-Z0-9 .]*$");
                if (!isValid)
                    throw new CommandException("Invalid key name: " + key);
            }

            String stringLeft = inputLine.substring(index + 1);

            return new ParseResult(collection, keyList, stringLeft);
        }

        /**
         * Classe di supporto per il parsing del comando.
         */
        private record ParseResult(String collection, List<String> key, String stringLeft) {}
    }

    /**
     * Metodo principale di avvio del database.
     *
     * @param args argomenti passati a riga di comando.
     *
     * @throws IOException exception in caso di errore di I/O.
     */
    public static void main(String[] args) throws IOException {
        InitDB.init();
        startServer();
    }
}

