package Model;

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
    }

    public synchronized void shuffle(){
        Collections.shuffle(cards);
    }

    public synchronized Card draw(){
        Card card = cards.get(0);
        cards.remove(0);
        return card;
    }

    public ArrayList<Card> getCards(){
        return cards;
    }
}
