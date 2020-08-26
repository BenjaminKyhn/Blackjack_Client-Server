import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

// Brug synchronized Deck for at undgå race conditions

public class BlackjackServer implements BlackjackConstants{
    private ObjectInputStream fromPlayer1;
    private ObjectOutputStream toPlayer1;
    private ObjectInputStream fromPlayer2;
    private ObjectOutputStream toPlayer2;

    public static void main(String[] args) {
        new BlackjackServer();
    }

    public BlackjackServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8015);
            System.out.println(new Date() + ": Blackjack server started");

            while (true) {
                Socket player1 = serverSocket.accept();
                System.out.println("Player one connected.");
                toPlayer1 = new ObjectOutputStream(player1.getOutputStream());
                fromPlayer1 = new ObjectInputStream(player1.getInputStream());
                toPlayer1.writeObject(PLAYER1);

                Socket player2 = serverSocket.accept();
                System.out.println("Player two connected.");
                toPlayer2 = new ObjectOutputStream(player2.getOutputStream());
                fromPlayer2 = new ObjectInputStream(player2.getInputStream());
                toPlayer2.writeObject(PLAYER2);

                System.out.println("Starting a new session...");
                new Thread(new HandleASession(player1, player2)).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    class HandleASession implements Runnable {
        private Socket player1;
        private Socket player2;

        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public void run() {
            try {
                Deck deck = new Deck();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}