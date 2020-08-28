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
                ArrayList<ArrayList<Card>> playerHands = new ArrayList<>();
                ArrayList<Card> dealerHand = new ArrayList<>();

                // Generate player hands and contain them in a list. The dealer's hand is the last element of the list.
                for (int i = 0; i < numberOfPlayers; i++) {
                    ArrayList<Card> hand = new ArrayList<>();
                    hand.add(deck.draw());
                    hand.add(deck.draw());
                    playerHands.add(hand);
                }

                dealerHand.add(deck.draw());
                dealerHand.add(deck.draw());

                toPlayers.get(0).writeObject(playerHands.get(0).get(0));
                toPlayers.get(0).writeObject(playerHands.get(0).get(1));
                toPlayers.get(0).writeObject(playerHands.get(1).get(0));
                toPlayers.get(0).writeObject(playerHands.get(1).get(1));
                toPlayers.get(0).writeObject(dealerHand.get(0));

                toPlayers.get(1).writeObject(playerHands.get(1).get(0));
                toPlayers.get(1).writeObject(playerHands.get(1).get(1));
                toPlayers.get(1).writeObject(playerHands.get(0).get(0));
                toPlayers.get(1).writeObject(playerHands.get(0).get(1));
                toPlayers.get(1).writeObject(dealerHand.get(0));

                System.out.println("All cards have been dealt. Waiting for player 1 to make a move...");

                for (int j = 0; j < numberOfPlayers; j++) {
                    toPlayers.get(j).writeObject(1);
                }

                // Read moves from players
                for (int i = 0; i < numberOfPlayers; i++) {
                    String answer = (String) fromPlayers.get(i).readObject();
                    while (!answer.toLowerCase().equals("stand")) {
                        if (answer.toLowerCase().equals("hit")) {
                            System.out.println("Player " + (i + 1) + " chose to hit. Drawing a new card and waiting " +
                                    "for the next move...");
                            Card card = deck.draw();
                            playerHands.get(i).add(card);
                            toPlayers.get(i).writeObject(card);
                        }
                        else
                            System.out.println("Please type hit or stand.");
                        answer = (String) fromPlayers.get(i).readObject();
                    }
                    System.out.println("Stand");
                }

                System.out.println("Both players are finished playing.");

                //TODO: Send data to all connected players about the player currently playing
                //TODO: Add lose condition for players choosing to hit

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}