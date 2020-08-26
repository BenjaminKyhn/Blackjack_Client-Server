import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class BlackjackClient1 {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private String host = "localhost";
    private int port = 8015;

    public static void main(String[] args) {
        new BlackjackClient1();
    }

    public BlackjackClient1() {
        connectToServer();
    }

    public void connectToServer() {
        try {
            Socket socket = new Socket(host, port);

            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());

            int player = (int) fromServer.readObject();
            System.out.println("Connected to Server. You are player " + player + ".");
            if (player == 1) {
                System.out.println("Waiting for other player...\n");
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        new Thread(() -> {
            try {
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
                for (int i = 0; i < myHand.size(); i++) {
                    handValue += myHand.get(i).getValue();
                }

                System.out.println("You were dealt " + myHand.get(0).getRank() + " of " + myHand.get(1).getSuit() +
                        " and " + myHand.get(1).getRank() + " of " + myHand.get(1).getSuit() + ". Value of your hand is " +
                        (handValue) + ".");
                System.out.println("The other player was dealth " + otherPlayerHand.get(0).getRank() + " of " +
                        otherPlayerHand.get(0).getSuit() + " and " + otherPlayerHand.get(1).getRank() + " of " +
                        otherPlayerHand.get(1).getSuit() + ". Value of his hand is " + (otherPlayerHand.get(0).getValue() +
                        otherPlayerHand.get(1).getValue()) + ".");
                System.out.println("The dealer drew " + dealerHand.get(0).getRank() + " of " + dealerHand.get(0).getSuit() +
                        " and an unknown card. The value of the dealers hand so far is " + (dealerHand.get(0).getValue()) + ".\n");

                Scanner input = new Scanner(System.in);

                if (handValue < 21) {
                    System.out.println("Do you want to HIT or STAND?");
                    String answer = "";
                    while (true) {
                        answer = input.nextLine();
                        if (answer.toLowerCase().equals("hit") || answer.toLowerCase().equals("stand"))
                            break;
                    }
                    toServer.writeObject(answer);
                    if (answer.toLowerCase().equals("hit")){
                        hitCount++;
                        myHand.add((Card) fromServer.readObject());
                        handValue += myHand.get(hitCount + 1).getValue();
                        System.out.println("You hit " + myHand.get(hitCount + 1).getRank() + " of " +
                                myHand.get(hitCount + 1).getSuit() + ". The value of your hand is " + (handValue));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}