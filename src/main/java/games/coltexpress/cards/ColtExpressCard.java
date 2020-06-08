package games.coltexpress.cards;

import core.components.Card;

import java.util.Objects;

public class ColtExpressCard extends Card {
    public enum CardType {
        MoveSideways,
        CollectMoney,
        Punch,
        MoveMarshal,
        MoveUp,
        Shoot,
        Bullet
    }

    public CardType cardType;
    public final int playerID;

    public ColtExpressCard(int playerID, CardType cardType) {
        super(cardType.toString());
        this.cardType = cardType;
        this.playerID = playerID;
    }

    public ColtExpressCard(int playerID, CardType cardType, int ID) {
        super(cardType.toString(), ID);
        this.cardType = cardType;
        this.playerID = playerID;
    }

    public String toString(){
        return cardType.toString() + "(" + playerID + ")";
    }

    @Override
    public Card copy() {
        return new ColtExpressCard(playerID, cardType, componentID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ColtExpressCard that = (ColtExpressCard) o;
        return playerID == that.playerID &&
                cardType == that.cardType;
    }
}