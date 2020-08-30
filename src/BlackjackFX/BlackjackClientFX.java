package BlackjackFX;

import Model.Card;
import Model.Ranks;
import UI.MyLabel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

//TODO: lblMessages adjusts position whenever the text inside changes
//TODO: Fix the visual bug that happened when I had 5 cards in hand (last card FOUR of SPADES):
// The fith card did not show up in the GUI, but the sixth and seventh did.
// Also FOUR of SPADES showed up the other client.

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
    private int handScore = 0;
    private int otherPlayerHandScore = 0;
    private int dealerHandScore = 0;
    private int playerTurn;
    private String answer = "";

    private AnchorPane pane;
    private MyLabel lblDealerName;
    private MyLabel lblPlayer1Name;
    private MyLabel lblPlayer2Name;
    private MyLabel lblDealerScore;
    private MyLabel lblPlayer1Score;
    private MyLabel lblPlayer2Score;
    private MyLabel lblMessages;
    private Button btHit;
    private Button btStand;

    private Image genericCard = new Image("image/card/1.png");

    private Image card1;
    private Image card2;
    private Image card3;
    private Image card4;
    private Image card5;
    private Image card6;

    private ImageView imageView1 = new ImageView();
    private ImageView imageView2 = new ImageView();
    private ImageView imageView3 = new ImageView();
    private ImageView imageView4 = new ImageView();
    private ImageView imageView5 = new ImageView();
    private ImageView imageView6 = new ImageView();

    @Override
    public void start(Stage stage) throws Exception {
        pane = new AnchorPane();

        lblDealerName = new MyLabel("Dealer");
        lblPlayer1Name = new MyLabel("Player 1");
        lblPlayer2Name = new MyLabel("Player 2");
        lblMessages = new MyLabel("Messages will appear here...");
        lblDealerName.setFont(new Font("Arial", 24));
        lblPlayer1Name.setFont(new Font("Arial", 24));
        lblPlayer2Name.setFont(new Font("Arial", 24));
        lblDealerScore = new MyLabel(0);
        lblPlayer1Score = new MyLabel(0);
        lblPlayer2Score = new MyLabel(0);
        lblDealerScore.setFont(new Font("Arial", 24));
        lblPlayer1Score.setFont(new Font("Arial", 24));
        lblPlayer2Score.setFont(new Font("Arial", 24));

        lblMessages.setPrefWidth(300);
        lblMessages.setTextAlignment(TextAlignment.CENTER);
        lblMessages.setWrapText(true);

        btHit = new Button("HIT");
        btStand = new Button("STAND");
        btHit.setPrefWidth(75);
        btStand.setPrefWidth(75);
        btHit.setVisible(false);
        btStand.setVisible(false);

        btHit.setOnMouseClicked(e -> {
            btHit.setVisible(false);
            btStand.setVisible(false);
            answer = "hit";
            try {
                toServer.writeObject(answer);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        btStand.setOnMouseClicked(e -> {
            btHit.setVisible(false);
            btStand.setVisible(false);
            answer = "stand";
            try {
                toServer.writeObject(answer);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        pane.getChildren().addAll(lblDealerName, lblPlayer1Name, lblPlayer2Name, imageView1, imageView2, imageView3, imageView4,
                imageView5, imageView6, lblMessages, btHit, btStand, lblDealerScore, lblPlayer1Score, lblPlayer2Score);

        Scene scene = new Scene(pane, 600, 600);
        stage.setTitle("BlackjackFX");
        stage.setMinHeight(600);
        stage.setMinWidth(600);
        stage.setScene(scene);
        stage.show();

        // Adjust the position of player 1's UI elements
        imageView1.xProperty().bind(pane.layoutXProperty().add(120));
        imageView1.yProperty().bind(pane.heightProperty().subtract(genericCard.getHeight() + 50));
        imageView2.xProperty().bind(imageView1.xProperty().add(15));
        imageView2.yProperty().bind(imageView1.yProperty());
        lblPlayer1Name.translateXProperty().bind(imageView1.xProperty().add(lblPlayer1Name.getPrefWidth() / 2));
        lblPlayer1Name.translateYProperty().bind(imageView1.yProperty().subtract(50));
        lblPlayer1Score.translateXProperty().bind(imageView1.xProperty().subtract(lblPlayer1Score.getWidth() + 50));
        lblPlayer1Score.translateYProperty().bind(imageView1.yProperty().add((genericCard.getHeight() / 2) - (lblPlayer1Score.getHeight() / 2)));

        // Adjust the position of player 2's UI elements
        imageView4.xProperty().bind(pane.widthProperty().subtract(genericCard.getWidth() + 120));
        imageView4.yProperty().bind(pane.heightProperty().subtract(genericCard.getHeight() + 50));
        imageView3.xProperty().bind(imageView4.xProperty().subtract(15));
        imageView3.yProperty().bind(imageView4.yProperty());
        lblPlayer2Name.translateXProperty().bind(imageView3.xProperty().add(lblPlayer2Name.getPrefWidth() / 2));
        lblPlayer2Name.translateYProperty().bind(imageView3.yProperty().subtract(50));
        lblPlayer2Score.translateXProperty().bind(imageView3.xProperty().subtract(lblPlayer2Score.getWidth() + 50));
        lblPlayer2Score.translateYProperty().bind(imageView3.yProperty().add((genericCard.getHeight() / 2) - (lblPlayer2Score.getHeight() / 2)));

        // Adjust the position of the dealer's UI elements
        imageView5.xProperty().bind(pane.widthProperty().divide(2).subtract((genericCard.getWidth() / 2) + 7.5));
        imageView5.setY(50);
        imageView6.xProperty().bind(imageView5.xProperty().add(15));
        imageView6.setY(50);
        lblDealerName.translateXProperty().bind(pane.widthProperty().divide(2).subtract(lblDealerName.getWidth() / 2));
        lblDealerName.translateYProperty().bind(imageView6.yProperty().add(genericCard.getHeight() + 25));
        lblDealerScore.translateXProperty().bind(imageView5.xProperty().subtract(lblDealerScore.getWidth() + 50));
        lblDealerScore.translateYProperty().bind(imageView5.yProperty().add((genericCard.getHeight() / 2) - (lblDealerScore.getHeight() / 2)));

        // Adjust the position of other UI elements
        lblMessages.translateXProperty().bind(pane.widthProperty().divide(2).subtract(lblMessages.getPrefWidth() / 2));
        lblMessages.translateYProperty().bind(pane.heightProperty().divide(2).subtract(50));
        btHit.translateXProperty().bind(lblMessages.translateXProperty().add(lblMessages.getPrefWidth() + 30));
        btHit.translateYProperty().bind(pane.heightProperty().divide(2).subtract(btHit.getHeight() + 10));
        btStand.translateXProperty().bind(lblMessages.translateXProperty().add(lblMessages.getPrefWidth() + 30));
        btStand.translateYProperty().bind(pane.heightProperty().divide(2).add(10));

        // TODO: In the future change card 1 and card 3's xProperty dynamically when cards are added to the hand

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
            lblMessages.setText("Welcome to Blackjack!");
            if (player == 1) {
                System.out.println("Waiting for more players...");
                lblMessages.appendText("\nWaiting for more players...");
                lblPlayer1Name.setText("You");
            } else if (player == 2)
                lblPlayer2Name.setText("You");
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

    private void dealCards() throws IOException, ClassNotFoundException, InterruptedException {
        // Contain all players' cards in their own lists
        myHand.add((Card) fromServer.readObject());
        myHand.add((Card) fromServer.readObject());
        otherPlayerHand.add((Card) fromServer.readObject());
        otherPlayerHand.add((Card) fromServer.readObject());
        dealerHand.add((Card) fromServer.readObject());

        playSround("shuffle", 1000);

        // Update cards in the UI
        card1 = new Image("image/card/" + myHand.get(0).getNumber() + ".png");
        card2 = new Image("image/card/" + myHand.get(1).getNumber() + ".png");
        card3 = new Image("image/card/" + otherPlayerHand.get(0).getNumber() + ".png");
        card4 = new Image("image/card/" + otherPlayerHand.get(1).getNumber() + ".png");
        card5 = new Image("image/card/b2fv.png");
        card6 = new Image("image/card/" + dealerHand.get(0).getNumber() + ".png");

        if (player == 1) {
            imageView1.setImage(card1);
            playSround("place", 300);
            imageView2.setImage(card2);
            playSround("place", 300);
            imageView3.setImage(card3);
            playSround("place", 300);
            imageView4.setImage(card4);
            playSround("place", 300);
        } else if (player == 2) {
            imageView1.setImage(card3);
            playSround("place", 300);
            imageView2.setImage(card4);
            playSround("place", 300);
            imageView3.setImage(card1);
            playSround("place", 300);
            imageView4.setImage(card2);
            playSround("place", 300);
        }
        imageView5.setImage(card5);
        playSround("place", 300);
        imageView6.setImage(card6);
        playSround("place", 300);

        // Keep track of the players' hand scores
        for (int i = 0; i < 2; i++) {
            handScore += myHand.get(i).getValue();
            otherPlayerHandScore += otherPlayerHand.get(i).getValue();
        }
        dealerHandScore += dealerHand.get(0).getValue();

        // Update the UI with hand scores
        updateScore(handScore, true, false);
        updateScore(otherPlayerHandScore, false, false);
        updateScore(dealerHandScore, false, true);

        // Print out the current hand status of all players
        System.out.println("You were dealt " + myHand.get(0).getRank() + " of " + myHand.get(0).getSuit() +
                " and " + myHand.get(1).getRank() + " of " + myHand.get(1).getSuit() + ". Value of your hand is " +
                (handScore) + ".");
        System.out.println("The other player was dealth " + otherPlayerHand.get(0).getRank() + " of " +
                otherPlayerHand.get(0).getSuit() + " and " + otherPlayerHand.get(1).getRank() + " of " +
                otherPlayerHand.get(1).getSuit() + ". Value of his hand is " + (otherPlayerHand.get(0).getValue() +
                otherPlayerHand.get(1).getValue()) + ".");
        System.out.println("The dealer drew " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                " and an unknown card. The value of the dealers hand so far is " + (dealerHand.get(0).getValue()) + ".\n");
    }

    private void takeTurn() throws IOException, ClassNotFoundException {
        hitCount = 0;
        answer = "";
        if (handScore < 21) { // here
            System.out.println("Your turn. Do you want to HIT or STAND?");
            Platform.runLater(() -> {
                lblMessages.setText("Your turn. Do you want to HIT or STAND?");
            });

            // Show buttons to allow answers
            btHit.setVisible(true);
            btStand.setVisible(true);

            // Infinite loop waits for a button click
            while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("hit")) {
            }

            while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("bust")) {
                if (answer.toLowerCase().equals("hit")) {
                    // Update the hand
                    hitCount++;
                    Card card = (Card) fromServer.readObject();
                    myHand.add(card);
                    handScore = calculateHandScore(myHand);

                    // Update GUI
                    addCardToGUI(card, myHand.size(), true, false);
                    updateScore(handScore, true, false);

                    System.out.println("You hit " + myHand.get(hitCount + 1).getRank() + " of " +
                            myHand.get(hitCount + 1).getSuit() + ".");
                    Platform.runLater(() -> {
                        lblMessages.setText("You hit " + myHand.get(hitCount + 1).getRank() + " of " +
                                myHand.get(hitCount + 1).getSuit() + ".");
                    });
                    if (handScore <= 21) {
                        System.out.println("The value of your hand is " + (handScore) + ".");
                        System.out.println("Do you want to HIT or STAND?");
                        Platform.runLater(() -> {
                            lblMessages.appendText("\n\nThe value of your hand is " + (handScore) + "."
                                    + "\nDo you want to HIT or STAND?");
                        });
                    } else {
                        System.out.println("You bust! The value of your hand is " + handScore + "!");
                        Platform.runLater(() -> {
                            lblMessages.appendText("\nYou bust! The value of your hand is " + handScore + "!");
                        });
                        answer = "bust";
                        lost = true;
                    }
                } else {
                    System.out.println("Please type hit or stand.");
                    Platform.runLater(() -> {
                        lblMessages.setText("Please type hit or stand");
                    });
                }
                if (!answer.toLowerCase().equals("bust")) {
                    // Show buttons to allow answers
                    btHit.setVisible(true);
                    btStand.setVisible(true);

                    // reset the answer
                    answer = "";

                    // Infinite loop waits for a button click
                    while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("hit")) {
                    }
                } else
                    toServer.writeObject(answer);
            }
            if (answer.toLowerCase().equals("bust")) {
                System.out.println("YOU LOSE!\n");
                Platform.runLater(() -> {
                    lblMessages.appendText("\n\nYOU LOSE!");
                    lblDealerName.appendText(" (WIN)");
                    if (player == 1) {
                        lblPlayer1Name.appendText(" (LOSE)");
                    } else if (player == 2) {
                        lblPlayer2Name.appendText(" (LOSE)");
                    }
                });
            } else {
                System.out.println("You chose to stand.\n");
                Platform.runLater(() -> {
                    lblMessages.setText("You chose to stand.");
                });
            }

        } else {
            System.out.println("You hit natural Blackjack!");
            Platform.runLater(() -> {
                lblMessages.appendText("\n\nYou hit natural Blackjack!");
            });
        }
    }

    private void observerOtherPlayer() throws IOException, ClassNotFoundException {
        System.out.println("Other player's turn.");
        Platform.runLater(() -> {
            lblMessages.setText("Other player's turn.");
        });
        hitCount = 0;
        if (otherPlayerHandScore < 21) {
            String answer = (String) fromServer.readObject();
            while (!answer.toLowerCase().equals("stand") && !answer.toLowerCase().equals("bust")) {
                if (answer.toLowerCase().equals("hit")) {
                    // Update the hand
                    hitCount++;
                    Card card = (Card) fromServer.readObject();
                    otherPlayerHand.add(card);
                    otherPlayerHandScore = calculateHandScore(otherPlayerHand);

                    // Update GUI
                    addCardToGUI(card, otherPlayerHand.size(), false, false);
                    updateScore(otherPlayerHandScore, false, false);

                    System.out.println("The other player hit " + otherPlayerHand.get(hitCount + 1).getRank() + " of " +
                            otherPlayerHand.get(hitCount + 1).getSuit() + ".");
                    Platform.runLater(() -> {
                        lblMessages.setText("The other player hit " + otherPlayerHand.get(hitCount + 1).getRank() + " of " +
                                otherPlayerHand.get(hitCount + 1).getSuit() + ".");
                    });
                    if (otherPlayerHandScore <= 21) {
                        System.out.println("The value of his hand is " + otherPlayerHandScore + ".");
                        Platform.runLater(() -> {
                            lblMessages.appendText("\nThe value of his hand is " + otherPlayerHandScore + ".");
                        });
                    } else {
                        System.out.println("He bust! The value of his hand is " + otherPlayerHandScore + "!");
                        Platform.runLater(() -> {
                            lblMessages.appendText("\n\nHe bust! The value of his hand is " + otherPlayerHandScore + "!");
                            if (player == 1) {
                                lblPlayer2Name.appendText(" (LOSE)");
                            } else if (player == 2) {
                                lblPlayer1Name.appendText(" (LOSE)");
                            }
                        });
                    }
                }
                answer = (String) fromServer.readObject();
            }
            if (answer.toLowerCase().equals("bust")) {
                System.out.println("The other player has lost.\n");
                Platform.runLater(() -> {
                    lblMessages.setText("\nThe other player has lost.");
                });
                otherPlayerLost = true;
            } else {
                System.out.println("The other player chose to stand.\n");
                Platform.runLater(() -> {
                    lblMessages.setText("\nThe other player chose to stand.");
                });
            }
        } else {
            System.out.println("The other player hit natural Blackjack!");
            Platform.runLater(() -> {
                lblMessages.setText("\n\nThe other player hit a natural Blackjack!");
            });
        }
    }

    private void receiveDealersHand() throws IOException, ClassNotFoundException {
        Card card = (Card) fromServer.readObject();
        dealerHand.add(card);
        dealerHandScore += dealerHand.get(1).getValue();

        // Update GUI
        Image cardImage = new Image("image/card/" + card.getNumber() + ".png");
        Platform.runLater(() -> {
            imageView5.setImage(cardImage);
        });
        updateScore(dealerHandScore, false, true);

        System.out.println("The dealer's hand is " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                " and " + dealerHand.get(1).getRank() + " of " + dealerHand.get(1).getSuit() +
                ". The current value of his hand is " + dealerHandScore + ".");
        Platform.runLater(() -> {
            lblMessages.appendText("\n\nThe dealer's hand is " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                    " and " + dealerHand.get(1).getRank() + " of " + dealerHand.get(1).getSuit() +
                    ". The current value of his hand is " + dealerHandScore + ".");
        });
    }

    private void takeDealerTurn() throws IOException, ClassNotFoundException {
        if (dealerHandScore == 21) {
            System.out.println("The dealer has natural Blackjack!");
            Platform.runLater(() -> {
                lblMessages.setText("\n\nThe dealer has natural Blackjack!");
            });
            if (!lost) {
                System.out.println("YOU LOSE!");
                Platform.runLater(() -> {
                    lblMessages.appendText("\n\nYOU LOSE!");
                    if (player == 1) {
                        lblPlayer2Name.appendText(" (LOSE)");
                    } else if (player == 2) {
                        lblPlayer1Name.appendText(" (LOSE)");
                    }
                });
                lost = true;
            }
        }

        if (!lost || !otherPlayerLost) {
            while (dealerHandScore < 17) {
                Card card = (Card) fromServer.readObject();
                dealerHand.add(card);
                dealerHandScore = calculateHandScore(dealerHand);

                // Update GUI
                addCardToGUI(card, dealerHand.size(), false, true);
                updateScore(dealerHandScore, false, true);

                System.out.println("The dealer hit " + card.getRank() + " of " + card.getSuit() + ". The value " +
                        "of his hand is now " + dealerHandScore + ".");
                Platform.runLater(() -> {
                    lblMessages.setText("The dealer hit " + card.getRank() + " of " + card.getSuit() + ". The value " +
                            "of his hand is now " + dealerHandScore + ".");
                });
                if (dealerHandScore > 21) {
                    System.out.println("The dealer bust!");
                    Platform.runLater(() -> {
                        lblMessages.setText("The dealer bust!");
                    });
                }
            }
        }
    }

    private void checkForWin() {
        // Compare own score with dealer's score
        if ((!lost && dealerHandScore == 21) || (!lost && dealerHandScore > handScore && dealerHandScore <= 21)
                || (!lost && dealerHandScore == handScore && dealerHandScore <= 21)) {
            System.out.println("\nYOU LOSE! THE DEALER WINS!");
            Platform.runLater(() -> {
                lblMessages.setText("YOU LOSE! THE DEALER WINS!");
                lblDealerName.appendText(" (WIN)");
                if (player == 1) {
                    lblPlayer1Name.appendText(" (LOSE)");
                } else if (player == 2) {
                    lblPlayer2Name.appendText(" (LOSE)");
                }
            });
        } else if ((!lost && dealerHandScore > 21) || (!lost && handScore > dealerHandScore)) {
            System.out.println("\nYOU WIN!");
            Platform.runLater(() -> {
                lblMessages.setText("YOU WIN!");
                lblDealerName.appendText(" (LOSE)");
                if (player == 1) {
                    lblPlayer1Name.appendText(" (WIN)");
                } else if (player == 2) {
                    lblPlayer2Name.appendText(" (WIN)");
                }
            });
        }

        // Compare the other player's score with the dealer's score
        if ((!otherPlayerLost && dealerHandScore == 21) || (!otherPlayerLost && dealerHandScore > otherPlayerHandScore && dealerHandScore <= 21)
                || (!otherPlayerLost && dealerHandScore == otherPlayerHandScore && dealerHandScore <= 21)) {
            Platform.runLater(() -> {
                if (player == 1) {
                    lblPlayer2Name.appendText(" (LOSE)");
                } else if (player == 2) {
                    lblPlayer1Name.appendText(" (LOSE)");
                }
            });
        } else if ((!otherPlayerLost && dealerHandScore > 21) || (!otherPlayerLost && otherPlayerHandScore > dealerHandScore)) {
            Platform.runLater(() -> {
                if (player == 1) {
                    lblPlayer2Name.appendText(" (WIN)");
                } else if (player == 2) {
                    lblPlayer1Name.appendText(" (WIN)");
                }
            });
        } else if (lost && otherPlayerLost) {
            System.out.println("THE DEALER BEAT ALL PLAYERS!");
            Platform.runLater(() -> {
                lblMessages.setText("THE DEALER BEAT ALL PLAYERS!");
            });
        }
    }

    private int calculateHandScore(ArrayList<Card> cards) {
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

    private void addCardToGUI(Card card, int handSize, boolean myCard, boolean dealerCard) {
        Image cardImage = new Image("image/card/" + card.getNumber() + ".png");
        ImageView cardImageView = new ImageView(cardImage);
        Platform.runLater(() -> {
            pane.getChildren().add(cardImageView);
        });
        if (dealerCard) {
            cardImageView.xProperty().bind(imageView5.xProperty().add(15 * (handSize - 1)));
            cardImageView.yProperty().bind(imageView5.yProperty());
        } else if (player == 1) {
            if (myCard) {
                cardImageView.xProperty().bind(imageView1.xProperty().add(15 * (handSize - 1)));
                cardImageView.yProperty().bind(imageView1.yProperty());
            } else {
                cardImageView.xProperty().bind(imageView3.xProperty().add(15 * (handSize - 1)));
                cardImageView.yProperty().bind(imageView3.yProperty());
            }

        } else if (player == 2) {
            if (myCard) {
                cardImageView.xProperty().bind(imageView3.xProperty().add(15 * (handSize - 1)));
                cardImageView.yProperty().bind(imageView3.yProperty());
            } else {
                cardImageView.xProperty().bind(imageView1.xProperty().add(15 * (handSize - 1)));
                cardImageView.yProperty().bind(imageView1.yProperty());
            }
        }
    }

    private void updateScore(int handScore, boolean myCard, boolean dealerCard) {
        Platform.runLater(() -> {
            if (dealerCard) {
                lblDealerScore.setText(handScore);
            } else if ((player == 1 && myCard) || (player == 2 && !myCard)) {
                lblPlayer1Score.setText(handScore);
            } else if ((player == 2 && myCard) || (player == 1 && !myCard)) {
                lblPlayer2Score.setText(handScore);
            }
        });
    }

    private void playSround(String name, int wait) throws InterruptedException {
        String soundFile = "";
        if (name.equals("place"))
            soundFile = "src/sound/cardPlace1.wav";
        else if (name.equals("shuffle"))
            soundFile = "src/sound/cardFan1.wav";
        Media sound = new Media(new File(soundFile).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
        Thread.sleep(wait);
    }
}