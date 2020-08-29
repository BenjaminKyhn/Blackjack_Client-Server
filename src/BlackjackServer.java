import Model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class BlackjackServer implements BlackjackConstants {
    private ArrayList<ObjectInputStream> fromPlayers = new ArrayList<>();
    private ArrayList<ObjectOutputStream> toPlayers = new ArrayList<>();
    private int numberOfPlayers = 2;
    private int currentPlayer;

    public static void main(String[] args) {
        new BlackjackServer();
    }

    public BlackjackServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8015);
                System.out.println(new Date() + ": Blackjack server started. Waiting for players to connect...");

                while (true) {
                    // Connect to player 1
                    Socket player1 = serverSocket.accept();
                    System.out.println("Player one connected.");
                    toPlayers.add(new ObjectOutputStream(player1.getOutputStream()));
                    fromPlayers.add(new ObjectInputStream(player1.getInputStream()));
                    toPlayers.get(0).writeObject(BlackjackConstants.PLAYER1); // send player number
                    toPlayers.get(0).writeObject(numberOfPlayers); // send number of players in the game

                    // Connect to player 2
                    Socket player2 = serverSocket.accept();
                    System.out.println("Player two connected.");
                    toPlayers.add(new ObjectOutputStream(player2.getOutputStream()));
                    fromPlayers.add(new ObjectInputStream(player2.getInputStream()));
                    toPlayers.get(1).writeObject(BlackjackConstants.PLAYER2); // send player number
                    toPlayers.get(1).writeObject(numberOfPlayers); // send number of players in the game

                    // Start the game session in a new thread
                    System.out.println("Game session started for two players");
                    new Thread(new HandleASession(player1, player2)).start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    class HandleASession implements Runnable {
        private Socket player1;
        private Socket player2;
        private boolean continueToPlay = true;
        private ArrayList<ArrayList<Card>> playerHands = new ArrayList<>();
        private ArrayList<Card> dealerHand = new ArrayList<>();
        private int handValue;
        private int[] handValues = new int[numberOfPlayers];
        private boolean[] playerLost = new boolean[numberOfPlayers];
        private Deck deck = new Deck();

        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public void run() {
            try {
                // Initialize deck, player hands and dealers hand
                startGame(playerHands, dealerHand, handValues, handValue, playerLost, deck);

                // Send cards drawn to the players
                sendHandsToPlayers(playerHands, dealerHand);
                System.out.println("All cards have been dealt. Waiting for player 1 to make a move...");

                // Ready player moves
                readPlayerMoves(handValues, deck ,playerHands, playerLost);

                // Send the dealer's other card to the players
                sendDealerCardsToPlayers(dealerHand);

                // Take the dealer's turn
                takeDealerTurn(handValue, playerLost, deck, dealerHand);

                System.out.println("Game finished.");

                //TODO: Handle more than one session. Something about moving streams to the session.
                //TODO: Fix dealer's opening hand value showing 1 if he has an ace

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startGame(ArrayList<ArrayList<Card>> playerHands, ArrayList<Card> dealerHand, int[] handValues,
                           int handValue, boolean[] playerLost, Deck deck){
        // Keep track of player status (if he loses)
        for (int i = 0; i < numberOfPlayers; i++) {
            playerLost[i] = false;
        }

        // Generate player hands and contain them in a list.
        for (int i = 0; i < numberOfPlayers; i++) {
            ArrayList<Card> hand = new ArrayList<>();
            hand.add(deck.draw());
            hand.add(deck.draw());
            playerHands.add(hand);
        }

        // Draw cards for the dealer
        dealerHand.add(new Card(Ranks.ACE, Suits.HEARTS));
        dealerHand.add(new Card(Ranks.ACE, Suits.HEARTS));
//        dealerHand.add(deck.draw());
//        dealerHand.add(deck.draw());

        // Keep track of hand values
        handValue = calculateHandValue(dealerHand);
        for (int i = 0; i < numberOfPlayers; i++) {
            handValues[i] = calculateHandValue(playerHands.get(i));
        }
    }

    private void sendHandsToPlayers(ArrayList<ArrayList<Card>> playerHands, ArrayList<Card> dealerHand) throws IOException {
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
    }

    private void readPlayerMoves(int[] handValues, Deck deck, ArrayList<ArrayList<Card>> playerHands,
                                 boolean[] playerLost) throws IOException, ClassNotFoundException {
        for (int i = 0; i < numberOfPlayers; i++) {
            // Keep track of the current player
            currentPlayer = i + 1;
            // Let all players know who the current player is
            for (int j = 0; j < numberOfPlayers; j++) {
                toPlayers.get(j).writeObject(currentPlayer);
            }

            // Receive answers only if the player didn't hit 21
            if (handValues[i] < 21) {
                // Receive an answer from the current player
                String answer = (String) fromPlayers.get(i).readObject();

                // Send the answer to inactive players
                if (currentPlayer == 1) {
                    toPlayers.get(1).writeObject(answer);
                }
                if (currentPlayer == 2) {
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
                    } else
                        System.out.println("Please type hit or stand.");

                    // Receive an answer
                    answer = (String) fromPlayers.get(i).readObject();

                    // Send the answer to inactive players
                    if (currentPlayer == 1) {
                        toPlayers.get(1).writeObject(answer);
                    }
                    if (currentPlayer == 2) {
                        toPlayers.get(0).writeObject(answer);
                    }
                }
                if (answer.toLowerCase().equals("bust")) {
                    System.out.println("The player bust.");
                    playerLost[i] = true;
                } else
                    System.out.println("The player stands.");
            }
        }
    }

    private void sendDealerCardsToPlayers(ArrayList<Card> dealerHand) throws IOException {
        for (int i = 0; i < numberOfPlayers; i++) {
            toPlayers.get(i).writeObject(dealerHand.get(1));
        }
    }

    private void takeDealerTurn(int handValue, boolean[] playerLost, Deck deck, ArrayList<Card> dealerHand) throws IOException {
        if (handValue == 21)
            System.out.println("Dealer has natural Blackjack.");

        // Hit cards only if some of the players haven't lost
        for (int i = 0; i < numberOfPlayers; i++) {
            if (!playerLost[i]) {
                // Hit new cards if the handValue is below 17
                while (handValue < 17) {
                    Card card = deck.draw();
                    dealerHand.add(card);
                    handValue = calculateHandValue(dealerHand);
                    for (int j = 0; j < numberOfPlayers; j++) {
                        toPlayers.get(j).writeObject(card);
                    }
                }
                break;
            }
        }
    }

    // Method for determining Ace value and calculating total hand value
    private int calculateHandValue(ArrayList<Card> cards) {
        int value = 0;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            value += card.getValue();
        }

        if (value > 21) {
            for (int i = cards.size() - 1; i >= 0; i--) {
                Card card = cards.get(i);
                if (card.getRank() == Ranks.ACE) {
                    card.setValue(1);
                    value = 0;
                    for (int j = 0; j < cards.size(); j++) {
                        value += cards.get(j).getValue();
                    }
                }
                if (value <= 21)
                    break;
            }
        }
        return value;
    }
}