package guide;

import core.actions.AbstractAction;
import core.components.Card;
import core.components.Deck;
import core.components.FrenchCard;
import utilities.Pair;

import java.util.List;

/**
 * This class is a mapping class for JSON files, storing all the information of JSON games.
 * @param <T>
 */
public class PreGameState<T extends Card> {

    private Integer playerCount;

    // <0, Stand>, <1, Hit> ...
    private List<Pair<Long, AbstractAction>> playerIdAndActions;

    private SimulateInfo simulateInfo;

    /**
     * store the order of each card
     */
    private Deck<T> drawDeck;

    /**
     * If the game has more than 1 rounds, use this
     */
    private List<Deck<T>> drawDecks;
    /**
     * If the game has more than 1 rounds, this variable represents the game is in which round
     */
    private int indexx = 0;

    /**
     * used in Game Result module for introducing.
     * e.g. When the dealer busts, all players who have not busted win.
     */
    private String gameResultDesc;

    private Long seed;

    /**
     * This class stores information relevant for game simulation, allowing the code to execute to a specified state for user practice.
     */
    public static class SimulateInfo {

        /**
         * The code displays to the user after executing up to which Action
         */
        private int beginActionIndex;
        /**
         * The name of the callback method, called for validation when the novice is in the simulation module.
         */
        private String isSuccess;
        /**
         * Before simulating, what strategy text displays
         */
        private String startText;
        /**
         * When novices success in challenge, what text displays
         */
        private String successText;
        /**
         * When novices fail in challenge, what text displays
         */
        private String failText;
        private List<String> players;

        public int getBeginActionIndex() {
            return beginActionIndex;
        }

        public void setBeginActionIndex(int beginActionIndex) {
            this.beginActionIndex = beginActionIndex;
        }

        public String getIsSuccess() {
            return isSuccess;
        }

        public void setIsSuccess(String isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getStartText() {
            return startText;
        }

        public void setStartText(String startText) {
            this.startText = startText;
        }

        public String getSuccessText() {
            return successText;
        }

        public void setSuccessText(String successText) {
            this.successText = successText;
        }

        public String getFailText() {
            return failText;
        }

        public void setFailText(String failText) {
            this.failText = failText;
        }

        public List<String> getPlayers() {
            return players;
        }

        public void setPlayers(List<String> players) {
            this.players = players;
        }
    }

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

    public SimulateInfo getSimulateInfo() {
        return simulateInfo;
    }

    public void setSimulateInfo(SimulateInfo simulateInfo) {
        this.simulateInfo = simulateInfo;
    }
}
