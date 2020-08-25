import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class BlackjackClient2 implements BlackjackConstants {
    private static String host = "localhost";
    private static int port = 8010;
    private static DataInputStream fromServer;
    private static DataOutputStream toServer;
    private static boolean continueToPlay = true;

    public static void main(String[] args) {
        connectToServer();
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket(host, port);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                int player = fromServer.readInt();

                if (player == DEALER){
                    System.out.println("You are the dealer");
                }
                else if (player == PLAYER1){
                    System.out.println("You are the player");
                }

                while (continueToPlay) {
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
