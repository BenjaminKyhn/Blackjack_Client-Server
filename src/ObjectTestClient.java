import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ObjectTestClient {
    private static String host = "localhost";
    private static int port = 8011;
    private static ObjectInputStream fromServer;
    private static ObjectOutputStream toServer;

    public static void main(String[] args) {
        connectToServer();
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket(host, port);
            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                System.out.println("test");
                Object card = fromServer.readObject();
                System.out.println(card);
                System.out.println("test");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}