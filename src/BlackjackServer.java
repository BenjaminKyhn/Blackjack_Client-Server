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
    private int currentPlayer;

    public static void main(String[] args) {
        new BlackjackServer();
    }

    public BlackjackServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8015);
            System.out.println(new Date() + ": Blackjack server started. Waiting for players to connect...");

            while (true) {
                // Connect to player 1
                Socket player1 = serverSocket.accept();
                System.out.println("Player one connected.");
                toPlayers.add(new ObjectOutputStream(player1.getOutputStream()));
                fromPlayers.add(new ObjectInputStream(player1.getInputStream()));
                toPlayers.get(0).writeObject(PLAYER1); // send player number
                toPlayers.get(0).writeObject(numberOfPlayers); // send number of players in the game

                // Connect to player 2
                Socket player2 = serverSocket.accept();
                System.out.println("Player two connected.");
                toPlayers.add(new ObjectOutputStream(player2.getOutputStream()));
                fromPlayers.add(new ObjectInputStream(player2.getInputStream()));
                toPlayers.get(1).writeObject(PLAYER2); // send player number
                toPlayers.get(1).writeObject(numberOfPlayers); // send number of players in the game

                // Start the game session in a new thread
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
                // Create a new deck of cards
                Deck deck = new Deck();

                // Generate player hands and contain them in a list. The dealer's hand is the last element of the list.
                ArrayList<ArrayList<Card>> playerHands = new ArrayList<>();
                ArrayList<Card> dealerHand = new ArrayList<>();
                for (int i = 0; i < numberOfPlayers; i++) {
                    ArrayList<Card> hand = new ArrayList<>();
                    hand.add(deck.draw());
                    hand.add(deck.draw());
                    playerHands.add(hand);
                }

                // Draw cards for the dealer
                dealerHand.add(deck.draw());
                dealerHand.add(deck.draw());

                // Keep track of the dealer's hand value
                int handValue = dealerHand.get(0).getValue() + dealerHand.get(1).getValue();

                // Send information about player hands to player 1
                toPlayers.get(0).writeObject(playerHands.get(0).get(0));
                toPlayers.get(0).writeObject(playerHands.get(0).get(1));
                toPlayers.get(0).writeObject(playerHands.get(1).get(0));
                toPlayers.get(0).writeObject(playerHands.get(1).get(1));
                toPlayers.get(0).writeObject(dealerHand.get(0));

                // Send information about player hands to player 2
                toPlayers.get(1).writeObject(playerHands.get(1).get(0));
                toPlayers.get(1).writeObject(playerHands.get(1).get(1));
                toPlayers.get(1).writeObject(playerHands.get(0).get(0));
                toPlayers.get(1).writeObject(playerHands.get(0).get(1));
                toPlayers.get(1).writeObject(dealerHand.get(0));

                System.out.println("All cards have been dealt. Waiting for player 1 to make a move...");

                // Read moves from players
                for (int i = 0; i < numberOfPlayers; i++) {
                    // Keep track of the current player
                    currentPlayer = i + 1;
                    // Let all players know who the current player is
                    for (int j = 0; j < numberOfPlayers; j++) {
                        toPlayers.get(j).writeObject(currentPlayer);
                    }
                    // Receive an answer from the current player
                    String answer = (String) fromPlayers.get(i).readObject();

                    // Send the answer to inactive players
                    if (currentPlayer == 1){
                        toPlayers.get(1).writeObject(answer);
                    }
                    if (currentPlayer == 2){
                        toPlayers.get(0).writeObject(answer);
                    }

                    // Handle hit and stand answers
                    while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("bust")) {
                        if (answer.toLowerCase().equals("hit")) {
                            System.out.println("Player " + (i + 1) + " chose to hit. Drawing a new card and waiting " +
                                    "for the next move...");
                            Card card = deck.draw();
                            playerHands.get(i).add(card);
                            for (int j = 0; j < numberOfPlayers; j++) {
                                toPlayers.get(j).writeObject(card);
                            }
                        }
                        else
                            System.out.println("Please type hit or stand.");

                        // Receive an answer
                        answer = (String) fromPlayers.get(i).readObject();

                        // Send the answer to inactive players
                        if (currentPlayer == 1){
                            toPlayers.get(1).writeObject(answer);
                        }
                        if (currentPlayer == 2){
                            toPlayers.get(0).writeObject(answer);
                        }
                    }
                    System.out.println("The player stands.");
                }

                // Send the dealer's other card to the players
                for (int i = 0; i < numberOfPlayers; i++) {
                    toPlayers.get(i).writeObject(dealerHand.get(1));
                }

                // Hit new cards if the handValue is below 17
                while (handValue < 17){
                    Card card = deck.draw();
                    dealerHand.add(card);
                    handValue += card.getValue();
                    System.out.println(calculateHandValue(dealerHand));
                    for (int i = 0; i < numberOfPlayers; i++) {
                        toPlayers.get(i).writeObject(card);
                    }
                }

                System.out.println("Game finished.");

                //TODO: Disallow dealer from drawing anymore cards if both players already bust
                //TODO: Fix Ace value
                //TODO: Handle more than one session
                //TODO: add clause for when the initial hand is 21 (in client)

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Method for determining Ace value and calculating total hand value
    public int calculateHandValue(ArrayList<Card> cards){
        int value = 0;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            value += card.getValue();
        }

        outer: while (true){
            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                if (card.getRank() == Ranks.ACE){
                    card.setValue(1);
                    value = 0;
                    for (int j = 0; j < cards.size(); j++) {
                        value += cards.get(j).getValue();
                    }
                }
                if (value <= 21)
                    break outer;
            }
        }

        return value;
    }
}