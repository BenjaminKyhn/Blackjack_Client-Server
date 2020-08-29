package BlackjackFX;

import Model.Card;
import Model.Ranks;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class BlackjackClientFX extends Application {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private String host = "localhost";
    private int port = 8016;
    private int player;
    private int numberOfPlayers;
    private boolean lost = false;
    private boolean otherPlayerLost = false;

    @Override
    public void start(Stage stage) throws Exception {
        Pane pane = new Pane();

        Scene scene = new Scene(pane, 320, 320);
        stage.setTitle("BlackjackFX");
        stage.setScene(scene);
        stage.show();

        connectToServer();
    }

    public void connectToServer() {
        try {
            Socket socket = new Socket(host, port);

            // Establish input- and output streams
            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());

            // Receive player number from server
            player = (int) fromServer.readObject();

            // Receive number of players in the session from the server
            numberOfPlayers = (int) fromServer.readObject();
            System.out.println("Connected to Blackjack server. This session is for " + numberOfPlayers + " players " +
                    "and you are player " + player + ".");
            if (player == 1) {
                System.out.println("Waiting for more players...");
            }
            System.out.println();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        new Thread(() -> {
            try {
                // Start the game session
                // Contain all players' cards in their own lists
                ArrayList<Card> myHand = new ArrayList<>();
                ArrayList<Card> otherPlayerHand = new ArrayList<>();
                ArrayList<Card> dealerHand = new ArrayList<>();
                myHand.add((Card) fromServer.readObject());
                myHand.add((Card) fromServer.readObject());
                otherPlayerHand.add((Card) fromServer.readObject());
                otherPlayerHand.add((Card) fromServer.readObject());
                dealerHand.add((Card) fromServer.readObject());

                int hitCount = 0;
                int handValue = 0;
                int otherPlayerHandValue = 0;
                int dealerHandValue = 0;

                // Keep track of the players' hand values
                for (int i = 0; i < 2; i++) {
                    handValue += myHand.get(i).getValue();
                    otherPlayerHandValue += otherPlayerHand.get(i).getValue();
                }

                // Print out the current hand status of all players
                System.out.println("You were dealt " + myHand.get(0).getRank() + " of " + myHand.get(0).getSuit() +
                        " and " + myHand.get(1).getRank() + " of " + myHand.get(1).getSuit() + ". Value of your hand is " +
                        (handValue) + ".");
                System.out.println("The other player was dealth " + otherPlayerHand.get(0).getRank() + " of " +
                        otherPlayerHand.get(0).getSuit() + " and " + otherPlayerHand.get(1).getRank() + " of " +
                        otherPlayerHand.get(1).getSuit() + ". Value of his hand is " + (otherPlayerHand.get(0).getValue() +
                        otherPlayerHand.get(1).getValue()) + ".");
                System.out.println("The dealer drew " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                        " and an unknown card. The value of the dealers hand so far is " + (dealerHand.get(0).getValue()) + ".\n");

                int playerTurn;
                Scanner input = new Scanner(System.in);

                // Start the players' turns
                for (int i = 0; i < numberOfPlayers; i++) {
                    playerTurn = (int) fromServer.readObject();

                    // Take own turn
                    if (playerTurn == player) {
                        hitCount = 0;
                        if (handValue < 21) { // here
                            System.out.println("Your turn. Do you want to HIT or STAND?");
                            String answer = input.nextLine();
                            toServer.writeObject(answer);
                            while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("bust")) {
                                if (answer.toLowerCase().equals("hit")) {
                                    hitCount++;
                                    myHand.add((Card) fromServer.readObject());
                                    handValue = calculateHandValue(myHand);
                                    System.out.println("You hit " + myHand.get(hitCount + 1).getRank() + " of " +
                                            myHand.get(hitCount + 1).getSuit() + ".");
                                    if (handValue <= 21) {
                                        System.out.println("The value of your hand is " + (handValue) + ".");
                                        System.out.println("Do you want to HIT or STAND?");
                                    } else {
                                        System.out.println("You bust! The value of your hand is " + handValue + "!");
                                        answer = "bust";
                                        lost = true;
                                    }
                                } else
                                    System.out.println("Please type hit or stand.");
                                if (!answer.toLowerCase().equals("bust"))
                                    answer = input.nextLine();
                                toServer.writeObject(answer);
                            }
                            if (answer.toLowerCase().equals("bust"))
                                System.out.println("YOU LOSE.\n");
                            else
                                System.out.println("You chose to stand.\n");
                        } else
                            System.out.println("You hit natural Blackjack!");
                    }

                    // Observe the other players' turns
                    else {
                        System.out.println("Other player's turn.");
                        hitCount = 0;
                        if (otherPlayerHandValue < 21) {
                            String answer = (String) fromServer.readObject();
                            while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("bust")) {
                                if (answer.toLowerCase().equals("hit")) {
                                    hitCount++;
                                    otherPlayerHand.add((Card) fromServer.readObject());
                                    otherPlayerHandValue = calculateHandValue(otherPlayerHand);
                                    System.out.println("The other player hit " + otherPlayerHand.get(hitCount + 1).getRank() + " of " +
                                            otherPlayerHand.get(hitCount + 1).getSuit() + ".");
                                    if (otherPlayerHandValue <= 21) {
                                        System.out.println("The value of his hand is " + otherPlayerHandValue + ".");
                                    } else
                                        System.out.println("He bust! The value of his hand is " + otherPlayerHandValue + "!");
                                }
                                answer = (String) fromServer.readObject();
                            }
                            if (answer.toLowerCase().equals("bust")) {
                                System.out.println("The other player has lost.\n");
                                otherPlayerLost = true;
                            } else
                                System.out.println("The other player chose to stand.\n");
                        } else
                            System.out.println("The other player hit natural Blackjack!");
                    }
                }

                // Receive information about the dealer's hand
                dealerHand.add((Card) fromServer.readObject());
                dealerHandValue += dealerHand.get(0).getValue();
                dealerHandValue += dealerHand.get(1).getValue();
                System.out.println("The dealer's hand is " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                        " and " + dealerHand.get(1).getRank() + " of " + dealerHand.get(1).getSuit() +
                        ". The current value of his hand is " + dealerHandValue + ".");

                // Observe moves from the dealer
                if (dealerHandValue == 21) {
                    System.out.println("The dealer has natural Blackjack!");
                    if (!lost) {
                        System.out.println("YOU LOSE.");
                        lost = true;
                    }
                }

                if (!lost || !otherPlayerLost) {
                    while (dealerHandValue < 17) {
                        Card card = (Card) fromServer.readObject();
                        dealerHand.add(card);
                        dealerHandValue = calculateHandValue(dealerHand);
                        System.out.println("The dealer hit " + card.getRank() + " of " + card.getSuit() + ". The value " +
                                "of his hand is now " + dealerHandValue + ".");
                        if (dealerHandValue > 21)
                            System.out.println("The dealer bust!");
                    }
                }

                // Check who won
                if ((!lost && dealerHandValue == 21) || (!lost && dealerHandValue > handValue && dealerHandValue <= 21)
                        || (!lost && dealerHandValue == handValue && dealerHandValue <= 21))
                    System.out.println("\nYOU LOSE! THE DEALER WINS!");
                else if ((!lost && dealerHandValue > 21) || (!lost && handValue > dealerHandValue))
                    System.out.println("\nYOU WIN!");
                else if (lost && otherPlayerLost)
                    System.out.println("THE DEALER BEAT ALL PLAYERS!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Method for determining Ace value and calculating total hand value
    public int calculateHandValue(ArrayList<Card> cards) {
        int value = 0;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            value += card.getValue();
        }

        if (value > 21) {
            for (int i = 0; i < cards.size(); i++) {
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