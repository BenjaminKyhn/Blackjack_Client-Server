import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class StudentServer {
    private ObjectOutputStream outputToClient;
    private ObjectInputStream inputFromClient;

    public static void main(String[] args) {
        new Thread(() ->{
            new StudentServer();
        }).start();
    }

    public StudentServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8003);
            System.out.println("Server started ");

            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(new HandleASession(socket)).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputFromClient.close();
                outputToClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class HandleASession implements Runnable {
        private Socket socket;

        public HandleASession(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                outputToClient = new ObjectOutputStream(socket.getOutputStream());
                inputFromClient = new ObjectInputStream(socket.getInputStream());

                int clientNo = inputFromClient.readInt();
                System.out.println("Client no: " + clientNo);

                StudentAddress object = (StudentAddress) inputFromClient.readObject();

                outputToClient.writeObject(object);
                System.out.println(object.getName() + " sent back to the client");
            } catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }
}
