package UI;

import javafx.scene.control.Label;

public class MyLabel extends Label {

    public MyLabel(String text){
        setText(text);
    }

    public void appendText(String newText) {
        setText(getText() + newText);
    }

}