package guide;

import core.actions.AbstractAction;
import core.components.Card;
import core.components.Deck;
import core.components.FrenchCard;
import utilities.Pair;

import java.util.List;

public class PreGameState<T extends Card> {

    private Integer playerCount;

    private List<Pair<Long, AbstractAction>> playerIdAndActions;

    private Deck<T> drawDeck;

    /**
     * If the game has more than 1 rounds, use this
     */
    private List<Deck<T>> drawDecks;
    private int indexx = 0;

    private String gameResultDesc;

    private String strategy;

    private Long seed;

    public List<Pair<Long, AbstractAction>> getPlayerIdAndActions() {
        return playerIdAndActions;
    }

    public void setPlayerIdAndActions(List<Pair<Long, AbstractAction>> playerIdAndActions) {
        this.playerIdAndActions = playerIdAndActions;
    }

    public Deck<T> getDrawDeck() {
        return drawDeck;
    }

    public void setDrawDeck(Deck<T> drawDeck) {
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

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public List<Deck<T>> getDrawDecks() {
        return drawDecks;
    }

    public void setDrawDecks(List<Deck<T>> drawDecks) {
        this.drawDecks = drawDecks;
    }

    public int getIndexx() {
        return indexx;
    }

    public void setIndexx(int indexx) {
        this.indexx = indexx;
    }

    public void addIndexx() {
        this.indexx += 1;
        this.indexx %= drawDecks.size();
    }

    public void resetIndexx() {
        this.setIndexx(0);
    }
}
