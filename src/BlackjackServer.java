import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// Brug synchronized Deck for at undgå race conditions

public class BlackjackServer {
    public static void main(String[] args) {
        new Thread(() ->{
            new BlackjackServer();
        }).start();
    }

    public BlackjackServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8015);
            System.out.println("Server started ");

            while (true) {
                Socket player1 = serverSocket.accept();
                System.out.println("Player one connected.");

                Socket player2 = serverSocket.accept();
                System.out.println("Player two connected.");

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

        private ObjectInputStream fromPlayer1;
        private ObjectOutputStream toPlayer1;
        private ObjectInputStream fromPlayer2;
        private ObjectOutputStream toPlayer2;

        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public void run() {
            try {
                toPlayer1 = new ObjectOutputStream(player1.getOutputStream());
                fromPlayer1 = new ObjectInputStream(player1.getInputStream());
                toPlayer2 = new ObjectOutputStream(player2.getOutputStream());
                fromPlayer2 = new ObjectInputStream(player2.getInputStream());

                int clientNo1 = fromPlayer1.readInt();
                int clientNo2 = fromPlayer2.readInt();
                System.out.println("Player1 client no: " + clientNo1);
                System.out.println("Player1 client no: " + clientNo2);

                Card card1 = (Card) fromPlayer1.readObject();
                Card card2 = (Card) fromPlayer2.readObject();

                toPlayer1.writeObject(card1);
                toPlayer2.writeObject(card2);
            } catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }
}