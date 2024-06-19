package guide.auto;

import core.Game;
import core.actions.AbstractAction;
import core.components.PartialObservableDeck;
import games.loveletter.LoveLetterGameState;
import games.loveletter.actions.PlayCard;
import games.loveletter.cards.LoveLetterCard;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import utilities.JSONUtils;
import utilities.Pair;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class LoveLetterGameStrategy implements IGameStrategy {

    public static Map<String, Game> strategyTextAndMechanism = new HashMap<>();

    public static Map<String, Game> strategyTextAndSimulate = new HashMap<>();

    public static List<PartialObservableDeck<LoveLetterCard>> tmpCardsForReserve;
    public static boolean tmpCardsForReserveSwitch = false;

    // When you want to add a new strategy, please modify this count
    private final int mechanismStrategyCount = 3;
    private final int simulateStrategyCount = 4;

    private LoveLetterStrategyEnum loveLetterStrategyEnum;

    private static Pair<Long, Game> gameForMechanism;

    @Override
    public boolean isValid(String strategy, Game game, Long seed) {
        if (StringUtils.isBlank(strategy) || Objects.isNull(game)) {
            return false;
        }

        if (LoveLetterStrategyEnum.MECHANISM.getName().equals(strategy)) {
            this.loveLetterStrategyEnum = LoveLetterStrategyEnum.MECHANISM;
            isMechanism(game, seed);
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

    private void isMechanism(Game game, Long seed) {
        // case1: For introducing the role each action
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();
        Map<String, Boolean> vis = new HashMap<>();
        int limitActionCount = 0;
        for (Pair<Integer, AbstractAction> pair : history) {
            PlayCard playCard = (PlayCard) pair.b;
            vis.put(playCard.getCardType().name(), true);
            limitActionCount += 1;
            if (limitActionCount >= 20) break;
        }
        System.out.println(vis.size() + " " + history.size());
        if (vis.size() == 8) {
            gameForMechanism = new Pair<>(seed, game);
        }
    }

    @Override
    public void exportJson() {
        assert isEnd();
        String path = "data/preGameState/LoveLetter";
        if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.MECHANISM) {
            path += "/Mechanism";
            File[] allFiles = JSONUtils.getAllFile(path);
            int allFileSize = allFiles == null ? 0 : allFiles.length;
            assert gameForMechanism != null;
            GameResultForJSON gameResultForJSON = new GameResultForJSON();
            gameResultForJSON.setPlayerCount(gameForMechanism.b.getPlayers().size());

            List<Pair<Integer, AbstractAction>> history = gameForMechanism.b.getGameState().getHistory();
            List<GameResultForJSON.Action> actions = new ArrayList<>();
            for (Pair<Integer, AbstractAction> pair : history) {
                GameResultForJSON.Action action = new GameResultForJSON.Action();
                action.setPlayer(pair.a);
                action.setAction((PlayCard) pair.b);
                actions.add(action);
            }
            List<GameResultForJSON.Deck> decks = new ArrayList<>();
            tmpCardsForReserve = tmpCardsForReserve.subList(1, tmpCardsForReserve.size());
            for (PartialObservableDeck<LoveLetterCard> a : tmpCardsForReserve) {
                List<LoveLetterCard> components = new ArrayList<>(a.copy().getComponents());

                GameResultForJSON.Deck deck = new GameResultForJSON.Deck();
                List<GameResultForJSON.Deck.Card> cards = new ArrayList<>();
                for (LoveLetterCard component : components) {
                    GameResultForJSON.Deck.Card card = new GameResultForJSON.Deck.Card();
                    card.setCardType(component.cardType.name());
                    cards.add(card);
                }
                deck.setCards(cards);
                deck.setName(String.valueOf(allFiles == null ? 0 : allFiles.length));
                deck.setVisibilityMode("VISIBLE_TO_ALL");
                decks.add(deck);
            }

            gameResultForJSON.setHistoryText(gameForMechanism.b.getGameState().getHistoryAsText());
            gameResultForJSON.setActions(actions);
            gameResultForJSON.setGameResultDesc("");
            gameResultForJSON.setSeed(gameForMechanism.a);
            gameResultForJSON.setStrategy(null);
            gameResultForJSON.setDecks(decks);

            LoveLetterGameState gs = (LoveLetterGameState) gameForMechanism.b.getGameState();
//            System.out.println(gs.getRoundCounter());
//            Assert.assertEquals(gs.getRoundCounter(), tmpCardsForReserve.size() - 1);

            JSONUtils.writeToJsonFile(gameResultForJSON, path + "/" + allFileSize);

        }

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

        // this can instead deck
        private Long seed;

        private List<GameResultForJSON.Deck> decks;

        private List<String> historyText;

        private String strategy;

        public List<String> getHistoryText() {
            return historyText;
        }

        public void setHistoryText(List<String> historyText) {
            this.historyText = historyText;
        }

        public Long getSeed() {
            return seed;
        }

        public void setSeed(Long seed) {
            this.seed = seed;
        }

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

        public List<Deck> getDecks() {
            return decks;
        }

        public void setDecks(List<Deck> decks) {
            this.decks = decks;
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

            private PlayCard action;

            public int getPlayer() {
                return player;
            }

            public void setPlayer(int player) {
                this.player = player;
            }

            public PlayCard getAction() {
                return action;
            }

            public void setAction(PlayCard action) {
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
                private String cardType;

                public String getCardType() {
                    return cardType;
                }

                public void setCardType(String cardType) {
                    this.cardType = cardType;
                }
            }
        }
    }
}
