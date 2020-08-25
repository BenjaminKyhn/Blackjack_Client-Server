import java.io.*;
import java.net.Socket;

public class BlackjackClient1 implements BlackjackConstants {
    private static String host = "localhost";
    private static int port = 8010;
    private static DataInputStream dataFromServer;
    private static DataOutputStream dataToServer;
    private static ObjectInputStream objectFromServer;
    private static ObjectOutputStream objectToServer;
    private static boolean continueToPlay = true;

    public static void main(String[] args) {
        connectToServer();
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket(host, port);
            dataFromServer = new DataInputStream(socket.getInputStream());
            dataToServer = new DataOutputStream(socket.getOutputStream());
            objectFromServer = new ObjectInputStream(socket.getInputStream());
            objectToServer = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                int player = dataFromServer.readInt();

                if (player == DEALER) {
                    System.out.println("You are the dealer.");
                    System.out.println("Waiting for player to join...");

                    // Receive startup notification from server
                    dataFromServer.readInt();

                    System.out.println("Player has joined. Dealing cards.");
                } else if (player == PLAYER1) {
                    System.out.println("You are the player.");
                }

                Object card1 = objectFromServer.readObject();
                Object card2 = objectFromServer.readObject();
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
        dataToServer.writeInt(card1);
        dataToServer.writeInt(card2);
        dataToServer.writeInt(card3);
        dataToServer.writeInt(card4);
    }

    private static void receiveInfoFromServer() throws IOException {
        int status = dataFromServer.readInt();
    }
}
