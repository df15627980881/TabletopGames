package guide;

import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import utilities.Pair;

import java.util.List;

public class PreGameState {

    private List<Pair<Long, AbstractAction>> playerIdAndActions;

    private Deck<FrenchCard> drawDeck;

    public List<Pair<Long, AbstractAction>> getPlayerIdAndActions() {
        return playerIdAndActions;
    }

    public void setPlayerIdAndActions(List<Pair<Long, AbstractAction>> playerIdAndActions) {
        this.playerIdAndActions = playerIdAndActions;
    }

    public Deck<FrenchCard> getDrawDeck() {
        return drawDeck;
    }

    public void setDrawDeck(Deck<FrenchCard> drawDeck) {
        this.drawDeck = drawDeck;
    }
}
