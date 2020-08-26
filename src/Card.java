import java.io.Serializable;

public class Card implements Serializable {
    private Ranks rank;
    private Suits suit;
    private int value;

    public Card(Ranks rank, Suits suit){
        this.rank = rank;
        this.suit = suit;
        this.value = rank.getValue();
    }

    public Ranks getRank() {
        return rank;
    }

    public void setRank(Ranks rank) {
        this.rank = rank;
    }

    public Suits getSuit() {
        return suit;
    }

    public void setSuit(Suits suit) {
        this.suit = suit;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
