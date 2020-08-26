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

            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());

            int player = (int)fromServer.readObject();
            System.out.println("Connected to Server. You are player " + player + ".");
            if (player == 1){
                System.out.println("Waiting for other player...");
            }
        }
        catch (IOException | ClassNotFoundException ex){
            ex.printStackTrace();
        }

        new Thread(() -> {
            try {
                System.out.println("test");
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}