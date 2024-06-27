package it.unimib.sd2024.debug;

import com.google.gson.JsonParser;
import it.unimib.sd2024.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;


public class InitDB {
    private static final boolean FILL_DB = true;

    private static final boolean CLEAN_DB = true;

    public static void init() {
        if (!FILL_DB) {
            return;
        }

        if (CLEAN_DB) {
            String os = System.getProperty("os.name").toLowerCase();
            String folder;
            if (os.contains("win")) {
                folder = System.getenv("APPDATA") + File.separator + "WebNexusDB";
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                folder = System.getenv("HOME") + File.separator + "WebNexusDB";
            } else {
                throw new UnsupportedOperationException("Unsupported operating system");
            }

            var file = new File(folder);
            if (file.exists()) {
                for (var f : Objects.requireNonNull(file.listFiles())) {
                    f.delete();
                }
            }
        }

        Thread thread = new Thread(() -> {
            System.out.println("Database handler started.");
            try(var socket = new Socket("localhost", Main.PORT)) {
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var out = new PrintWriter(socket.getOutputStream(), true);

                for (var domain : DomainGenerator.domains.entrySet()) {
                    out.println("set domains ; domains , " + domain.getKey() + " ; " + domain.getValue());
                    var response = in.readLine();

                    if (response == null) {
                        System.err.println("Error while setting domain.");
                        System.exit(1);
                    }

                    var responseArray = JsonParser.parseString(response).getAsJsonArray();
                    if (!responseArray.get(0).getAsBoolean()) {
                        System.err.println("Error while setting domain.");
                        System.exit(1);
                    }
                }

                for (var user : UsersGenerator.users.entrySet()) {
                    out.println("set users ; users , " + user.getKey() + " ; " + user.getValue());
                    var response = in.readLine();

                    if (response == null) {
                        System.err.println("Error while setting user.");
                        System.exit(1);
                    }

                    var responseArray = JsonParser.parseString(response).getAsJsonArray();
                    if (!responseArray.get(0).getAsBoolean()) {
                        System.err.println("Error while setting user.");
                        System.exit(1);
                    }
                }

                for (var order : OrdersGenerator.orders.entrySet()) {
                    out.println("set orders ; orders , " + order.getKey() + " ; " + order.getValue());
                    var response = in.readLine();

                    if (response == null) {
                        System.err.println("Error while setting order.");
                        System.exit(1);
                    }

                    var responseArray = JsonParser.parseString(response).getAsJsonArray();
                    if (!responseArray.get(0).getAsBoolean()) {
                        System.err.println("Error while setting order.");
                        System.exit(1);
                    }
                }

                out.println("Disconnect");
                in.readLine();
                in.close();
                out.close();
                System.out.println("Domains set successfully.");
            } catch (Exception e) {
                System.err.println("Error while starting the server.");
                System.exit(1);
            }
        });
        thread.start();
    }
}
