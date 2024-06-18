package guide.auto;

import core.Game;
import core.actions.AbstractAction;
import games.loveletter.LoveLetterGameState;
import org.apache.commons.lang3.StringUtils;
import utilities.Pair;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoveLetterGameStrategy implements IGameStrategy {

    public static Map<String, Game> strategyTextAndMechanism = new HashMap<>();

    public static Map<String, Game> strategyTextAndSimulate = new HashMap<>();

    // When you want to add a new strategy, please modify this count
    private final int mechanismStrategyCount = 3;
    private final int simulateStrategyCount = 4;

    private LoveLetterStrategyEnum loveLetterStrategyEnum;

    private static Game gameForMechanism;

    @Override
    public boolean isValid(String strategy, Game game, Long seed) {
        if (StringUtils.isBlank(strategy) || Objects.isNull(game)) {
            return false;
        }

        if (LoveLetterStrategyEnum.MECHANISM.getName().equals(strategy)) {
            this.loveLetterStrategyEnum = LoveLetterStrategyEnum.MECHANISM;
            isMechanism(game);
            return true;
        }

        if (LoveLetterStrategyEnum.SIMULATE.getName().equals(strategy)) {
            this.loveLetterStrategyEnum = LoveLetterStrategyEnum.SIMULATE;
            isSimulate(game, seed);
            return true;
        }

        return false;
    }

    private void isSimulate(Game game, Long seed) {
    }

    private void isMechanism(Game game) {
        // case1: For introducing the role each action
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();
        Map<AbstractAction, Boolean> vis = new HashMap<>();
        for (Pair<Integer, AbstractAction> pair : history) {
            vis.put(pair.b, true);
        }
        if (vis.size() == 6) gameForMechanism = game;
    }

    @Override
    public void exportJson() {

    }

    @Override
    public boolean isEnd() {
        if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.MECHANISM) {
            return Objects.nonNull(gameForMechanism);
        }
        return false;
    }

    public enum LoveLetterStrategyEnum {
        MECHANISM("mechanism"),
        SIMULATE("simulate");

        private final String name;

        LoveLetterStrategyEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class GameResultForJSON implements Serializable {

        @Serial
        private static final long serialVersionUID = -8804626337589489886L;

        private int playerCount;

        private List<GameResultForJSON.Action> actions;

        private String gameResultDesc;

        private GameResultForJSON.Deck deck;

        private String strategy;

        public int getPlayerCount() {
            return playerCount;
        }

        public void setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
        }

        public List<Action> getActions() {
            return actions;
        }

        public void setActions(List<Action> actions) {
            this.actions = actions;
        }

        public String getGameResultDesc() {
            return gameResultDesc;
        }

        public void setGameResultDesc(String gameResultDesc) {
            this.gameResultDesc = gameResultDesc;
        }

        public Deck getDeck() {
            return deck;
        }

        public void setDeck(Deck deck) {
            this.deck = deck;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public static class Action implements Serializable {

            @Serial
            private static final long serialVersionUID = 2637664543223761578L;

            private int player;

            private String action;

            public int getPlayer() {
                return player;
            }

            public void setPlayer(int player) {
                this.player = player;
            }

            public String getAction() {
                return action;
            }

            public void setAction(String action) {
                this.action = action;
            }
        }

        public static class Deck implements Serializable {

            @Serial
            private static final long serialVersionUID = 1324615145762560801L;

            private String name;

            private String visibilityMode;

            private List<GameResultForJSON.Deck.Card> cards;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getVisibilityMode() {
                return visibilityMode;
            }

            public void setVisibilityMode(String visibilityMode) {
                this.visibilityMode = visibilityMode;
            }

            public List<GameResultForJSON.Deck.Card> getCards() {
                return cards;
            }

            public void setCards(List<GameResultForJSON.Deck.Card> cards) {
                this.cards = cards;
            }

            public static class Card implements Serializable {

                @Serial
                private static final long serialVersionUID = 1539693350338665166L;
                private String type;

                private String suite;

                private String number;

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public String getSuite() {
                    return suite;
                }

                public void setSuite(String suite) {
                    this.suite = suite;
                }

                public String getNumber() {
                    return number;
                }

                public void setNumber(String number) {
                    this.number = number;
                }
            }
        }
    }
}
