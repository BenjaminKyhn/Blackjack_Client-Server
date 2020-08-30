package UI;

import javafx.scene.control.Label;

public class MyLabel extends Label {

    public MyLabel(String text){
        setText(text);
    }

    public MyLabel(int number){
        setText(String.valueOf(number));
    }

    public void appendText(String newText) {
        setText(getText() + newText);
    }

    public void setText(int number){
        setText(String.valueOf(number));
    }

}