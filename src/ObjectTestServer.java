import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ObjectTestServer {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(8011);
                System.out.println("Server startet at socket " + server.getLocalPort());

                while (true) {
                    System.out.println("Wait for client...");

                    // Connect the dealer
                    Socket socket = server.accept();
                    System.out.println("IP address is " + socket.getInetAddress());

                    new Thread(new HandleASession(socket)).start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    static class HandleASession implements Runnable, BlackjackConstants {
        private Socket socket;

        public HandleASession(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream from = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream to = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}