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

    private SimulateInfo simulateInfo;

    private Deck<T> drawDeck;

    /**
     * If the game has more than 1 rounds, use this
     */
    private List<Deck<T>> drawDecks;
    private int indexx = 0;

    private String gameResultDesc;

    private String strategy;

    private Long seed;

    public static class SimulateInfo {

        /**
         * Indicate which action we want new user to simulate
         */
        private int beginActionIndex;
        private String isSuccess;
        private String startText;
        private String successText;
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

    public SimulateInfo getSimulateInfo() {
        return simulateInfo;
    }

    public void setSimulateInfo(SimulateInfo simulateInfo) {
        this.simulateInfo = simulateInfo;
    }
}
