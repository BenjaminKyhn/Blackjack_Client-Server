import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BlackjackClient2 {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private String host = "localhost";
    private int port = 8015;

    public static void main(String[] args) {
        new BlackjackClient2();
    }

    public BlackjackClient2(){
        connectToServer();
    }

    public void connectToServer(){
        try {
            Socket socket = new Socket(host, port);

            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        new Thread(() -> {
            try {
                toServer.writeInt(2);

                Card s = new Card("Ace", "Diamonds");
                toServer.writeObject(s);

                Card card = (Card) fromServer.readObject();

                System.out.println(card.getValue());
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}