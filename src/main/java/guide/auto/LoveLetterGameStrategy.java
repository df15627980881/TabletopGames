package guide.auto;

import core.AbstractGameState;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.PartialObservableDeck;
import games.loveletter.LoveLetterGameState;
import games.loveletter.actions.BaronAction;
import games.loveletter.actions.GuardAction;
import games.loveletter.actions.KingAction;
import games.loveletter.actions.PlayCard;
import games.loveletter.cards.LoveLetterCard;
import guide.PreGameState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import utilities.JSONUtils;
import utilities.Pair;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class LoveLetterGameStrategy implements IGameStrategy {

    public static Map<String, Game> strategyTextAndMechanism = new HashMap<>();

    public static Map<String, Pair<PreGameState.SimulateInfo, Game>> strategyTextAndSimulate = new HashMap<>();

    public static Map<String, Game> strategyTextAndGameResults = new HashMap<>();

    public static List<PartialObservableDeck<LoveLetterCard>> tmpCardsForReserve;
    public static boolean tmpCardsForReserveSwitch = false;

    // When you want to add a new strategy, please modify this count
//    private final int mechanismStrategyCount = 3;
    private final int simulateStrategyCount = 4;
    private final int gameResultStrategyCount = 2;

    private LoveLetterStrategyEnum loveLetterStrategyEnum;

    private static Pair<Long, Game> gameForMechanism;

    private final String gameResultStrategyText01 = "Round ends when only a single player is left.";
    private final String gameResultStrategyText02 = "there are no cards left in the draw pile";

    private final String simulateStrategyText01 = "When another player plays a card, you can infer the cards in their hand from the outcome and use some cards to defeat them.";

    private final String simulateStrategyText02 = "When you hold a Baron, you should discard the smaller card in your hand.";

    private final String simulateStrategyText03 = "In the early stages of the game, using a Guard can guess an opponent's card. To increase the chances of success, players often guess lower cards (each valued 2-5, appearing twice) at the start of the game to improve their success rate.";

    private final String simulateStrategyText04 = "When your opponent uses a King on you, the card in their hand becomes known to you as well, so try to defeat them.";


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

        if (LoveLetterStrategyEnum.GAME_RESULT.getName().equals(strategy)) {
            this.loveLetterStrategyEnum = LoveLetterStrategyEnum.GAME_RESULT;
            isGameResult(game, seed);
            return true;
        }

        if (LoveLetterStrategyEnum.SIMULATE.getName().equals(strategy)) {
            this.loveLetterStrategyEnum = LoveLetterStrategyEnum.SIMULATE;
            isSimulate(game, seed);
            return true;
        }

        return false;
    }

    private void isGameResult(Game game, Long seed) {
        // case1: Round ends when only a single player is left
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        if (gs.getRemainingCards() != 0) {
            strategyTextAndGameResults.put(gameResultStrategyText01, game);
            return;
        }

        // case2: there are no cards left in the draw pile
        int remainPlayerCount = 0;
        List<PartialObservableDeck<LoveLetterCard>> playerHandCards = gs.getPlayerHandCards();
        for (PartialObservableDeck<LoveLetterCard> playerHandCard : playerHandCards) {
            remainPlayerCount += playerHandCard.getComponents().size() != 0 ? 1 : 0;
        }
        if (remainPlayerCount > 1)
            strategyTextAndGameResults.put(gameResultStrategyText02, game);
    }

    private void isSimulate(Game game, Long seed) {
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();


        // case1: When another player plays a card, you can infer the cards in their hand from the outcome and use some cards to defeat them.
        // Player 1 played a Baron, and Player 2, who had a Countess in hand, lost the game. It can be inferred that Player 1's card was the Princess, and he can be defeated using the Prince.
        Game initGame = resetActionForGame(game, seed);
        /**
        for (int i=0; i<history.size(); ++i) {
            AbstractAction action = history.get(i).b;
            LoveLetterGameState currentGS = (LoveLetterGameState) initGame.getGameState();
            List<PartialObservableDeck<LoveLetterCard>> playerHandCards = currentGS.getPlayerHandCards();
            if (action instanceof BaronAction) {
                BaronAction ba = (BaronAction) action;
                if (ba.getPlayerID() == 1 && ba.getTargetPlayer() == 2
                        && CollectionUtils.isNotEmpty(playerHandCards.get(0).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(1).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(2).getComponents())
                        && playerHandCards.get(2).getComponents().get(0).cardType == LoveLetterCard.CardType.Countess
                        && playerHandCards.get(1).getComponents().get(0).cardType == LoveLetterCard.CardType.Princess
                        && playerHandCards.get(0).getComponents().get(0).cardType == LoveLetterCard.CardType.Prince) {
                    PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
                    simulateInfo.setBeginActionIndex(i+1);
                    simulateInfo.setIsSuccess("method2");
                    simulateInfo.setStartText("When another player plays a card, you can infer the cards in their hand from the outcome and use some cards to defeat them. please try to defeat player1.");
                    simulateInfo.setSuccessText("Congratulations! It seems you have mastered this strategy.");
                    simulateInfo.setFailText("Oops! You can try it again.");
                    List<String> players = new ArrayList<>();
                    players.add("HumanGUIPlayer");
                    for (int j=1; j<initGame.getGameState().getNPlayers(); ++j) players.add("MCTS");
                    simulateInfo.setPlayers(players);
                    strategyTextAndSimulate.put(simulateStrategyText01, new Pair<>(simulateInfo, game));
                    return;
                }
            }
            initGame.processOneAction(action);
        }
         **/

        /**
        // case2: 当你手中握着 Baron 时，你应该舍弃手中较小的牌
        initGame = resetActionForGame(game, seed);
        for (int i=0; i<history.size(); ++i) {
            AbstractAction action = history.get(i).b;
            LoveLetterGameState currentGS = (LoveLetterGameState) initGame.getGameState();
            List<PartialObservableDeck<LoveLetterCard>> playerHandCards = currentGS.getPlayerHandCards();
            if (action instanceof BaronAction) {
                BaronAction ba = (BaronAction) action;
                if (ba.getPlayerID() == 0 && ba.getTargetPlayer() == 2
                        && CollectionUtils.isNotEmpty(playerHandCards.get(0).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(1).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(2).getComponents())
                        && playerHandCards.get(0).getComponents().size() > 1
                        && playerHandCards.get(2).getComponents().get(0).cardType != LoveLetterCard.CardType.Guard
                        && playerHandCards.get(1).getComponents().get(0).cardType != LoveLetterCard.CardType.Guard
                        && ((playerHandCards.get(0).getComponents().get(0).cardType == LoveLetterCard.CardType.Guard)
                        || playerHandCards.get(0).getComponents().get(1).cardType == LoveLetterCard.CardType.Guard)) {
                    PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
                    simulateInfo.setBeginActionIndex(i);
                    simulateInfo.setIsSuccess("method3");
                    simulateInfo.setStartText("When you hold a Baron, you should discard the smaller card in your hand, please try not to lose the game.");
                    simulateInfo.setSuccessText("Congratulations! It seems you have mastered this strategy.");
                    simulateInfo.setFailText("Oops! You can try it again.");
                    List<String> players = new ArrayList<>();
                    players.add("HumanGUIPlayer");
                    for (int j=1; j<initGame.getGameState().getNPlayers(); ++j) players.add("MCTS");
                    simulateInfo.setPlayers(players);
                    strategyTextAndSimulate.put(simulateStrategyText02, new Pair<>(simulateInfo, game));
                    return;
                }
            }
            initGame.processOneAction(action);
        }
         **/
/**
        // case3: 在游戏初期，使用Guard可以猜中对手。为了成功增加胜算，玩家通常会在游戏开始时猜出较低的牌（每张 2-5 张牌出现两次），以提高成功率
        initGame = resetActionForGame(game, seed);
        for (int i=0; i<history.size(); ++i) {
            AbstractAction action = history.get(i).b;
            LoveLetterGameState currentGS = (LoveLetterGameState) initGame.getGameState();
            List<PartialObservableDeck<LoveLetterCard>> playerHandCards = currentGS.getPlayerHandCards();
            if (action instanceof GuardAction) {
                GuardAction ba = (GuardAction) action;
                if (ba.getPlayerID() == 0 && ba.getTargetPlayer() == 2
                        && CollectionUtils.isNotEmpty(playerHandCards.get(0).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(1).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(2).getComponents())
                        && (playerHandCards.get(2).getComponents().get(0).cardType.getValue() >= 2 && playerHandCards.get(2).getComponents().get(0).cardType.getValue() <= 5)
                        && (playerHandCards.get(1).getComponents().get(0).cardType.getValue() >= 2 && playerHandCards.get(1).getComponents().get(0).cardType.getValue() <= 5)
                        ) {
                    PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
                    simulateInfo.setBeginActionIndex(i);
                    simulateInfo.setIsSuccess("method4");
                    simulateInfo.setStartText("In the early stages of the game, using a Guard can guess an opponent's card. To increase the chances of success, players often guess lower cards (each valued 2-5, appearing twice) at the start of the game to improve their success rate.");
                    simulateInfo.setSuccessText("Congratulations! It seems you have mastered this strategy.");
                    simulateInfo.setFailText("Oops! You can try it again.");
                    List<String> players = new ArrayList<>();
                    players.add("HumanGUIPlayer");
                    for (int j=1; j<initGame.getGameState().getNPlayers(); ++j) players.add("MCTS");
                    simulateInfo.setPlayers(players);
                    strategyTextAndSimulate.put(simulateStrategyText03, new Pair<>(simulateInfo, game));
                    return;
                }
            }
            initGame.processOneAction(action);
        }
 **/

        // case4: 当对手对你使用King时，他手上的牌对于你来说也是已知的，试着击败他吧
        initGame = resetActionForGame(game, seed);
        for (int i=0; i<history.size(); ++i) {
            AbstractAction action = history.get(i).b;
            LoveLetterGameState currentGS = (LoveLetterGameState) initGame.getGameState();
            List<PartialObservableDeck<LoveLetterCard>> playerHandCards = currentGS.getPlayerHandCards();
            if (action instanceof KingAction) {
                KingAction ka = (KingAction) action;
                if (ka.getPlayerID() == 2 && ka.getTargetPlayer() == 0
                        && CollectionUtils.isNotEmpty(playerHandCards.get(0).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(1).getComponents())
                        && CollectionUtils.isNotEmpty(playerHandCards.get(2).getComponents())
                        && (playerHandCards.get(2).getComponents().get(0).cardType == LoveLetterCard.CardType.Guard)
                        && (playerHandCards.get(0).getComponents().get(0).cardType != LoveLetterCard.CardType.Guard)
                ) {
                    PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
                    simulateInfo.setBeginActionIndex(i+1);
                    simulateInfo.setIsSuccess("method5");
                    simulateInfo.setStartText("When your opponent uses a King on you, the card in their hand becomes known to you as well, so try to defeat them.");
                    simulateInfo.setSuccessText("Congratulations! It seems you have mastered this strategy.");
                    simulateInfo.setFailText("Oops! You can try it again.");
                    List<String> players = new ArrayList<>();
                    players.add("HumanGUIPlayer");
                    for (int j=1; j<initGame.getGameState().getNPlayers(); ++j) players.add("MCTS");
                    simulateInfo.setPlayers(players);
                    strategyTextAndSimulate.put(simulateStrategyText04, new Pair<>(simulateInfo, game));
                    return;
                }
            }
            initGame.processOneAction(action);
        }
    }

    private Game resetActionForGame(Game game, Long seed) {
        AbstractGameState gameState = game.getGameState().copy();
        // Don't use gameState.gameParameter.randomSeed
        gameState.reset(seed);
        return new Game(game.getGameType(), game.getPlayers(),
                game.getGameType().createForwardModel(null, game.getPlayers().size()), gameState);
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
        HashSet<LoveLetterCard.CardType> cardTypesAppear = new HashSet<>();
        for (int i=0; i<10; ++i) {
            PlayCard playCard = (PlayCard) history.get(i).b;
            cardTypesAppear.add(playCard.getCardType());
        }
        System.out.println(vis.size() + " " + history.size() + " " + cardTypesAppear.size());
        if (vis.size() == 8 && cardTypesAppear.size() == 8) {
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
            for (PartialObservableDeck<LoveLetterCard> a : gameForMechanism.b.getGameState().getNowDecks()) {
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

            JSONUtils.writeToJsonFile(gameResultForJSON, path + "/" + allFileSize);

        } else if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.GAME_RESULT) {
            path += "/GameResult";
            File[] allFiles = JSONUtils.getAllFile(path);
            int allFileSize = allFiles == null ? 0 : allFiles.length;
            assert strategyTextAndGameResults.size() != 0;
            for (Map.Entry<String, Game> entry : strategyTextAndGameResults.entrySet()) {
                Game game = entry.getValue();

                GameResultForJSON gameResultForJSON = new GameResultForJSON();
                gameResultForJSON.setPlayerCount(game.getPlayers().size());

                List<Pair<Integer, AbstractAction>> history = game.getGameState().getHistory();
                List<GameResultForJSON.Action> actions = new ArrayList<>();
                for (Pair<Integer, AbstractAction> pair : history) {
                    GameResultForJSON.Action action = new GameResultForJSON.Action();
                    action.setPlayer(pair.a);
                    action.setAction((PlayCard) pair.b);
                    actions.add(action);
                }
                List<GameResultForJSON.Deck> decks = new ArrayList<>();
                for (PartialObservableDeck<LoveLetterCard> a : game.getGameState().getNowDecks()) {
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

                gameResultForJSON.setHistoryText(game.getGameState().getHistoryAsText());
                gameResultForJSON.setActions(actions);
                gameResultForJSON.setGameResultDesc(entry.getKey());
                gameResultForJSON.setSeed(game.getGameState().getCoreGameParameters().getRandomSeed());
                gameResultForJSON.setStrategy(null);
                gameResultForJSON.setDecks(decks);

                JSONUtils.writeToJsonFile(gameResultForJSON, path + "/" + allFileSize++);
            }
        } else if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.SIMULATE) {
            path += "/Simulate";
            File[] allFiles = JSONUtils.getAllFile(path);
            int allFileSize = allFiles == null ? 0 : allFiles.length;
            assert strategyTextAndSimulate.size() != 0;
            for (Map.Entry<String, Pair<PreGameState.SimulateInfo, Game>> entry : strategyTextAndSimulate.entrySet()) {
                Game game = entry.getValue().b;

                GameResultForJSON gameResultForJSON = new GameResultForJSON();
                gameResultForJSON.setPlayerCount(game.getPlayers().size());

                List<Pair<Integer, AbstractAction>> history = game.getGameState().getHistory();
                List<GameResultForJSON.Action> actions = new ArrayList<>();
                for (Pair<Integer, AbstractAction> pair : history) {
                    GameResultForJSON.Action action = new GameResultForJSON.Action();
                    action.setPlayer(pair.a);
                    action.setAction((PlayCard) pair.b);
                    actions.add(action);
                }
                List<GameResultForJSON.Deck> decks = new ArrayList<>();
                for (PartialObservableDeck<LoveLetterCard> a : game.getGameState().getNowDecks()) {
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

                gameResultForJSON.setHistoryText(game.getGameState().getHistoryAsText());
                gameResultForJSON.setActions(actions);
//                gameResultForJSON.setGameResultDesc(entry.getKey());
                gameResultForJSON.setSeed(game.getGameState().getCoreGameParameters().getRandomSeed());
                gameResultForJSON.setStrategy(null);
                gameResultForJSON.setDecks(decks);
                gameResultForJSON.setSimulateInfo(entry.getValue().a);

                JSONUtils.writeToJsonFile(gameResultForJSON, path + "/" + allFileSize++);
            }
        }

    }

    @Override
    public void recordDeck(AbstractGameState gs) {
        LoveLetterGameState gameState = (LoveLetterGameState) gs;
        List<Deck<LoveLetterCard>> playerDiscardCards = gameState.getPlayerDiscardCards();
        playerDiscardCards = playerDiscardCards.stream().filter(x -> x.getComponents().size() == 0).toList();
        if (playerDiscardCards.size() != gameState.getNPlayers() || !tmpCardsForReserveSwitch) return;
        List<PartialObservableDeck<LoveLetterCard>> playerHandCards = gameState.getPlayerHandCards();
        playerHandCards = playerHandCards.stream().filter(x -> x.getComponents().size() == 0).collect(Collectors.toList());
        if (playerHandCards.size() == gameState.getNPlayers()) return;
        playerHandCards = gameState.getPlayerHandCards();
        PartialObservableDeck<LoveLetterCard> deck = new PartialObservableDeck<>("recordDeck"+gs.getCurrentPlayer(), gs.getCurrentPlayer(), gameState.getPlayerHandCards().get(0).getDeckVisibility());
        deck.add(gameState.getRemovedCard().copy());
        int currentPlayer = gameState.getCurrentPlayer();
        for (int i=currentPlayer, j=0; i<gameState.getNPlayers() && j<gameState.getNPlayers(); i=(i+1)%gameState.getNPlayers(), ++j) {
            if (j == currentPlayer)
                deck.add(playerHandCards.get(j).get(1).copy());
            else
                deck.add(playerHandCards.get(j).get(0).copy());
        }
        deck.add(playerHandCards.get(currentPlayer).get(0).copy());

        for (int i=0; i<gameState.getRemainingCards(); ++i) deck.add(gameState.getDrawPile().get(i).copy());
        deck.reverse();
        gameState.getNowDecks().add(deck);
//        System.out.println(gameState.getNowDecks().size());
        int count = (int) Arrays.stream(gameState.getPlayerResults()).filter(x -> x == CoreConstants.GameResult.GAME_ONGOING).count();
        Assert.assertEquals(count, gameState.getNPlayers());
        Assert.assertEquals(deck.getComponents().size(), 16);
    }

    @Override
    public boolean isEnd() {
        System.out.println("strategyTextAndSimulate.size(): " + strategyTextAndSimulate.size());
        if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.MECHANISM) {
            return Objects.nonNull(gameForMechanism);
        }
        if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.GAME_RESULT) {
            return strategyTextAndGameResults.size() == gameResultStrategyCount;
        }
        if (this.loveLetterStrategyEnum == LoveLetterStrategyEnum.SIMULATE) {
            return strategyTextAndSimulate.size() == 1;
//            return strategyTextAndSimulate.size() == simulateStrategyCount;
        }
        return false;
    }

    public enum LoveLetterStrategyEnum {
        MECHANISM("mechanism"),
        GAME_RESULT("gameResult"),
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
        private PreGameState.SimulateInfo simulateInfo;

        public PreGameState.SimulateInfo getSimulateInfo() {
            return simulateInfo;
        }

        public void setSimulateInfo(PreGameState.SimulateInfo simulateInfo) {
            this.simulateInfo = simulateInfo;
        }

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
