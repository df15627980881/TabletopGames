package guide.auto;

import core.AbstractGameState;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import games.blackjack.BlackjackGameState;
import games.blackjack.actions.Hit;
import games.blackjack.actions.Stand;
import guide.PreGameState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import utilities.JSONUtils;
import utilities.Pair;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class BlackjackGameStrategy implements IGameStrategy {

    public static Map<String, Game> strategyTextAndGameResults = new HashMap<>();

    public static Map<String, Pair<PreGameState.SimulateInfo, Game>> strategyTextAndSimulate = new HashMap<>();

    // When you want to add a new strategy, please modify this count
    private final int gameResultStrategyCount = 3;
    private final int simulateStrategyCount = 4;

    private final String gameResultStrategyText01 = "When the dealer's total number of cards is the highest in the game and doesn't bust, the dealer wins and the remaining players lose the game.";
    private final String gameResultStrategyText02 = "When the dealer's hand has not busted, the player with the highest score who has also not busted wins. If the players tie for the dealer's score, then they draw.";
    private final String gameResultStrategyText03 = "When the dealer busts, all players who have not busted win.";

    private final String simulateStrategyText01 = "When the dealer's upcard is a good one, a 7, 8, 9, 10-card, or ace for example, the player should not stop drawing until a total of 17 or more is reached.";

    private final String simulateStrategyText02 = "When the dealer's upcard is a poor one, 4, 5, or 6, the player should stop drawing as soon as he gets a total of 12 or higher. The strategy here is never to take a card if there is any chance of going bust. The desire with this poor holding is to let the dealer hit and hopefully go over 21.";

    private final String simulateStrategyText03 = "When the dealer's up card is a fair one, 2 or 3, the player should stop with a total of 13 or higher.";

    private final String simulateStrategyText04 = "With a soft hand, the general strategy is to keep hitting until a total of at least 18 is reached. Thus, with an ace and a six (7 or 17), the player would not stop at 17, but would hit.";

    public BlackjackStrategyEnum strategyEnum;

    @Override
    public boolean isValid(String strategy, Game game, Long seed) {
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
            isSimulate(game, seed);
            return true;
        }

        return false;
    }

    private void isSimulate(Game game, Long seed) {

        PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
        simulateInfo.setBeginActionIndex(0);
        simulateInfo.setIsSuccess("method1");
        simulateInfo.setStartText("Now, here are some common playing strategies recommended to you, please try to have two players win the dealer at the same time!");
        simulateInfo.setSuccessText("Congratulations! It seems you have mastered this strategy.");
        simulateInfo.setFailText("Oops! You can try it again.");
        List<String> players = new ArrayList<>();
        for (int i=0; i<game.getGameState().getNPlayers()-1; ++i) {
            players.add("HumanGUIPlayer");
        }
        players.add("MCTS");
        simulateInfo.setPlayers(players);
        
        BlackjackGameState gs = (BlackjackGameState) game.getGameState();
        Deck<FrenchCard> allDeck = FrenchCard.generateDeck("DrawDeck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        //shuffle the cards
        allDeck.shuffle(new Random((gs.getGameParameters().getRandomSeed())));

        Game initGame = resetActionForGame(game, seed);
        gs = (BlackjackGameState) initGame.getGameState();
        List<PartialObservableDeck<FrenchCard>> playerDecks = gs.getPlayerDecks();
        // case1: When the dealer's upcard is a good one, a 7, 8, 9, 10-card, or ace for example, the player should not stop drawing until a total of 17 or more is reached.
        FrenchCard frenchCard = playerDecks.get(gs.getDealerPlayer()).getComponents().get(1);
        if (frenchCard.type != FrenchCard.FrenchCardType.Number || (frenchCard.number >= 7 && frenchCard.number <= 10)) {
            for (int i=0; i<gs.getNPlayers()-1; ++i) {
                List<FrenchCard> components = gs.getPlayerDecks().get(i).getComponents();
                assert components != null && components.size() == 2;
                int sum = calDeckSum(components);
                while (sum < 17) {
                    initGame.processOneAction(new Hit(i, true, false));
                    gs = (BlackjackGameState) initGame.getGameState();
                    playerDecks = gs.getPlayerDecks();
                    components = playerDecks.get(i).getComponents();
                    sum = calDeckSum(components);
                }
                // The program is designed with when sum is equal to 21, player is no need to do Stand Action.
                if (sum < 21) {
                    initGame.processOneAction(new Stand());
                    gs = (BlackjackGameState) initGame.getGameState();
                    playerDecks = gs.getPlayerDecks();
                    components = playerDecks.get(i).getComponents();
                    sum = calDeckSum(components);
                }
            }
            gs = (BlackjackGameState) initGame.getGameState();
            while (calDeckSum(gs.getPlayerDecks().get(gs.getDealerPlayer()).getComponents()) < 17 && gs.getGameStatus() != CoreConstants.GameResult.GAME_END) {
                initGame.processOneAction(new Hit(gs.getDealerPlayer(), true, false));
                gs = (BlackjackGameState) initGame.getGameState();
            }
            assert gs.getGameStatus() == CoreConstants.GameResult.GAME_END;
            boolean v = gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
            for (int i = 0; i < gs.getPlayerResults().length - 1; i++) {
                v &= gs.getPlayerResults()[i] == CoreConstants.GameResult.WIN_GAME;
            }
            if (v && !strategyTextAndSimulate.containsKey(simulateStrategyText01)) {
                for (Pair<Integer, AbstractAction> pair : gs.getHistory()) {
                    System.out.println("1: " + pair.a + pair.b);
                }
                assert getWinGameCount(gs.getPlayerResults()) == gs.getNPlayers() - 1 && gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
                simulateInfo.setStartText(gameResultStrategyText01);
                strategyTextAndSimulate.put(simulateStrategyText01, new Pair<>(simulateInfo,game));
                return;
            } else {
                // reset initGame again
                initGame = resetActionForGame(game, seed);
                gs = (BlackjackGameState) initGame.getGameState();
            }
        }

        // case2: When the dealer's upcard is a poor one, 4, 5, or 6, the player should stop drawing as soon as he gets a total of 12 or higher. The strategy here is never to take a card if there is any chance of going bust. The desire with this poor holding is to let the dealer hit and hopefully go over 21.
        if (frenchCard.type == FrenchCard.FrenchCardType.Number && frenchCard.number >= 4 && frenchCard.number <= 6) {
            for (int i=0; i<gs.getNPlayers()-1; ++i) {
                List<FrenchCard> components = gs.getPlayerDecks().get(i).getComponents();
                assert components != null && components.size() == 2;
                int sum = calDeckSum(components);
                while (sum < 12) {
                    initGame.processOneAction(new Hit(i, true, false));
                    gs = (BlackjackGameState) initGame.getGameState();
                    playerDecks = gs.getPlayerDecks();
                    components = playerDecks.get(i).getComponents();
                    sum = calDeckSum(components);
                }
                // The program is designed with when sum is equal to 21, player is no need to do Stand Action.
                if (sum < 21) {
                    initGame.processOneAction(new Stand());
                    gs = (BlackjackGameState) initGame.getGameState();
                    playerDecks = gs.getPlayerDecks();
                    components = playerDecks.get(i).getComponents();
                    sum = calDeckSum(components);
                }
            }
            gs = (BlackjackGameState) initGame.getGameState();
            while (calDeckSum(gs.getPlayerDecks().get(gs.getDealerPlayer()).getComponents()) < 17 && gs.getGameStatus() != CoreConstants.GameResult.GAME_END) {
                initGame.processOneAction(new Hit(gs.getDealerPlayer(), true, false));
                gs = (BlackjackGameState) initGame.getGameState();
            }
            assert gs.getGameStatus() == CoreConstants.GameResult.GAME_END;
            boolean v = gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
            for (int i = 0; i < gs.getPlayerResults().length - 1; i++) {
                v &= gs.getPlayerResults()[i] == CoreConstants.GameResult.WIN_GAME;
            }
            if (v && !strategyTextAndSimulate.containsKey(simulateStrategyText02)) {
                for (Pair<Integer, AbstractAction> pair : gs.getHistory()) {
                    System.out.println("2: " + pair.a + pair.b);
                }
                assert getWinGameCount(gs.getPlayerResults()) == gs.getNPlayers() - 1 && gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
                simulateInfo.setStartText(simulateStrategyText02);
                strategyTextAndSimulate.put(simulateStrategyText02, new Pair<>(simulateInfo,game));
                return;
            } else {
                // reset initGame again
                initGame = resetActionForGame(game, seed);
                gs = (BlackjackGameState) initGame.getGameState();
            }
        }

        // Case3: When the dealer's up card is a fair one, 2 or 3, the player should stop with a total of 13 or higher.
        if (frenchCard.type == FrenchCard.FrenchCardType.Number && frenchCard.number >= 2 && frenchCard.number <= 3) {
            for (int i=0; i<gs.getNPlayers()-1; ++i) {
                List<FrenchCard> components = gs.getPlayerDecks().get(i).getComponents();
                assert components != null && components.size() == 2;
                int sum = calDeckSum(components);
                while (sum < 13) {
                    initGame.processOneAction(new Hit(i, true, false));
                    gs = (BlackjackGameState) initGame.getGameState();
                    playerDecks = gs.getPlayerDecks();
                    components = playerDecks.get(i).getComponents();
                    sum = calDeckSum(components);
                }
                // The program is designed with when sum is equal to 21, player is no need to do Stand Action.
                if (sum < 21) {
                    initGame.processOneAction(new Stand());
                    gs = (BlackjackGameState) initGame.getGameState();
                    playerDecks = gs.getPlayerDecks();
                    components = playerDecks.get(i).getComponents();
                    sum = calDeckSum(components);
                }
            }
            gs = (BlackjackGameState) initGame.getGameState();
            while (calDeckSum(gs.getPlayerDecks().get(gs.getDealerPlayer()).getComponents()) < 17 && gs.getGameStatus() != CoreConstants.GameResult.GAME_END) {
                initGame.processOneAction(new Hit(gs.getDealerPlayer(), true, false));
                gs = (BlackjackGameState) initGame.getGameState();
            }
            assert gs.getGameStatus() == CoreConstants.GameResult.GAME_END;
            boolean v = gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
            for (int i = 0; i < gs.getPlayerResults().length - 1; i++) {
                v &= gs.getPlayerResults()[i] == CoreConstants.GameResult.WIN_GAME;
            }
            if (v && !strategyTextAndSimulate.containsKey(simulateStrategyText03)) {
                for (Pair<Integer, AbstractAction> pair : gs.getHistory()) {
                    System.out.println("3: " + pair.a + pair.b);
                }
                assert getWinGameCount(gs.getPlayerResults()) == gs.getNPlayers() - 1 && gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
                simulateInfo.setStartText(simulateStrategyText03);
                strategyTextAndSimulate.put(simulateStrategyText03, new Pair<>(simulateInfo,game));
                return;
            } else {
                // reset initGame again
                initGame = resetActionForGame(game, seed);
                gs = (BlackjackGameState) initGame.getGameState();
            }
        }

        // case 4: With a soft hand, the general strategy is to keep hitting until a total of at least 18 is reached. Thus, with an ace and a six (7 or 17), the player would not stop at 17, but would hit.
        List<Integer> softHandPlayers = new ArrayList<>();
        for (int i=0; i<gs.getNPlayers()-1; ++i) {
            List<FrenchCard> components = gs.getPlayerDecks().get(i).getComponents();
            assert components != null && components.size() == 2;
            if (BooleanUtils.isFalse((components.get(0).type != FrenchCard.FrenchCardType.Number && components.get(1).type == FrenchCard.FrenchCardType.Number)
                || (components.get(1).type != FrenchCard.FrenchCardType.Number && components.get(0).type == FrenchCard.FrenchCardType.Number))) {
                continue;
            }
            softHandPlayers.add(i);
            int sum = calDeckSum(components);
            while (sum < 18) {
                initGame.processOneAction(new Hit(i, true, false));
                gs = (BlackjackGameState) initGame.getGameState();
                playerDecks = gs.getPlayerDecks();
                components = playerDecks.get(i).getComponents();
                sum = calDeckSum(components);
            }
            // The program is designed with when sum is equal to 21, player is no need to do Stand Action.
            if (sum < 21) {
                initGame.processOneAction(new Stand());
                gs = (BlackjackGameState) initGame.getGameState();
                playerDecks = gs.getPlayerDecks();
                components = playerDecks.get(i).getComponents();
                sum = calDeckSum(components);
            }
        }
        if (softHandPlayers.size() == gs.getNPlayers() - 1) {
            gs = (BlackjackGameState) initGame.getGameState();
            while (calDeckSum(gs.getPlayerDecks().get(gs.getDealerPlayer()).getComponents()) < 17 && gs.getGameStatus() != CoreConstants.GameResult.GAME_END) {
                initGame.processOneAction(new Hit(gs.getDealerPlayer(), true, false));
                gs = (BlackjackGameState) initGame.getGameState();
            }
            assert gs.getGameStatus() == CoreConstants.GameResult.GAME_END;
            boolean v = gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME && CollectionUtils.isNotEmpty(softHandPlayers) && softHandPlayers.size() == gs.getNPlayers() - 1;
            for (int i = 0; i < gs.getPlayerResults().length - 1; i++) {
                v &= gs.getPlayerResults()[i] == CoreConstants.GameResult.WIN_GAME;
            }
            if (v && !strategyTextAndSimulate.containsKey(simulateStrategyText04)) {
                for (Pair<Integer, AbstractAction> pair : gs.getHistory()) {
                    System.out.println("4: " + pair.a + pair.b);
                }
                for (CoreConstants.GameResult playerResult : gs.getPlayerResults()) {
                    System.out.println(playerResult);
                }
                assert getWinGameCount(gs.getPlayerResults()) == gs.getNPlayers() - 1 && gs.getPlayerResults()[gs.getDealerPlayer()] == CoreConstants.GameResult.LOSE_GAME;
                simulateInfo.setStartText(simulateStrategyText04);
                strategyTextAndSimulate.put(simulateStrategyText04, new Pair<>(simulateInfo,game));
                return;
            } else {
                // reset initGame again
                initGame = resetActionForGame(game, seed);
                gs = (BlackjackGameState) initGame.getGameState();
            }
        }
    }

    private int getWinGameCount(CoreConstants.GameResult[] results) {
        int count = 0;
        for (CoreConstants.GameResult result : results) {
            if (result == CoreConstants.GameResult.WIN_GAME) {
                count += 1;
            }
        }
        return count;
    }

    private Game resetActionForGame(Game game, Long seed) {
        AbstractGameState gameState = game.getGameState().copy();
        // Don't use gameState.gameParameter.randomSeed
        gameState.reset(seed);
        return new Game(game.getGameType(), game.getPlayers(),
                game.getGameType().createForwardModel(null, game.getPlayers().size()), gameState);
    }

    @Override
    public void exportJson() {
        String path = "data/preGameState/Blackjack";
        if (strategyEnum == BlackjackStrategyEnum.GAME_RESULT) {
            path += "/GameResult";
            File[] allFiles = JSONUtils.getAllFile(path);
            int allFileSize = allFiles == null ? 0 : allFiles.length;
            assert strategyTextAndGameResults.size() == gameResultStrategyCount;
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
        } else if (strategyEnum == BlackjackStrategyEnum.SIMULATE) {
            path += "/Simulate";
            File[] allFiles = JSONUtils.getAllFile(path);
            int allFileSize = allFiles == null ? 0 : allFiles.length;
            assert strategyTextAndSimulate.size() == simulateStrategyCount;
            for (Map.Entry<String, Pair<PreGameState.SimulateInfo, Game>> entry : strategyTextAndSimulate.entrySet()) {
                Game game = entry.getValue().b;
                BlackjackGameState gs = (BlackjackGameState) game.getGameState();
                GameResultForJSON gameResultForJSON = new GameResultForJSON();
                gameResultForJSON.setPlayerCount(gs.getNPlayers());
                gameResultForJSON.setGameResultDesc("");
//                gameResultForJSON.setStrategy(entry.getKey());

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
                gameResultForJSON.setSimulateInfo(entry.getValue().a);

                JSONUtils.writeToJsonFile(gameResultForJSON, path + "/" + allFileSize++);
            }
        }
    }

    @Override
    public boolean isEnd() {
        if (this.strategyEnum == BlackjackStrategyEnum.GAME_RESULT) {
            return strategyTextAndGameResults.size() == gameResultStrategyCount;
        }
        if (this.strategyEnum == BlackjackStrategyEnum.SIMULATE) {
            return strategyTextAndSimulate.size() == simulateStrategyCount;
        }
        return false;
    }

    @Override
    public void recordDeck(AbstractGameState gameState) {
        // Do nothing Because it has just one round.
    }

    private int calDeckSum(List<FrenchCard> cards) {
        int sum = 0;
        for (FrenchCard card : cards) {
            if (card.type != FrenchCard.FrenchCardType.Number)
                sum += 10;
            else
                sum += card.number;
        }
        return sum;
    }

    private void isGameResult(Game game) {
        BlackjackGameState gs = (BlackjackGameState) game.getGameState();
        CoreConstants.GameResult[] playerResults = gs.getPlayerResults();
        List<PartialObservableDeck<FrenchCard>> playerDecks = gs.getPlayerDecks();
        int nPlayer = gs.getNPlayers();
        int[] handSum = new int[nPlayer];
        for (int i=0; i<nPlayer; ++i) {
            handSum[i] = calDeckSum(playerDecks.get(i).getComponents());
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

        private PreGameState.SimulateInfo simulateInfo;

        public PreGameState.SimulateInfo getSimulateInfo() {
            return simulateInfo;
        }

        public void setSimulateInfo(PreGameState.SimulateInfo simulateInfo) {
            this.simulateInfo = simulateInfo;
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

                @Serial
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
