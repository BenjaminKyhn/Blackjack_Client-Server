import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

// Brug synchronized Deck for at undgå race conditions

public class BlackjackServer implements BlackjackConstants {
    private ArrayList<ObjectInputStream> fromPlayers = new ArrayList<>();
    private ArrayList<ObjectOutputStream> toPlayers = new ArrayList<>();
    private int numberOfPlayers = 2;

    public static void main(String[] args) {
        new BlackjackServer();
    }

    public BlackjackServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8015);
            System.out.println(new Date() + ": Blackjack server started. Waiting for players to connect...");

            while (true) {
                Socket player1 = serverSocket.accept();
                System.out.println("Player one connected.");
                toPlayers.add(new ObjectOutputStream(player1.getOutputStream()));
                fromPlayers.add(new ObjectInputStream(player1.getInputStream()));
                toPlayers.get(0).writeObject(PLAYER1); // send player number
                toPlayers.get(0).writeObject(numberOfPlayers); // send number of players in the game

                Socket player2 = serverSocket.accept();
                System.out.println("Player two connected.");
                toPlayers.add(new ObjectOutputStream(player2.getOutputStream()));
                fromPlayers.add(new ObjectInputStream(player2.getInputStream()));
                toPlayers.get(1).writeObject(PLAYER2); // send player number
                toPlayers.get(1).writeObject(numberOfPlayers); // send number of players in the game

                System.out.println("Game session started for two players");
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
                ArrayList<Card> dealerHand = new ArrayList<>();
                ArrayList<Card> player1Hand = new ArrayList<>();
                ArrayList<Card> player2Hand = new ArrayList<>();

                player1Hand.add(deck.draw());
                player1Hand.add(deck.draw());
                player2Hand.add(deck.draw());
                player2Hand.add(deck.draw());
                dealerHand.add(deck.draw());
                dealerHand.add(deck.draw());

                toPlayers.get(0).writeObject(player1Hand.get(0));
                toPlayers.get(0).writeObject(player1Hand.get(1));
                toPlayers.get(0).writeObject(player2Hand.get(0));
                toPlayers.get(0).writeObject(player2Hand.get(1));
                toPlayers.get(0).writeObject(dealerHand.get(0));

                toPlayers.get(1).writeObject(player2Hand.get(0));
                toPlayers.get(1).writeObject(player2Hand.get(1));
                toPlayers.get(1).writeObject(player1Hand.get(0));
                toPlayers.get(1).writeObject(player1Hand.get(1));
                toPlayers.get(1).writeObject(dealerHand.get(0));

                int player1HitCount = 0;
                int player2HitCount = 0;

                System.out.println("All cards have been dealt. Waiting for player 1 to make a move...");

                // fori loop that reads moves from a player i = amount of players
                // This requires the Input and Output streams to be contained in a list, so you can iterate through them

                // Read moves from player 1
                String answerPlayer1 = (String) fromPlayers.get(0).readObject();
                while (!answerPlayer1.toLowerCase().equals("stand")) {
                    if (answerPlayer1.toLowerCase().equals("hit")) {
                        player1HitCount++;
                        System.out.println("Player 1 chose hit. Drawing a new card and waiting for next answer...");
                        player1Hand.add(deck.draw());
                        toPlayers.get(0).writeObject(player1Hand.get(player1HitCount + 1));
                    } else
                        System.out.println("Please type hit or stand.");
                    answerPlayer1 = (String) fromPlayers.get(0).readObject();
                }

                // Read moves from player 2
                System.out.println("Player 1 chose to stand. Waiting for player 2 to make a move...");
                String answerPlayer2 = (String) fromPlayers.get(1).readObject();
                while (!answerPlayer2.toLowerCase().equals("stand")) {
                    if (answerPlayer2.toLowerCase().equals("hit")) {
                        player2HitCount++;
                        System.out.println("Player 2 chose hit. Drawing a new card and waiting for next answer...");
                        player2Hand.add(deck.draw());
                        toPlayers.get(1).writeObject(player2Hand.get(player2HitCount + 1));
                    } else
                        System.out.println("Please type hit or stand.");
                    answerPlayer2 = (String) fromPlayers.get(1).readObject();
                }

                System.out.println("Player 2 chose to stand. Both players are finished playing");

                //TODO: Send data to all connected players about the player currently playing
                //TODO: Add lose condition for players choosing to hit

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}