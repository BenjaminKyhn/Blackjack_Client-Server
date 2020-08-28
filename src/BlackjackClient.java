import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class BlackjackClient {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private String host = "localhost";
    private int port = 8015;
    private int player;
    private int numberOfPlayers;

    public static void main(String[] args) {
        new BlackjackClient();
    }

    public BlackjackClient() {
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

                // Keep track of the players' hand values
                for (int i = 0; i < myHand.size(); i++) {
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
                    if (playerTurn == player){
                        hitCount = 0;
                        if (handValue < 21) {
                            System.out.println("Your turn. Do you want to HIT or STAND?");
                            String answer = input.nextLine();
                            toServer.writeObject(answer);
                            while (!answer.toLowerCase().equals("stand")) {
                                if (answer.toLowerCase().equals("hit")) {
                                    hitCount++;
                                    myHand.add((Card) fromServer.readObject());
                                    handValue += myHand.get(hitCount + 1).getValue();
                                    System.out.println("You hit " + myHand.get(hitCount + 1).getRank() + " of " +
                                            myHand.get(hitCount + 1).getSuit() + ". The value of your hand is " + (handValue) + ".");
                                }
                                else
                                    System.out.println("Please type hit or stand.");
                                answer = input.nextLine();
                                toServer.writeObject(answer);
                            }
                            System.out.println("You chose to stand.\n");
                        }
                    }

                    // Observe the other players' turns
                    else {
                        System.out.println("Other player's turn.");
                        hitCount = 0;
                        String answer = (String) fromServer.readObject();
                        while (!answer.toLowerCase().equals("stand")){
                            if (answer.toLowerCase().equals("hit")){
                                hitCount++;
                                otherPlayerHand.add((Card) fromServer.readObject());
                                otherPlayerHandValue += otherPlayerHand.get(hitCount + 1).getValue();
                                System.out.println("The other player hit " + otherPlayerHand.get(hitCount + 1).getRank() + " of " +
                                        otherPlayerHand.get(hitCount + 1).getSuit() + ". The value of his hand is " + (otherPlayerHandValue) + ".");
                            }
                            answer = (String) fromServer.readObject();
                        }
                        System.out.println("The other player chose to stand.\n");
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}