import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import it.unimib.sd2024.DatabaseHandler;
import it.unimib.sd2024.Main;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetTest {

    public SetTest() {
        Thread server = new Thread(() -> {
            try {
                Main.main(new String[]{});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.start();
    }

    @Test
    public void normalSet() {
        var arr = new Thread[10];
        for (int i = 0; i<10; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                try (Socket socket = new Socket("localhost", Main.PORT)) {
                    var out = new PrintWriter(socket.getOutputStream(), true);
                    var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("set beppe" + 1 +
                            " ; ciao3 ; {\"name\":" + finalI +"}");
                    var array = JsonParser.parseString(in.readLine()).getAsJsonArray();
                    System.out.println(array);
                    assertTrue(array.get(0).getAsBoolean());
                    assertTrue(array.get(1).getAsBoolean());
                    out.println("Disconnect");
                    in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            arr[i] = thread;
        }

        for (Thread thread : arr) {
            thread.start();
        }

        for (Thread thread : arr) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void keyPathTest() {
        var arr = new Thread[2];
        for (int i = 0; i<2; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                try (Socket socket = new Socket("localhost", Main.PORT)) {
                    var out = new PrintWriter(socket.getOutputStream(), true);
                    var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("set beppe" + 45 +
                            " ; ciao3,32131, 53424, sads ; {\"name\":" + finalI +"}");
                    var array = JsonParser.parseString(in.readLine()).getAsJsonArray();
                    System.out.println(array);
                    assertTrue(array.get(0).getAsBoolean());
                    assertTrue(array.get(1).getAsBoolean());
                    out.println("Disconnect");
                    in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            arr[i] = thread;
        }

        for (Thread thread : arr) {
            thread.start();
        }

        for (Thread thread : arr) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void delete() {
        Thread thread = new Thread(() -> {
            try (Socket socket = new Socket("localhost", Main.PORT)) {
                var out = new PrintWriter(socket.getOutputStream(), true);
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("delete beppe1 ; ciao3 ; ");
                var array = JsonParser.parseString(in.readLine()).getAsJsonArray();
                System.out.println(array);
                assertTrue(array.get(0).getAsBoolean());
                assertTrue(array.get(1).getAsBoolean());
                out.println("Disconnect");
                in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
