import java.io.Serializable;

public class Card implements Serializable {
    private String value;
    private String suit;

    public Card(String value, String suit){
        this.value = value;
        this.suit = suit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public int getNumericValue(boolean smallAce){
        if (value.equals("Jack") || value.equals("Queen") || value.equals("King"))
            return 10;
        else if (value.equals("Ace")){
            if (smallAce)
                return 1;
            else
                return 11;
        }
        else
            return Integer.parseInt(value);
    }
}
