package guide.auto;

import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import games.blackjack.BlackjackGameState;
import org.apache.commons.lang3.StringUtils;
import utilities.JSONUtils;
import utilities.Pair;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class BlackjackGameStrategy implements IGameStrategy {

    public static Map<String, Game> strategyTextAndGameResults = new HashMap<>();

    public static Map<String, Game> strategyTextAndSimulate = new HashMap<>();

    private final String gameResultStrategyText01 = "When the dealer's total number of cards is the highest in the game and doesn't bust, the dealer wins and the remaining players lose the game.";
    private final String gameResultStrategyText02 = "When the dealer's hand has not busted, the player with the highest score who has also not busted wins. If the players tie for the dealer's score, then they draw.";
    private final String gameResultStrategyText03 = "When the dealer busts, all players who have not busted win.";

    public BlackjackStrategyEnum strategyEnum;

    @Override
    public boolean isValid(String strategy, Game game) {
        if (StringUtils.isBlank(strategy) || Objects.isNull(game)) {
            return false;
        }

        if (BlackjackStrategyEnum.GAME_RESULT.getName().equals(strategy)) {
            this.strategyEnum = BlackjackStrategyEnum.GAME_RESULT;
            isGameResult(game);
            return true;
        }
        
        if (BlackjackStrategyEnum.SIMULATE.getName().equals(strategy)) {
            this.strategyEnum = BlackjackStrategyEnum.SIMULATE;
            isSimulate(game);
            return true;
        }

        return false;
    }

    private void isSimulate(Game game) {
        BlackjackGameState gs = (BlackjackGameState) game.getGameState();
        List<GameResultForJSON.Deck.Card> cards = new ArrayList<>();
        Deck<FrenchCard> allDeck = FrenchCard.generateDeck("DrawDeck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        //shuffle the cards
        allDeck.shuffle(new Random((gs.getGameParameters().getRandomSeed())));

        // case1: When the dealer's upcard is a good one, a 7, 8, 9, 10-card, or ace for example, the player should not stop drawing until a total of 17 or more is reached.

    }

    @Override
    public void exportJson() {
        String path = "data/preGameState/Blackjack";
        if (strategyEnum == BlackjackStrategyEnum.GAME_RESULT) {
            path += "/GameResult";
            File[] allFiles = JSONUtils.getAllFile(path);
            int allFileSize = allFiles == null ? 0 : allFiles.length;
            assert strategyTextAndGameResults.size() == 3;
            for (Map.Entry<String, Game> entry : strategyTextAndGameResults.entrySet()) {
                Game game = entry.getValue();
                BlackjackGameState gs = (BlackjackGameState) game.getGameState();
                GameResultForJSON gameResultForJSON = new GameResultForJSON();
                gameResultForJSON.setPlayerCount(gs.getNPlayers());
                gameResultForJSON.setGameResultDesc(entry.getKey());

                List<GameResultForJSON.Action> actions = new ArrayList<>();
                for (Pair<Integer, AbstractAction> pair : gs.getHistory()) {
                    GameResultForJSON.Action action = new GameResultForJSON.Action();
                    action.setPlayer(pair.a);
                    action.setAction(pair.b.toString());
                    actions.add(action);
                }
                gameResultForJSON.setActions(actions);

                GameResultForJSON.Deck deck = new GameResultForJSON.Deck();
                deck.setName(String.valueOf(allFiles == null ? 0 : allFiles.length));
                deck.setVisibilityMode("VISIBLE_TO_ALL");

                List<GameResultForJSON.Deck.Card> cards = new ArrayList<>();
                Deck<FrenchCard> allDeck = FrenchCard.generateDeck("DrawDeck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
                //shuffle the cards
                allDeck.shuffle(new Random((gs.getGameParameters().getRandomSeed())));
                for (FrenchCard frenchCard : allDeck.getComponents()) {
                    GameResultForJSON.Deck.Card card = new GameResultForJSON.Deck.Card();
                    card.setSuite(frenchCard.suite.name());
                    if (FrenchCard.FrenchCardType.Number == frenchCard.type)
                        card.setNumber(String.valueOf(frenchCard.number));
                    card.setType(frenchCard.type.name());
                    cards.add(card);
                }
                assert cards.size() == 52;
                deck.setCards(cards);
                gameResultForJSON.setDeck(deck);

                JSONUtils.writeToJsonFile(gameResultForJSON, path + "/" + allFileSize++);
            }
        }
    }

    @Override
    public boolean isEnd() {
        if (this.strategyEnum == BlackjackStrategyEnum.GAME_RESULT) {
            return strategyTextAndGameResults.size() == 3;
        }
        return false;
    }

    private void isGameResult(Game game) {
        BlackjackGameState gs = (BlackjackGameState) game.getGameState();
        CoreConstants.GameResult[] playerResults = gs.getPlayerResults();
        List<PartialObservableDeck<FrenchCard>> playerDecks = gs.getPlayerDecks();
        int nPlayer = gs.getNPlayers();
        int[] handSum = new int[nPlayer];
        for (int i=0; i<nPlayer; ++i) {
            PartialObservableDeck<FrenchCard> deck = playerDecks.get(i);
            List<FrenchCard> cards = deck.getComponents();
            int sum = 0;
            for (FrenchCard card : cards) {
                if (card.type != FrenchCard.FrenchCardType.Number)
                    sum += 10;
                else
                    sum += card.number;
            }
            handSum[i] = sum;
        }

        int nNotBustPlayerExcludeDealer = 0;
        for (int i=0; i<nPlayer-1; ++i) {
            if (handSum[i] > 21) nNotBustPlayerExcludeDealer += 1;
        }

        // case1: When the dealer's total number of cards is the highest in the game and doesn't bust, the dealer wins and the remaining players lose the game.
        if (playerResults[nPlayer-1] == CoreConstants.GameResult.WIN_GAME) {
            if (nNotBustPlayerExcludeDealer != nPlayer - 1 && nNotBustPlayerExcludeDealer != 0) {
                strategyTextAndGameResults.put(gameResultStrategyText01, game);
                return;
            }
        }

        // case2: When the dealer's hand has not busted, the player with the highest score who has also not busted wins. If the players tie for the dealer's score, then they draw.
        if (handSum[nPlayer-1] <= 21 && playerResults[nPlayer-1] == CoreConstants.GameResult.LOSE_GAME) {
            int maxScore = Arrays.stream(handSum).max().getAsInt();
            int nCountDraw = (int) Arrays.stream(handSum).filter(x -> x == maxScore).count();
            if (nCountDraw > 1) {
                strategyTextAndGameResults.put(gameResultStrategyText02, game);
                return;
            }
        }

        // case3: When the dealer busts, all players who have not busted win.
        if (handSum[nPlayer-1] > 21 && nNotBustPlayerExcludeDealer > 0 && nNotBustPlayerExcludeDealer < nPlayer - 1) {
            strategyTextAndGameResults.put(gameResultStrategyText03, game);
            return;
        }
    }

    public void setStrategyEnum(guide.auto.BlackjackGameStrategy.BlackjackStrategyEnum strategyEnum) {
        this.strategyEnum = strategyEnum;
    }

    public enum BlackjackStrategyEnum {
        GAME_RESULT("gameResult"),
        MECHANISM("mechanism"),
        SIMULATE("simulate");

        private final String name;

        BlackjackStrategyEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class GameResultForJSON implements Serializable {

        @Serial
        private static final long serialVersionUID = 1790523706257500890L;

        private int playerCount;

        private List<Action> actions;

        private String gameResultDesc;

        private Deck deck;

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
            private static final long serialVersionUID = 5325297381677152836L;

            private String name;

            private String visibilityMode;

            private List<Card> cards;

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

            public List<Card> getCards() {
                return cards;
            }

            public void setCards(List<Card> cards) {
                this.cards = cards;
            }

            public static class Card implements Serializable {

                private static final long serialVersionUID = -2903022719017140496L;

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
