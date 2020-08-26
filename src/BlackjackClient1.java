import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BlackjackClient1 {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private String host = "localhost";
    private int port = 8015;

    public static void main(String[] args) {
        new BlackjackClient1();
    }

    public BlackjackClient1(){
        connectToServer();
    }

    public void connectToServer(){
        try {
            Socket socket = new Socket(host, port);

            System.out.println("Connected to server. Waiting for other player...");

            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        new Thread(() -> {
            try {

                toServer.writeInt(1);

                Card s = new Card("Queen", "Diamonds");
                toServer.writeObject(s);

                Card card = (Card) fromServer.readObject();

                System.out.println(card.getValue());
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}