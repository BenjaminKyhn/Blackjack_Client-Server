import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class BlackjackServer implements BlackjackConstants {
    private static int sessionNo = 1;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(8010);
                System.out.println("Server startet at socket " + server.getLocalPort());

                while (true) {
                    System.out.println("Wait for players to join the game session...");

                    // Connect the dealer
                    Socket dealer = server.accept();
                    System.out.println(new Date() + ": The dealer joined session " + sessionNo);
                    System.out.println("The dealer's IP address is " + dealer.getInetAddress());
                    new DataOutputStream(dealer.getOutputStream()).writeInt(DEALER);

                    // Connect player 1
                    Socket player1 = server.accept();
                    System.out.println(new Date() + ": Player 1 joined session " + sessionNo);
                    System.out.println("Player 1's IP address is " + player1.getInetAddress());
                    new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

                    System.out.println(new Date() + ": Starting a session for session " + sessionNo++);
                    new Thread(new HandleASession(dealer, player1)).start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    static class HandleASession implements Runnable, BlackjackConstants {
        private Socket dealer;
        private Socket player1;

        public HandleASession(Socket dealer, Socket player1) {
            this.dealer = dealer;
            this.player1 = player1;
        }

        @Override
        public void run() {
            try {
                DataInputStream fromDealer = new DataInputStream(dealer.getInputStream());
                DataOutputStream toDealer = new DataOutputStream(dealer.getOutputStream());
                DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
                ObjectInputStream objectFromDealer = new ObjectInputStream(dealer.getInputStream());
                ObjectOutputStream objectToDealer = new ObjectOutputStream(dealer.getOutputStream());
                ObjectInputStream objectFromPlayer1 = new ObjectInputStream(player1.getInputStream());
                ObjectOutputStream objectToPlayer1 = new ObjectOutputStream(player1.getOutputStream());

                toDealer.writeInt(1);

                ArrayList<Card> deck = getDeckOfCards();

                ArrayList<Card> dealerCards = new ArrayList<>();
                ArrayList<Card> playerCards = new ArrayList<>();

                for (int i = 0; i < 4; i++) {
                    Card card = deck.get((int)(Math.random() * 52));
                    if (deck.contains(card) && i < 2){
                        dealerCards.add(card);
                        deck.remove(card);
                    }
                    else if (deck.contains(card)){
                        playerCards.add(card);
                        deck.remove(card);
                    }
                }

                System.out.println("Deck:");
                for (int i = 0; i < deck.size(); i++) {
                    System.out.println(deck.get(i));
                }

                System.out.println("Dealer's cards:");
                for (int i = 0; i < dealerCards.size(); i++) {
                    System.out.println(dealerCards.get(i));
                }

                System.out.println("Player's cards:");
                for (int i = 0; i < playerCards.size(); i++) {
                    System.out.println(playerCards.get(i));
                }

                objectToDealer.writeObject(dealerCards.get(0));
                objectToDealer.writeObject(dealerCards.get(1));
                objectToPlayer1.writeObject(playerCards.get(0));
                objectToDealer.writeObject(playerCards.get(1));

                while (true){

                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        private static ArrayList<Card> getDeckOfCards(){
            ArrayList<Card> deck = new ArrayList<>();
            deck.add(new Card("2", "Clubs"));
            deck.add(new Card("3", "Clubs"));
            deck.add(new Card("4", "Clubs"));
            deck.add(new Card("5", "Clubs"));
            deck.add(new Card("6", "Clubs"));
            deck.add(new Card("7", "Clubs"));
            deck.add(new Card("8", "Clubs"));
            deck.add(new Card("9", "Clubs"));
            deck.add(new Card("10", "Clubs"));
            deck.add(new Card("Jack", "Clubs"));
            deck.add(new Card("Queen", "Clubs"));
            deck.add(new Card("King", "Clubs"));
            deck.add(new Card("Ace", "Clubs"));

            deck.add(new Card("2", "Diamonds"));
            deck.add(new Card("3", "Diamonds"));
            deck.add(new Card("4", "Diamonds"));
            deck.add(new Card("5", "Diamonds"));
            deck.add(new Card("6", "Diamonds"));
            deck.add(new Card("7", "Diamonds"));
            deck.add(new Card("8", "Diamonds"));
            deck.add(new Card("9", "Diamonds"));
            deck.add(new Card("10", "Diamonds"));
            deck.add(new Card("Jack", "Diamonds"));
            deck.add(new Card("Queen", "Diamonds"));
            deck.add(new Card("King", "Diamonds"));
            deck.add(new Card("Ace", "Diamonds"));

            deck.add(new Card("2", "Hearts"));
            deck.add(new Card("3", "Hearts"));
            deck.add(new Card("4", "Hearts"));
            deck.add(new Card("5", "Hearts"));
            deck.add(new Card("6", "Hearts"));
            deck.add(new Card("7", "Hearts"));
            deck.add(new Card("8", "Hearts"));
            deck.add(new Card("9", "Hearts"));
            deck.add(new Card("10", "Hearts"));
            deck.add(new Card("Jack", "Hearts"));
            deck.add(new Card("Queen", "Hearts"));
            deck.add(new Card("King", "Hearts"));
            deck.add(new Card("Ace", "Hearts"));

            deck.add(new Card("2", "Spades"));
            deck.add(new Card("3", "Spades"));
            deck.add(new Card("4", "Spades"));
            deck.add(new Card("5", "Spades"));
            deck.add(new Card("6", "Spades"));
            deck.add(new Card("7", "Spades"));
            deck.add(new Card("8", "Spades"));
            deck.add(new Card("9", "Spades"));
            deck.add(new Card("10", "Spades"));
            deck.add(new Card("Jack", "Spades"));
            deck.add(new Card("Queen", "Spades"));
            deck.add(new Card("King", "Spades"));
            deck.add(new Card("Ace", "Spades"));

            return deck;
        }
    }
}
