package BlackjackFX;

import Model.Card;
import Model.Ranks;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
    private int port = 8015;
    private int player;
    private int numberOfPlayers;
    private boolean lost = false;
    private boolean otherPlayerLost = false;
    private ArrayList<Card> myHand = new ArrayList<>();
    private ArrayList<Card> otherPlayerHand = new ArrayList<>();
    private ArrayList<Card> dealerHand = new ArrayList<>();
    private int hitCount = 0;
    private int handValue = 0;
    private int otherPlayerHandValue = 0;
    private int dealerHandValue = 0;
    private int playerTurn;

    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane pane = new AnchorPane();

        Label dealerName = new Label("Dealer");
        Label player1Name = new Label("Player 1");
        Label player2Name = new Label("Player 2");

        Image card1 = new Image("image/card/1.png");
        Image card2 = new Image("image/card/2.png");
        Image card3 = new Image("image/card/3.png");
        Image card4 = new Image("image/card/4.png");
        Image card5 = new Image("image/card/5.png");
        Image card6 = new Image("image/card/6.png");

        ImageView imageView1 = new ImageView(card1);
        ImageView imageView2 = new ImageView(card2);
        ImageView imageView3 = new ImageView(card3);
        ImageView imageView4 = new ImageView(card4);
        ImageView imageView5 = new ImageView(card5);
        ImageView imageView6 = new ImageView(card6);

        pane.getChildren().addAll(dealerName, player1Name, player2Name, imageView1, imageView2, imageView3, imageView4,
                imageView5, imageView6);

        Scene scene = new Scene(pane, 600, 600);
        stage.setTitle("BlackjackFX");
        stage.setMinHeight(600);
        stage.setMinWidth(600);
        stage.setScene(scene);
        stage.show();

        // Adjust the position of player 1's UI elements
        System.out.println(player1Name.getWidth());
        imageView1.xProperty().bind(pane.layoutXProperty().add(50));
        imageView1.yProperty().bind(pane.heightProperty().subtract(card1.getHeight() + 50));
        imageView2.xProperty().bind(imageView1.xProperty().add(card2.getWidth() + 25));
        imageView2.yProperty().bind(imageView1.yProperty());
        player1Name.translateXProperty().bind(imageView2.xProperty().subtract((player1Name.getWidth() / 2) + 12.5));
        player1Name.translateYProperty().bind(imageView2.yProperty().subtract(50));

        // Adjust the position of player 2's UI elements
        imageView4.xProperty().bind(pane.widthProperty().subtract(card4.getWidth() + 50));
        imageView4.yProperty().bind(pane.heightProperty().subtract(card4.getHeight() + 50));
        imageView3.xProperty().bind(imageView4.xProperty().subtract(card3.getWidth() + 25));
        imageView3.yProperty().bind(imageView4.yProperty());
        player2Name.translateXProperty().bind(imageView4.xProperty().subtract((player2Name.getWidth() / 2) + 12.5));
        player2Name.translateYProperty().bind(imageView4.yProperty().subtract(50));

        // Adjust the position of the dealer's UI elements
        imageView5.xProperty().bind(pane.widthProperty().divide(2).subtract(card5.getWidth() + 12.5));
        imageView5.setY(50);
        imageView6.xProperty().bind(pane.widthProperty().divide(2).add(12.5));
        imageView6.setY(50);
        dealerName.translateXProperty().bind(imageView6.xProperty().subtract((dealerName.getWidth() / 2) + 12.5));
        dealerName.translateYProperty().bind(imageView6.yProperty().add(card6.getHeight() + (50 - dealerName.getHeight())));

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

        playBlackjack();
    }

    public void playBlackjack() {
        new Thread(() -> {
            try {
                // Start the game session
                dealCards();

                // Start the players' turns
                for (int i = 0; i < numberOfPlayers; i++) {
                    playerTurn = (int) fromServer.readObject();

                    // Take own turn and observer other players' turns
                    if (playerTurn == player) {
                        takeTurn();
                    } else {
                        observerOtherPlayer();
                    }
                }

                // Receive information about the dealer's hand
                receiveDealersHand();

                // Observe moves from the dealer
                takeDealerTurn();

                // Check who won
                checkForWin();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void dealCards() throws IOException, ClassNotFoundException {
        // Contain all players' cards in their own lists
        myHand.add((Card) fromServer.readObject());
        myHand.add((Card) fromServer.readObject());
        otherPlayerHand.add((Card) fromServer.readObject());
        otherPlayerHand.add((Card) fromServer.readObject());
        dealerHand.add((Card) fromServer.readObject());

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
    }

    private void takeTurn() throws IOException, ClassNotFoundException {
        Scanner input = new Scanner(System.in);
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

    private void observerOtherPlayer() throws IOException, ClassNotFoundException {
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

    private void receiveDealersHand() throws IOException, ClassNotFoundException {
        dealerHand.add((Card) fromServer.readObject());
        dealerHandValue += dealerHand.get(0).getValue();
        dealerHandValue += dealerHand.get(1).getValue();
        System.out.println("The dealer's hand is " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                " and " + dealerHand.get(1).getRank() + " of " + dealerHand.get(1).getSuit() +
                ". The current value of his hand is " + dealerHandValue + ".");
    }

    private void takeDealerTurn() throws IOException, ClassNotFoundException {
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
    }

    private void checkForWin() {
        if ((!lost && dealerHandValue == 21) || (!lost && dealerHandValue > handValue && dealerHandValue <= 21)
                || (!lost && dealerHandValue == handValue && dealerHandValue <= 21))
            System.out.println("\nYOU LOSE! THE DEALER WINS!");
        else if ((!lost && dealerHandValue > 21) || (!lost && handValue > dealerHandValue))
            System.out.println("\nYOU WIN!");
        else if (lost && otherPlayerLost)
            System.out.println("THE DEALER BEAT ALL PLAYERS!");
    }

    private int calculateHandValue(ArrayList<Card> cards) {
        int value = 0;

        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            value += card.getValue();
        }

        // Determine Ace value
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