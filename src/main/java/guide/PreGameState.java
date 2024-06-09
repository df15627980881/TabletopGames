package guide;

import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import utilities.Pair;

import java.util.List;

public class PreGameState {

    private Integer playerCount;

    private List<Pair<Long, AbstractAction>> playerIdAndActions;

    private Deck<FrenchCard> drawDeck;

    private String gameResultDesc;

    private String strategy;

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

    public String getGameResultDesc() {
        return gameResultDesc;
    }

    public void setGameResultDesc(String gameResultDesc) {
        this.gameResultDesc = gameResultDesc;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
