import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class BlackjackServer implements BlackjackConstants {
    private static int sessionNo = 1;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(8005);
                System.out.println("Server startet at socket " + server.getLocalPort());

                while (true) {
                    System.out.println("Wait for players to join the game session...");

                    // Connect the dealer
                    Socket dealer = server.accept();
                    System.out.println(new Date() + ": The dealer joined session " + sessionNo);
                    System.out.println("The dealer's IP address is " + dealer.getInetAddress());
                    new DataOutputStream(dealer.getOutputStream()).writeInt(DEALER);

                    // Connect player 1
                    Socket player1 = server.accept();
                    System.out.println(new Date() + ": Player 1 joined session " + sessionNo);
                    System.out.println("Player 1's IP address is " + player1.getInetAddress());
                    new DataOutputStream(dealer.getOutputStream()).writeInt(PLAYER1);

                    System.out.println(new Date() + ": Starting a session for session " + sessionNo++);
                    new Thread(new HandleASession(dealer, player1)).start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    static class HandleASession implements Runnable, BlackjackConstants {
        private Socket dealer;
        private Socket player1;

        public HandleASession(Socket dealer, Socket player1) {
            this.dealer = dealer;
            this.player1 = player1;
        }

        @Override
        public void run() {
            try {
                DataInputStream fromDealer = new DataInputStream(dealer.getInputStream());
                DataOutputStream toDealer = new DataOutputStream(dealer.getOutputStream());
                DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());

                toDealer.writeInt(1);

                while (true){
                    int card = (int)(Math.random() * 52) + 1;
                    toPlayer1.writeInt(card);

                    int card1 = fromPlayer1.readInt();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
