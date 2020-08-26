import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> cards;

    public Deck(){
        cards = new ArrayList<>();
        for (Suits suit : Suits.values()){
            for (Ranks rank : Ranks.values()){
                cards.add(new Card(rank, suit));
            }
        }
        shuffle(cards);
    }

    public void shuffle(ArrayList<Card> cards){
        Collections.shuffle(cards);
    }

    public Card draw(){
        Card card = cards.get(0);
        cards.remove(0);
        return card;
    }

    public ArrayList<Card> getCards(){
        return cards;
    }
}
