package guide;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.blackjack.actions.Hit;
import guide.param.GuideGenerateParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import players.mcts.MCTSPlayer;
import players.simple.RandomPlayer;
import utilities.Pair;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import static core.CoreConstants.GameResult;


public class GuideGenerator {

    public static final int iteratorNum = 50;

    public final Map<GameType, List<CoreConstants.GameResult>> gameAndResultType = new HashMap<>(){{
        put(GameType.Blackjack, Lists.newArrayList(GameResult.WIN_GAME, GameResult.LOSE_GAME, GameResult.DRAW_GAME, GameResult.GAME_ONGOING, GameResult.GAME_END));
        put(GameType.LoveLetter, Lists.newArrayList(GameResult.LOSE_ROUND, GameResult.GAME_ONGOING, GameResult.WIN_ROUND, GameResult.DRAW_ROUND, GameResult.LOSE_GAME, GameResult.WIN_GAME));
    }};

    private final Map<CoreConstants.GameResult, SimulateForMechanismParam> resultAndActionSequencesMap;

    private static final String gameType = "Blackjack";

    public GuideGenerator() {
        this.resultAndActionSequencesMap = new HashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        List<Long> seeds = new ArrayList<>(iteratorNum);
        for (int i = 1; i <= iteratorNum; ++i) {
            seeds.add(System.currentTimeMillis());
            Thread.sleep(10);
        }
//        System.out.println("###");
        Map<Long, Game> result1 = runWithOutBannedAction(seeds, initParam(gameType));
//        Map<Long, Game> result2 = runWithOutBannedAction(seeds, initParam("Blackjack"));
//        List<Long> resultSeeds = checkValidPecks(result1, result2);
//        System.out.println("end");
//        if (CollectionUtils.isNotEmpty(resultSeeds)) {
//            for (Long resultSeed : resultSeeds) {
//                System.out.println(resultSeed);
//            }
//            SwingUtilities.invokeLater(() -> new InterfaceTech(resultSeeds.get(0), result1.get(resultSeeds.get(0))).display());

//        }
//        for (Long seed : seeds) {
//            if (result1.get(seed).getGameState().getHistory().size() <= 5) {
//                System.out.println(seed);
//                SwingUtilities.invokeLater(() -> new InterfaceTech(seed, result1.get(seed)).display());
//                break;
//            }
//        }
//        Map<Long, Game> data = preProcess(initParam("LoveLetter"));
//        Map<Long, Pair<Game, Game>> seedsForWinWithBannedAction = getSeedsForWinWithBannedAction(result1, new Hit(0));
//        if (MapUtils.isEmpty(seedsForWinWithBannedAction)) {
//            main(args);
//        } else {
        Map<Long, Game> seedsAndGameMapForMechanism = new HashMap<>();
        GuideGenerator guideGenerator = new GuideGenerator();
        for (Map.Entry<Long, Game> entry : result1.entrySet()) {
//                guideGenerator.getTriggerGameMechanics(entry.getKey(), entry.getValue());
            Game game = entry.getValue();
            for (GameResult playerResult : game.getGameState().getPlayerResults()) {
                if (!guideGenerator.resultAndActionSequencesMap.containsKey(playerResult)) {
                    AbstractGameState gameState = game.getGameState().copy();
                    gameState.reset(entry.getKey());
                    gameState.setFrame(game.getGameState().getFrame());
                    Game newGame = new Game(game.getGameType(), game.getPlayers(),
                            game.getGameType().createForwardModel(null, game.getPlayers().size()), gameState);
                    guideGenerator.resultAndActionSequencesMap.put(playerResult, new SimulateForMechanismParam(entry.getKey(),
                            newGame, game, game.getGameState().getHistory().stream().map(x -> x.b).collect(Collectors.toList()), playerResult));
                }
            }
        }

        List<Game> gamesForPreviousActionShow = Lists.newArrayList(guideGenerator.getGamesForPreviousActionShow(seeds.get(0), initParam(gameType)));

        PreGameState pre = PreGameStateUtils.getBlackjack();
        RandomPlayer player1 = new RandomPlayer();
        RandomPlayer player2 = new RandomPlayer();
        RandomPlayer player3 = new RandomPlayer();
        RandomPlayer player4 = new RandomPlayer();
        RandomPlayer player5 = new RandomPlayer();
        RandomPlayer player6 = new RandomPlayer();
        RandomPlayer player7 = new RandomPlayer();

        ArrayList<AbstractPlayer> players = new ArrayList<>(Lists.newArrayList(player1, player2, player3, player4, player5, player6, player7));
        AbstractGameState.isGuide = true;
        Game newGame = Game.runOne(GameType.valueOf(gameType), null, players, pre.getSeed(), false, null, null, 1);
        System.out.println(1);
        guideGenerator.resultAndActionSequencesMap.forEach((k, v) -> System.out.printf("%-15s : %s%n", k, v));
        SwingUtilities.invokeLater(() -> new InterfaceTech(pre.getSeed(), newGame, new ArrayList<>(guideGenerator.resultAndActionSequencesMap.values()), gamesForPreviousActionShow).display());
//        }


    }

    public static GuideGenerateParam initParam(String gameType) {
        GuideGenerateParam param = new GuideGenerateParam();

//        players.add(new OSLAPlayer());
//        players.add(new BasicMCTSPlayer());
//        players.add(new RandomPlayer());
//        players.add(new OSLAPlayer());
//        players.add(new BasicMCTSPlayer());
        String gameParams = null;

        param.setGameType(GameType.valueOf(gameType));
//        param.setPlayers(players);
        param.setParameterConfigFile(gameParams);

        return param;
    }

    public static Map<Long, Game> runWithOutBannedAction(List<Long> seeds, GuideGenerateParam param) {
        if (Objects.isNull(param) || CollectionUtils.isEmpty(seeds)) {
            System.out.println("process#param is Null");
            return null;
        }
        Map<Long, Game> result = new HashMap<>();
        GameType gameType = param.getGameType();

        //        players.add(new BasicMCTSPlayer());
        RandomPlayer player1 = new RandomPlayer();
        RandomPlayer player2 = new RandomPlayer();
        RandomPlayer player3 = new RandomPlayer();
        RandomPlayer player4 = new RandomPlayer();
        RandomPlayer player5 = new RandomPlayer();
        RandomPlayer player6 = new RandomPlayer();

        ArrayList<AbstractPlayer> players =
                new ArrayList<>(Lists.newArrayList(player1, player2, player3));
//        , player4, player5, player6));

        for (int i = 0; i < iteratorNum; ++i) {
            Game game;
            try {
                game = Game.runOne(gameType, null, players, seeds.get(i), false, null, null, 1);
            } catch (Exception e) {
                System.out.println("failed, continue next" + e.getMessage());
                continue;
            }

            for (int j = 0; j < game.getGameState().getPlayerResults().length; j++) {
//                System.out.println(game.getGameState().getPlayerResults()[j]);
//                System.out.println(param.getPlayers().get(5).getBannedAction() == null);
            }
//            System.out.println("----------");
//            System.out.println(game.getGameState().getHistory());

            result.put(seeds.get(i), game);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static Map<Long, Game> runWithBannedAction(List<Long> seeds, GuideGenerateParam param) {
        if (Objects.isNull(param) || CollectionUtils.isEmpty(seeds)) {
            System.out.println("process#param is Null");
            return null;
        }
        Map<Long, Game> result = new HashMap<>();
        GameType gameType = param.getGameType();

        //        players.add(new BasicMCTSPlayer());
        RandomPlayer player1 = new RandomPlayer();
        RandomPlayer player2 = new RandomPlayer();
//        RandomPlayer player3 = new RandomPlayer();
        RandomPlayer player4 = new RandomPlayer();
        RandomPlayer player5 = new RandomPlayer();
        RandomPlayer player6 = new RandomPlayer();


//        player1.setBannedAction(new Hit(0));
//        player5.setBannedAction(new Stand());
//        player1.setBannedAction(new BaronAction(-1, -1, -1, false, false));
        ArrayList<AbstractPlayer> players =
                new ArrayList<>(Lists.newArrayList(player1, player2));
//        , player4, player5, player6));

        for (int i = 0; i < iteratorNum; ++i) {
            Game game;
            try {
                game = Game.runOne(gameType, null, players, seeds.get(i), false, null, null, 1);
            } catch (Exception e) {
                System.out.println("failed, continue next");
                continue;
            }

            for (int j = 0; j < game.getGameState().getPlayerResults().length; j++) {
//                System.out.println(game.getGameState().getPlayerResults()[j]);
//                System.out.println(param.getPlayers().get(5).getBannedAction() == null);
            }
//            System.out.println("----------");
//            System.out.println(game.getGameState().getHistory());

            result.put(seeds.get(i), game);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * always let player0 run with banned action.
     * @param gameMap
     * @param bannedAction
     * @return
     */
    public static Map<Long, Pair<Game, Game>> getSeedsForWinWithBannedAction(Map<Long, Game> gameMap,
                                                                             AbstractAction bannedAction) {
        if (MapUtils.isEmpty(gameMap) || Objects.isNull(bannedAction)) {
            return new HashMap<>();
        }
        Map<Long, Pair<Game, Game>> results = new HashMap<>();
        Class<?> bannedActionClass = bannedAction.getClass();
        for (Map.Entry<Long, Game> entry : gameMap.entrySet()) {
            Game game = entry.getValue();
            Game gameWithBannedAction = runWithSpecificActionSequences(entry.getKey(), game,
                    game.getGameState().getHistory().stream().filter(a -> a.a != 0
                            || !bannedActionClass.isInstance(a.b)).map(x -> x.b).collect(Collectors.toList()));
//            System.out.println(game.getGameState().getPlayerResults()[0] + " " + gameWithBannedAction.getGameState().getPlayerResults()[0]);
//            game.getGameState().getHistory().forEach(System.out::println);
//            System.out.println("---");
            gameWithBannedAction.getGameState().getHistory().forEach(System.out::println);
            if (game.getGameState().getPlayerResults()[0] == CoreConstants.GameResult.WIN_GAME
                    && gameWithBannedAction.getGameState().getPlayerResults()[0] == CoreConstants.GameResult.LOSE_GAME) {
                results.put(entry.getKey(), new Pair<>(game, gameWithBannedAction));
            }
        }
        return results;
    }

    public static Game runWithSpecificActionSequences(Long seed, Game gameInit, List<AbstractAction> actions) {
        AbstractGameState gameState = gameInit.getGameState().copy();
        gameState.reset(seed);
        gameState.setFrame(gameInit.getGameState().getFrame());
        Game newGame = new Game(gameInit.getGameType(), gameInit.getPlayers(),
                gameInit.getGameType().createForwardModel(null, gameInit.getPlayers().size()), gameState);
        actions.forEach(newGame::processOneAction);
        return newGame;
    }

    public void getTriggerGameMechanics(Long seed, Game gameInit) {
        AbstractGameState gameState = gameInit.getGameState().copy();
        gameState.reset(seed);
        gameState.setFrame(gameInit.getGameState().getFrame());
        Game newGame = new Game(gameInit.getGameType(), gameInit.getPlayers(),
                gameInit.getGameType().createForwardModel(null, gameInit.getPlayers().size()), gameState);
        List<AbstractAction> actions = gameInit.getGameState().getHistory().stream().map(x -> x.b).toList();
        if (actions.size() <= 3) return;
//        for (int i = 0; i < actions.size() >> 1; i++) {
//            newGame.processOneAction(actions.get(i));
//        }
        // Because game is consist of 2 players, game must be not end

        simulateGameProcess(seed, newGame, newGame, Lists.newArrayList(), 0);
        System.out.println("resultAndActionSequencesMap: " + resultAndActionSequencesMap.size());
    }

    public void simulateGameProcess(Long seed, Game initGame, Game nowGame, List<AbstractAction> actions, int step) {
        if (resultAndActionSequencesMap.size() == gameAndResultType.get(initGame.getGameType()).size()) return;
        if (step > 2) {
            for (GameResult playerResult : nowGame.getGameState().getPlayerResults()) {
                if (!resultAndActionSequencesMap.containsKey(playerResult)) {
                    resultAndActionSequencesMap.put(playerResult, new SimulateForMechanismParam(seed, initGame, nowGame, actions, playerResult));
                }
            }
        }
        for (GameResult playerResult : nowGame.getGameState().getPlayerResults()) {
            if (playerResult == GameResult.LOSE_GAME || playerResult == GameResult.DRAW_GAME ||  playerResult == GameResult.WIN_GAME) {
                return;
            }
        }
        List<AbstractAction> avalibleActions = nowGame.getForwardModel().computeAvailableActions(nowGame.getGameState());
        for (AbstractAction avalibleAction : avalibleActions) {
            List<AbstractAction> tmp = new ArrayList<>(actions);
            tmp.add(avalibleAction);
            simulateGameProcess(seed, initGame, runWithSpecificActionSequences(seed, nowGame, tmp), tmp, step + 1);
        }
    }

    private Game getGamesForPreviousActionShow(Long seed, GuideGenerateParam param) {
        ArrayList<AbstractPlayer> players =
                new ArrayList<>();
        for (int i=0; i<param.getGameType().getMaxPlayers(); ++i) {
            players.add(new MCTSPlayer());
        }
        return Game.runOne(param.getGameType(), null, players, seed, false, null, null, 1);
    }

    private static List<Long> checkValidPecks(Map<Long, Game> resultWithBannedAction, Map<Long, Game> resultWithoutBannedAction) {
        List<Long> results = new ArrayList<>();
        for (Map.Entry<Long, Game> longGameEntry : resultWithBannedAction.entrySet()) {
            Long seed = longGameEntry.getKey();
            Game r1 = resultWithBannedAction.get(seed);
            Game r2 = resultWithoutBannedAction.get(seed);
            if (r1.getGameState().getPlayerResults()[0] == CoreConstants.GameResult.LOSE_GAME
                    && r2.getGameState().getPlayerResults()[0] == CoreConstants.GameResult.WIN_GAME
                    && r2.getGameState().getHistory().get(0).b instanceof Hit) {
                results.add(seed);
            }
        }
        return results;
    }

    public static class SimulateForMechanismParam {
        private Long seed;
        private Game initGame;
        private Game finalGame;
        private List<AbstractAction> actions;
        private GameResult gameResult;

        public SimulateForMechanismParam() {
        }

        public SimulateForMechanismParam(Long seed, Game initGame, Game finalGame, List<AbstractAction> actions, GameResult gameResult) {
            this.seed = seed;
            this.initGame = initGame;
            this.finalGame = finalGame;
            this.actions = actions;
            this.gameResult = gameResult;
        }

        public Game getFinalGame() {
            return finalGame;
        }

        public void setFinalGame(Game finalGame) {
            this.finalGame = finalGame;
        }

        public Long getSeed() {
            return seed;
        }

        public void setSeed(Long seed) {
            this.seed = seed;
        }

        public Game getInitGame() {
            return initGame;
        }

        public void setInitGame(Game initGame) {
            this.initGame = initGame;
        }

        public List<AbstractAction> getActions() {
            return actions;
        }

        public void setActions(List<AbstractAction> actions) {
            this.actions = actions;
        }

        public GameResult getGameResult() {
            return gameResult;
        }

        public void setGameResult(GameResult gameResult) {
            this.gameResult = gameResult;
        }

        @Override
        public String toString() {
            return "SimulateForMechanismParam{" +
                    "seed=" + seed +
                    ", gameResult=" + gameResult +
                    "actions=" + actions +
                    '}';
        }
    }

}
