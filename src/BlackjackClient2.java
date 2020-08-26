import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BlackjackClient2 extends Application {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;

    private Button btRegister = new Button("Register to the Server");

    String host = "localhost";

    @Override
    public void start(Stage stage) throws Exception {
        Pane pane = new Pane();
        pane.getChildren().add(btRegister);
        btRegister.setOnAction(new ButtonListener());

        Scene scene = new Scene(pane, 450, 200);
        stage.setTitle("BlackjackClient");
        stage.setScene(scene);
        stage.show();
    }

    private class ButtonListener implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                Socket socket = new Socket(host, 8015);

                fromServer = new ObjectInputStream(socket.getInputStream());
                toServer = new ObjectOutputStream(socket.getOutputStream());
            }
            catch (IOException ex){
                ex.printStackTrace();
            }

            new Thread(() -> {
                try {
                    toServer.writeInt(2);

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
}