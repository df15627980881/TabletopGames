package guide;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import core.components.FrenchCard;
import games.GameType;
import games.loveletter.LoveLetterGameState;
import gui.AbstractGUIManager;
import gui.GUI;
import gui.GamePanel;
import gui.views.ComponentView;
import guide.param.Question;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import players.PlayerType;
import players.human.ActionController;
import players.mcts.MCTSPlayer;
import players.simple.RandomPlayer;
import utilities.JSONUtils;
import utilities.Pair;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class InterfaceTech extends GUI {

    private JDialog tutorialDialog;

    private GameType gameType;

    public Game gameResult;

    public Game gameRunning;

    public List<GuideGenerator.SimulateForMechanismParam> simulateForMechanisms;

    private Long seed;

    private AbstractGUIManager gui;

    private Timer guiUpdater;

    private GamePanel gamePanel;

    private JPanel wrapper;

    private boolean showActionFeedback;

    private boolean showLastActions;

    /**
     * whether show recommended actions for tyro.
     */
    private boolean showPreviousActions;
    private int showLastActionsCount;
    private int showPreviousActionsCount;

    private final Object lock = new Object();
    private final Object lockResult = new Object();

    private ComponentView introduceEachCards;

    private JPanel buttonPanel;
    private JButton next;
    private boolean paused, started, end;
    private ActionController humanInputQueue;

    /**
     * In guide process, it isn't the same as that in gs
     */
    private int currentPlayer;

    private Thread gameThread;

    private JButton replay;

    private List<Game> gamesForPreviousActionShow;

    private List<AbstractPlayer> playersForSimulate;

    private QuestionService questionService;

    private ActionListener startTrigger;

    private boolean isFirstEnterStrategy;

    private PreGameState preGameState;


    public InterfaceTech() {
    }

    public InterfaceTech(Long seed, Game gameRunning, List<GuideGenerator.SimulateForMechanismParam> simulateForMechanisms, List<Game> gamesForPreviousActionShow) {
        init(seed, gameRunning, simulateForMechanisms, gamesForPreviousActionShow);
    }

    public void init(Long seed, Game gameResult, List<GuideGenerator.SimulateForMechanismParam> simulateForMechanisms, List<Game> gamesForPreviousActionShow) {
//        frame = new JFrame();
//        frame.setTitle(gameType.toString() + " Guide");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(300, 200);
//        frame.setLocationRelativeTo(null);
        this.gameResult = gameResult;
        this.seed = seed;
        this.gameType = gameResult.getGameType();
        this.simulateForMechanisms = simulateForMechanisms;
        this.gamePanel = new GamePanel();
        this.showActionFeedback = true;
        this.showLastActionsCount = 0;
        this.showPreviousActionsCount = 0;
        this.showLastActions = false;
        this.showPreviousActions = false;
        this.gamesForPreviousActionShow = gamesForPreviousActionShow;
        this.questionService = new QuestionService(this.gameType);
        this.next = new JButton("Next");
        this.replay = new JButton("Replay!");
        this.end = this.paused = this.started = false;
        this.humanInputQueue = new ActionController();
        this.playersForSimulate = new ArrayList<>();
        this.isFirstEnterStrategy = true;

        GuideContext.guideStage = GuideContext.GuideState.GUIDE_CLOSE;
        GuideContext.frame = InterfaceTech.this;
        GuideContext.deckForMechanism = PreGameStateUtils.get(gameType, "data/preGameState/" + gameType.name() + "/Mechanism/" + 0 + ".json");
        GuideContext.deckForResult = new ArrayList<>();
        GuideContext.deckForSimulate = new ArrayList<>();
        GuideContext.caller = new SimulateConditionCaller();
        GuideContext.deckForResultIndex = 0;
        GuideContext.deckForSimulateIndex = 0;

        File[] files = JSONUtils.getAllFile("data/preGameState/" + gameType.name() + "/GameResult");
        if (files == null || files.length == 0) {
            System.out.println("No gameResult game");
        } else {
            for (File file : files) {
                GuideContext.deckForResult.add(PreGameStateUtils.get(gameType, "data/preGameState/" + gameType.name() + "/GameResult/" + file.getName()));
            }
        }

        files = JSONUtils.getAllFile("data/preGameState/" + gameType.name() + "/Simulate");
        if (files == null || files.length == 0) {
            System.out.println("No gameResult game");
        } else {
            for (File file : files) {
                GuideContext.deckForSimulate.add(PreGameStateUtils.get(gameType, "data/preGameState/" + gameType.name() + "/Simulate/" + file.getName()));
            }
        }

        startTrigger = e -> {
            Runnable runnable = () -> {
                System.out.println("GuideContext.deckForSimulateIndex: " + GuideContext.deckForSimulateIndex);
                this.preGameState = GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex);
                next.setEnabled(false);
                end = false;
                buttonPanel.remove(replay);
                GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex).resetIndexx();
                gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
                GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex).resetIndexx();
                gameRunning.reset(playersForSimulate);
                gameRunning.setTurnPause(500);
                gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
                setFrameProperties();
                guiUpdater = new Timer(100, event -> updateGUI());
                guiUpdater.start();
                buildInterface(false);
                gameRunning.setPaused(paused);
//                System.out.println("GuideContext.deckForSimulateIndex: " + GuideContext.deckForSimulateIndex);
//                if (preGameState.getIndexx() == 0) {
//                    DialogUtils.show(DialogUtils.create(GuideContext.frame, "Game Guide", Boolean.TRUE, 300, 200, preGameState.getSimulateInfo().getStartText()));
//                }
                DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, this.preGameState.getSimulateInfo().getStartText()));
                if (Objects.nonNull(preGameState) && Objects.nonNull(preGameState.getSimulateInfo())) {;
                    List<Pair<Long, AbstractAction>> playerIdAndActions = preGameState.getPlayerIdAndActions();
                    for (int i = 0; i < preGameState.getSimulateInfo().getBeginActionIndex(); i++) {
//                    for (int i = 0; i < 0; i++) {
//                        LoveLetterGameState gs = (LoveLetterGameState) gameRunning.getGameState();
//                        gs.getPlayerHandCards().get(1).getComponents().forEach(System.out::println);
//                        System.out.println("---");
                        gameRunning.processOneAction(playerIdAndActions.get(i).b);
                        updateGUI();
                    }
                }
                updateGUI();
                System.out.println(gameRunning.getGameState().getGameStatus());
                gameRunning.run();
                end = true;
                buttonPanel.add(replay);
                next.setEnabled(true);
                guiUpdater.stop();
                buttonPanel.revalidate();
                buttonPanel.repaint();
                updateGUI();
                GuideContext.deckForSimulateIndex += 1;
            };
            gameThread = new Thread(runnable);
            gameThread.start();
        };
    }

    public void initIntroduceCards(String purpose) {
        GuideContext.guideStage = GuideContext.GuideState.SHOW_MECHANISM_TURN;
        gameRunning = resetActionForGame();
        gameType = gameRunning.getGameType();
        gamePanel = new GamePanel();
        gamePanel.setVisible(false);

        if (Objects.isNull(buttonPanel)) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        }
        next = new JButton("Next");
        buttonPanel.add(next);

        if (Objects.nonNull(wrapper)) {
            getContentPane().remove(wrapper);
        }
        wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.add(gamePanel);
        getContentPane().add(wrapper, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
//        introduceEachCards.setBorder(title);
        gamePanel.revalidate();
        gamePanel.setVisible(true);
        gamePanel.repaint();
        setFrameProperties();
//        this.introduceEachCards.setBorder(title);
//        next.addActionListener(e -> {
//            getContentPane().remove(buttonPanel);
//            runSecondPart();
//        });

        gui = gameType.createGUIManagerForGuide(gamePanel, gameRunning, purpose, this);
    }

    public void buildInterface(boolean reset) {
//        JPanel panel = new JPanel();
        if (reset) {
            gameRunning = resetActionForGame();
            gameRunning.setTurnPause(1000);
        }
        gamePanel = new GamePanel();
        gamePanel.setVisible(false);
//        gui = gameType.createGUIManager(gamePanel, gameRunning, null);
        gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : gameType.createGUIManager(gamePanel, gameRunning, null);
//        updateGUI();
        if (Objects.nonNull(wrapper)) {
            getContentPane().remove(wrapper);
        }
        wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.add(gamePanel);

        getContentPane().add(wrapper, BorderLayout.CENTER);
        gamePanel.revalidate();
        gamePanel.setVisible(true);
        gamePanel.repaint();
        setFrameProperties();
    }

//    private void processActions() {
//        listenForDecisions();
//        List<Pair<Integer, AbstractAction>> histories = gameResult.getGameState().getHistory();
//        for (Pair<Integer, AbstractAction> pair : histories) {
//            synchronized (gameRunning) {
//                gameRunning.aaa(pair.b);
//                gameRunning.notifyAll();
//                SwingUtilities.invokeLater(this::updateGUI);
//            }
//        }
//        System.out.println(333);
//    }

    private void beforeGameIntroduce() {
        try {
            GuideContext.guideStage = GuideContext.GuideState.SHOW_GAME_RULE;
            Method getRuleTextMethod = gui.getClass().getMethod("getRuleText");
            String ruleText = (String) getRuleTextMethod.invoke(gui);
            DialogUtils.show(DialogUtils.createFirstStep(InterfaceTech.this, "Game Guide", Boolean.TRUE,
                    1500, 750, "", gameType.name(), ruleText));
        } catch (NoSuchMethodException e) {
            System.out.println("getRuleText method is not found in the GUI manager class.");
        } catch (IllegalAccessException e) {
            System.out.println("getRuleText method cannot be accessed.");
        } catch (InvocationTargetException e) {
            System.out.println("getRuleText method threw an exception: " + e.getCause().getMessage());
        }
    }

    public SwingWorker<Void, AbstractAction> processSpecificActions(List<AbstractAction> actions) {
        SwingWorker<Void, AbstractAction> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ActionListener actionListener : next.getActionListeners()) {
                    next.removeActionListener(actionListener);
                }
                next.addActionListener(e -> {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                });
                for (AbstractAction action: actions) {
                    synchronized (gameRunning) {
                        currentPlayer = gameRunning.getGameState().getCurrentPlayer();
                        gameRunning.processOneAction(action);
                        publish(action);
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }
                        }
                    }

                }
                return null;
            }

            @Override
            protected void process(List<AbstractAction> chunks) {
                for (AbstractAction action : chunks) {
//                    System.out.println(action.getString(gameRunning.getGameState()));
                    ArrayList<JDialog> dialogs = action.createDialogWithFeedbackForNewbie(
                            InterfaceTech.this, gameRunning.getGameState(), currentPlayer);
                    if (CollectionUtils.isNotEmpty(dialogs)) {
                        dialogs.forEach(dialog -> {
                            dialog.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    gamePanel.revalidate();
                                    gamePanel.repaint();
                                }
                            });

                        });
                        dialogs.forEach(dialog -> {
                            dialog.setLocationRelativeTo(InterfaceTech.this);
                            dialog.setVisible(true);
                        });
                    }
//                    updateGUI();
                    gameRunning.getGameState().getDialogs().forEach(x -> {
                        x.setLocationRelativeTo(InterfaceTech.this);
                        x.setVisible(true);
//                        updateGUI();
                    });
                    updateGUI();
                    gameRunning.getGameState().setDialogs(new ArrayList<>());
//                    synchronized (lock) {
//                        lock.notifyAll();
//                    }
                    break;
                }
            }

            @Override
            protected void done() {
                updateGUI();
            }
        };
        return worker;
    }

    @Deprecated
    public SwingWorker<Void, AbstractAction> processActionsAndIntroduce() {
//        listenForDecisions();
        SwingWorker<Void, AbstractAction> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                for (Pair<Integer, AbstractAction> pair : gameResult.getGameState().getHistory()) {

                    synchronized (gameRunning) {
//                        if (pair.b.equals(Countless))
                        currentPlayer = gameRunning.getGameState().getCurrentPlayer();
                        gameRunning.processOneAction(pair.b);
//                        initDialog();
//                        gameRunning.notifyAll();
                        Thread.sleep(2000);
                        publish(pair.b);
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }
                        }
                    }

                }
                return null;
            }

            @Override
            protected void process(List<AbstractAction> chunks) {
                if (showActionFeedback) {
                    for (AbstractAction action : chunks) {
                        if (gameRunning.getGameState().getRoundCounter() >= 1) {
                            updateGUI();
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                            break;
                        }
                        ArrayList<JDialog> dialogs = action.createDialogWithFeedbackForNewbie(
                                InterfaceTech.this, gameRunning.getGameState(), currentPlayer);
                        if (CollectionUtils.isNotEmpty(dialogs)) {
                            dialogs.forEach(dialog -> {
                                dialog.setLocationRelativeTo(InterfaceTech.this);
                                dialog.setVisible(true);
                            });
                        }
//                    updateGUI();
                        gameRunning.getGameState().getDialogs().forEach(x -> {
                            x.setLocationRelativeTo(InterfaceTech.this);
                            x.setVisible(true);
//                        updateGUI();
                        });
                        updateGUI();
                        gameRunning.getGameState().setDialogs(new ArrayList<>());
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                } else if (showLastActions) {
                    if (showLastActionsCount + 7 > gameResult.getGameState().getHistory().size()) {
                        updateGUI();
                    } else {
                        showLastActionsCount += 1;
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                } else if (showPreviousActions) {
                    if (showPreviousActionsCount <= 7) {
                        showPreviousActionsCount += 1;
                        updateGUI();
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                } else {
                    updateGUI();
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            }

            @Override
            protected void done() {
//                    try {
//                        get();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//                    System.out.println(333);
                updateGUI();
                showGameResult();
            }
        };
//        worker.execute();
        return worker;
    }

    public void showGameResult() {
        StringBuilder text = new StringBuilder();
        text.append("Game is over! ");
        for (int i = 0; i < gameRunning.getGameState().getPlayerResults().length; i++) {
            text.append("Player").append(i).append(" ").append(gameRunning.getGameState().getPlayerResults()[i]).append(",");
        }

//        System.out.println(text.toString());
//        synchronized (lockResult) {
//            try {
//                System.out.println("222");
//                lockResult.wait();
//                System.out.println("333");
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        DialogUtils.show(DialogUtils.create(InterfaceTech.this,
                "Game Guide", Boolean.TRUE, 300, 200, text.toString()));
//            updateGUI();
//        }
    }

//    private void listenForDecisions() {
//        // add a listener to detect every time an action has been taken
//        gameRunning.addListener(new MetricsGameListener() {
//            @Override
//            public void onEvent(evaluation.metrics.Event event) {
//                if (event.type == Event.GameEvent.ACTION_TAKEN)
//                    updateSampleActions(event.state.copy(), gameRunning);
//            }
//        });
//
//        // and then do this at the start of the game
//        updateSampleActions(gameRunning.getGameState(), gameRunning);
//    }

//    private void updateSampleActions(AbstractGameState state, Game currentGame) {
//        if (state.isNotTerminal() && !currentGame.isHumanToMove()) {
//            int nextPlayerID = state.getCurrentPlayer();
//            AbstractPlayer nextPlayer = currentGame.getPlayers().get(nextPlayerID);
//            nextPlayer.getAction(state, nextPlayer.getForwardModel().computeAvailableActions(state, nextPlayer.getParameters().actionSpace));
//
//            JFrame AI_debug = new JFrame();
//            AI_debug.setTitle(String.format("Player %d, Tick %d, Round %d, Turn %d",
//                    nextPlayerID,
//                    currentGame.getTick(),
//                    state.getRoundCounter(),
//                    state.getTurnCounter()));
//            Map<AbstractAction, Map<String, Object>> decisionStats = nextPlayer.getDecisionStats();
//            if (decisionStats.size() > 1) {
//                AITableModel AIDecisions = new AITableModel(nextPlayer.getDecisionStats());
//                JTable table = new JTable(AIDecisions);
//                table.setAutoCreateRowSorter(true);
//                table.setDefaultRenderer(Double.class, (table1, value, isSelected, hasFocus, row, column) -> new JLabel(String.format("%.2f", (Double) value)));
//                JScrollPane scrollPane = new JScrollPane(table);
//                table.setFillsViewportHeight(true);
//                AI_debug.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//                AI_debug.add(scrollPane);
//                AI_debug.revalidate();
//                AI_debug.pack();
//                AI_debug.setVisible(true);
//            }
//        }
//    }


    public void updateGUI() {
//        AbstractGameState gameState = gameRunning.getGameState().copy();
//        int currentPlayer = gameState.getCurrentPlayer();
//        AbstractPlayer player = gameRunning.getPlayers().get(currentPlayer);
//        if (gui != null) {
//            gui.update(player, gameState, false);
//        }

        AbstractGameState gameState = gameRunning.getGameState().copy();
        int currentPlayer = gameState.getCurrentPlayer();
        AbstractPlayer player = gameRunning.getPlayers().get(currentPlayer);
        if (gui != null) {
            gui.update(player, gameState, gameRunning.isHumanToMove());
            if (!gameRunning.isHumanToMove()) {
                // in this case we allow a human to override an AI decision
                try {
                    if (humanInputQueue.hasAction()) {
                        gameRunning.getForwardModel().next(gameState, humanInputQueue.getAction());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!gameRunning.isHumanToMove())
                humanInputQueue.reset(); // clear out any actions clicked before their turn
            // 调用这两个会导致visibility重置，原因未知...
//            this.revalidate();
//            this.repaint();
        }

    }

    public void display() {
        try {
            buildInterface(true);
//            guiUpdater = new Timer(2000, event -> updateGUI());
//            guiUpdater.start();
//            return;
            beforeGameIntroduce();
            initIntroduceCards("All");
//            simulate();
//            runGameResult();

//                lockResult.wait();
//                showGameResult();

//            introduceComponents();
//            guiUpdater.stop();
//            updateGUI();
//            System.out.println("dsajkd");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void runSecondPart() {
        DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200,
                "Now let's go through the game step by step, trying to understand what happens with each Action（・∀・）"));
        buildInterface(true);
        updateGUI();
        SwingWorker<Void, AbstractAction> worker = processActionsAndIntroduce();
        worker.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                showLastActions = true;
                runTertiaryPart(simulateForMechanisms.get(0), 0);
            }
        });
        worker.execute();
    }

    public void runGameResult() {
        if (GuideContext.deckForResultIndex == 0) {
            for (ActionListener actionListener : next.getActionListeners()) {
                next.removeActionListener(actionListener);
            }
            next.addActionListener(e -> {
                GuideContext.deckForResultIndex += 1;
                runGameResult();
            });
            GuideContext.guideStage = GuideContext.GuideState.SHOW_GAME_RESULT;
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, "Now, Let's learn some game results"));
        }


        PreGameState preGameState = GuideContext.deckForResult.get(GuideContext.deckForResultIndex);
//        Set<Long> playerIds = new LinkedHashSet<>(preGameState.getPlayerIdAndActions().stream().map(x -> x.a).collect(Collectors.toSet()));
        List<AbstractAction> actions = new ArrayList<>(preGameState.getPlayerIdAndActions().stream().map(x -> {
            Pair<Long, AbstractAction> y = (Pair<Long, AbstractAction>) x;
            return y.b;
        }).toList());
        List<AbstractPlayer> players = new ArrayList<>();
        for (int i=0; i<preGameState.getPlayerCount(); ++i) {
            players.add(new RandomPlayer());
        }
        gameResult = Game.runOne(gameType, null, players, System.currentTimeMillis(), false, null, null, 1);
        preGameState.resetIndexx();
        buildInterface(true);
        updateGUI();
//        showActionFeedback = false;
        actions.forEach(x -> {
            gameRunning.processOneAction(x);
            updateGUI();
        });
        if (StringUtils.isNotBlank(preGameState.getGameResultDesc())) {
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, preGameState.getGameResultDesc()));
        }
        if (GuideContext.deckForResultIndex == GuideContext.deckForResult.size() - 1) {
            GuideContext.guideStage = GuideContext.GuideState.SIMULATE_ACTIONS_BY_PLAYERS;
            for (ActionListener actionListener : next.getActionListeners()) {
                next.removeActionListener(actionListener);
            }
            this.preGameState = GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex);
//            for (int i=0; i<preGameState2.getPlayerCount() - 1; ++i) {
//                playersForSimulate.add(PlayerType.valueOf("HumanGUIPlayer").createPlayerInstance(seed, humanInputQueue, null));
//            }
//            playersForSimulate.add(new MCTSPlayer());
            playersForSimulate = new ArrayList<>(this.preGameState.getSimulateInfo().getPlayers().stream()
                    .map(x -> PlayerType.valueOf(x).createPlayerInstance(seed, humanInputQueue, null)).toList());
            replay.addActionListener(e -> {
//                if (isFirstEnterStrategy) {
//                    DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, this.preGameState.getSimulateInfo().getStartText()));
//                    isFirstEnterStrategy = false;
//                }
                GuideContext.deckForSimulateIndex -= 1;
                Assert.assertTrue(GuideContext.deckForSimulateIndex >= 0);
                end = false;
                getContentPane().remove(replay);
                gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
                gameRunning.reset(playersForSimulate);
                gameRunning.setTurnPause(200);
                gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
                setFrameProperties();
                buildInterface(false);
                updateGUI();
                startTrigger.actionPerformed(e);
                updateGUI();
                next.setEnabled(false);
            });
            next.addActionListener(e -> {
//                isFirstEnterStrategy = true;
                gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
                gameRunning.reset(playersForSimulate);
                gameRunning.setTurnPause(200);
                gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
                setFrameProperties();
                buildInterface(false);
                end = false;
                getContentPane().remove(replay);
//                if (isFirstEnterStrategy) {
//                    DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, "Now, here are some common playing strategies recommended to you, please try to have two players win the dealer at the same time!"));
//                    DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, this.preGameState.getSimulateInfo().getStartText()));
//                    isFirstEnterStrategy = false;
//                }
                startTrigger.actionPerformed(e);
                simulate();
            });
        }
    }

    public void prepareSimulate() {
        GuideContext.guideStage = GuideContext.GuideState.SIMULATE_ACTIONS_BY_PLAYERS;
        for (ActionListener actionListener : next.getActionListeners()) {
            next.removeActionListener(actionListener);
        }
        PreGameState preGameState2 = GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex);
        for (int i=0; i<preGameState2.getPlayerCount() - 1; ++i) {
            playersForSimulate.add(PlayerType.valueOf("HumanGUIPlayer").createPlayerInstance(seed, humanInputQueue, null));
        }
        playersForSimulate.add(new MCTSPlayer());
        replay.addActionListener(e -> {
            isFirstEnterStrategy = true;
            GuideContext.deckForSimulateIndex -= 1;
            Assert.assertTrue(GuideContext.deckForSimulateIndex >= 0);
            end = false;
            getContentPane().remove(replay);
            gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
            gameRunning.reset(playersForSimulate);
            gameRunning.setTurnPause(200);
            gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
            setFrameProperties();
            buildInterface(false);
            startTrigger.actionPerformed(e);
            next.setEnabled(false);
        });
        next.addActionListener(e -> {
            gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
            gameRunning.reset(playersForSimulate);
            gameRunning.setTurnPause(200);
            gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
            setFrameProperties();
            buildInterface(false);
            end = false;
            getContentPane().remove(replay);
//            if (isFirstEnterStrategy) {
//                DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, "Now, here are some common playing strategies recommended to you, please try to have two players win the dealer at the same time!"));
//                isFirstEnterStrategy = false;
//            }
            startTrigger.actionPerformed(e);
            simulate();
        });
    }

    @Deprecated
    public void runTertiaryPart(GuideGenerator.SimulateForMechanismParam param, int indexx) {
        if (indexx == 0) {
            GuideContext.guideStage = GuideContext.GuideState.SHOW_GAME_RESULT;
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, "Now, Let's learn some game results"));
        }
        if (param == null) {
            return;
        }
        gameResult = param.getFinalGame();
        for (CoreConstants.GameResult playerResult : gameResult.getGameState().getPlayerResults()) {
            System.out.println(indexx + " " + playerResult);
        }
        gameRunning = param.getInitGame();
        buildInterface(false);
        updateGUI();
        showActionFeedback = false;
        DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, "Game Result: " + param.getGameResult().toString()));
        // Just show last 7 actions
        SwingWorker<Void, AbstractAction> worker = processActionsAndIntroduce();
        worker.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                if (indexx + 1 < simulateForMechanisms.size()) {
                    runTertiaryPart(simulateForMechanisms.get(indexx + 1), indexx + 1);
                } else {
                    getPreviousRecommendAction();
                }
            }
        });
        worker.execute();
    }

    public void simulate() {
        if (GuideContext.deckForSimulateIndex == GuideContext.deckForSimulate.size() - 1) {
            for (ActionListener actionListener : next.getActionListeners()) {
                next.removeActionListener(actionListener);
            }
            next.addActionListener(e -> {
                simulate();
                GuideContext.deckForSimulateIndex += 1;
            });
        }
        if (GuideContext.deckForSimulateIndex == GuideContext.deckForSimulate.size()) {
            for (ActionListener actionListener : next.getActionListeners()) {
                next.removeActionListener(actionListener);
            }
            runQuestions(questionService.getQuestions(), questionService.getQuestions().keySet().stream().toList(), 0);
            return;
        }
        getContentPane().removeAll();
        PreGameState preGameState = GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex);
//        List<AbstractPlayer> players = new ArrayList<>();
//        for (int i=0; i<preGameState.getPlayerCount() - 1; ++i) {
//            players.add(PlayerType.valueOf("HumanGUIPlayer").createPlayerInstance(seed, humanInputQueue, null));
//        }
//        players.add(new MCTSPlayer());
        playersForSimulate = new ArrayList<>(preGameState.getSimulateInfo().getPlayers().stream()
                .map(x -> PlayerType.valueOf(x).createPlayerInstance(seed, humanInputQueue, null)).toList());
        if (end) {
            buttonPanel.add(replay);
            isFirstEnterStrategy = true;
        }
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        if (StringUtils.isNotBlank(preGameState.getStrategy())) {
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, preGameState.getStrategy()));
        }
        updateGUI();
    }

    /**
     *
     */
    public void getPreviousRecommendAction() {
        System.out.println("getPreviousRecommendAction start!");
        for (ActionListener actionListener : next.getActionListeners()) {
            next.removeActionListener(actionListener);
        }
        if (gameType == GameType.Blackjack) {
            DialogUtils.show(DialogUtils.createWithoutPack(InterfaceTech.this, "Game Guide", Boolean.TRUE,
                    1200, 300, "<html><p>Winning tactics in Blackjack require that the player play each hand in the optimum way, and such strategy always takes into account what the dealer's upcard is. When the dealer's upcard is a good one, a 7, 8, 9, 10-card, or ace for example, the player should not stop drawing until a total of 17 or more is reached. When the dealer's upcard is a poor one, 4, 5, or 6, the player should stop drawing as soon as he gets a total of 12 or higher. The strategy here is never to take a card if there is any chance of going bust. The desire with this poor holding is to let the dealer hit and hopefully go over 21. Finally, when the dealer's up card is a fair one, 2 or 3, the player should stop with a total of 13 or higher. With a soft hand, the general strategy is to keep hitting until a total of at least 18 is reached. Thus, with an ace and a six (7 or 17), the player would not stop at 17, but would hit.</p></html>"));
//            next.addActionListener(e -> {
//                runQuestions();
//            });
        }
        GuideContext.guideStage = GuideContext.GuideState.SHOW_ACTIONS_EXECUTED_BY_MCTS;
        DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE,
                300, 200, "Now let's look at some recommended actions from MCTS algorithm"));
        gameResult = gamesForPreviousActionShow.get(0);
        buildInterface(true);
        showLastActions = false;
        showPreviousActions = true;
        updateGUI();
        SwingWorker<Void, AbstractAction> worker = processActionsAndIntroduce();
        worker.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                getContentPane().removeAll();
                runQuestions(questionService.getQuestions(), questionService.getQuestions().keySet().stream().toList(), 0);
            }
        });
        worker.execute();
    }

    private void runQuestions(Map<String, Question> questions, List<String> keys, int indexx) {
        if (indexx >= keys.size()) {
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE,
                    300, 200, "This marks the end of the tutorial. I hope you found it useful."));
            System.exit(0);
            return;
        }
        if (indexx == 0) {
            getContentPane().removeAll();
            GuideContext.guideStage = GuideContext.GuideState.SHOW_QUESTIONS;
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE,
                    300, 200, "Now let's answer some questions to check if we make sense"));
        }
        Question question = questions.get(keys.get(indexx));
        String questionNo = keys.get(indexx);
        JLabel questionLabel = new JLabel(question.toString(questionNo));
        add(questionLabel, BorderLayout.NORTH);

        List<JRadioButton> options = new ArrayList<>();
        int correctAnswerIndex = 0;
        int i = 0;
        for (Question.Option option : question.getOptions()) {
            if (option.getOption().equals(question.getAnswer())) {
                correctAnswerIndex = i;
            } else {
                i += 1;
            }
            options.add(new JRadioButton(option.toString()));
        }

        ButtonGroup group = new ButtonGroup();
        options.forEach(group::add);

        JPanel panel = new JPanel(new GridLayout(4, 1));
        options.forEach(panel::add);
        add(panel, BorderLayout.CENTER);

        JButton submitButton = new JButton("Submit");
        add(submitButton, BorderLayout.SOUTH);

        int finalCorrectAnswerIndex = correctAnswerIndex;
        submitButton.addActionListener(e -> {
            if (options.get(finalCorrectAnswerIndex).isSelected()) {
                JOptionPane.showMessageDialog(null, "Correct answer!");
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect answer. The correct answer is "
                        + question.getOptions().get(finalCorrectAnswerIndex).toString());
            }
            remove(questionLabel);
            remove(panel);
            remove(submitButton);
            runQuestions(questions, keys, indexx + 1);
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        QuestionService questionService = new QuestionService(GameType.Blackjack);
        (new InterfaceTech()).runQuestions(questionService.getQuestions(), questionService.getQuestions().keySet().stream().toList(), 0);
    }

    public Game resetActionForGame() {
        AbstractGameState gameState = gameResult.getGameState().copy();
        gameState.reset(seed);
        return new Game(gameResult.getGameType(), gameResult.getPlayers(),
                gameType.createForwardModel(null, gameResult.getPlayers().size()), gameState);
    }

    public JButton getNext() {
        return next;
    }
}
