package guide;

import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import utilities.Pair;

import java.util.List;

public class PreGameState {

    private Long seed;

    private List<Pair<Long, AbstractAction>> playerIdAndActions;

    private Deck<FrenchCard> drawDeck;

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

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
