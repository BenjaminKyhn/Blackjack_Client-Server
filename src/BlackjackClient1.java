import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlackjackClient1 implements BlackjackConstants {
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

                if (player == DEALER) {
                    System.out.println("You are the dealer.");
                    System.out.println("Waiting for player to join...");

                    // Receive startup notification from server
                    fromServer.readInt();

                    System.out.println("Player has joined. Dealing cards.");
                } else if (player == PLAYER1) {
                    System.out.println("You are the player.");
                }

                int card1 = fromServer.readInt();
                int card2 = fromServer.readInt();
                System.out.println("Your cards are " + card1 + " and " + card2 + ".");

                while (continueToPlay) {
                    if (player == DEALER) {
                    }
                    else if (player == PLAYER1) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void drawCards() throws IOException {
        int card1 = (int) (Math.random() * 52) + 1;
        int card2 = (int) (Math.random() * 52) + 1;
        int card3 = (int) (Math.random() * 52) + 1;
        int card4 = (int) (Math.random() * 52) + 1;
        toServer.writeInt(card1);
        toServer.writeInt(card2);
        toServer.writeInt(card3);
        toServer.writeInt(card4);
    }

    private static void receiveInfoFromServer() throws IOException {
        int status = fromServer.readInt();
    }
}