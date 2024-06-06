package guide;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import gui.AbstractGUIManager;
import gui.GUI;
import gui.GamePanel;
import gui.views.ComponentView;
import guide.param.Question;
import org.apache.commons.collections4.CollectionUtils;
import utilities.Pair;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InterfaceTech extends GUI {

    private JDialog tutorialDialog;

    private GameType gameType;

    private Game gameResult;

    public Game gameRunning;

    private List<GuideGenerator.SimulateForMechanismParam> simulateForMechanisms;

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

    /**
     * In guide process, it isn't the same as that in gs
     */
    private int currentPlayer;

    // TODO: 牌的数量，方向，庄位，软牌，爆牌，

    private List<Game> gamesForPreviousActionShow;

    private QuestionService questionService;

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
        gameResult.getGameState().setFrame(InterfaceTech.this);
    }

    public void initDialog() {
        // 创建对话框实例
        tutorialDialog = new JDialog(this, "游戏教程", true);
        tutorialDialog.setSize(300, 200);
        tutorialDialog.setLayout(new BorderLayout());

        JLabel label = new JLabel("<html><h2>欢迎来到游戏教程</h2><p>这里将介绍如何进行游戏。</p></html>");
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tutorialDialog.add(label, BorderLayout.CENTER);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> tutorialDialog.dispose());
        tutorialDialog.add(closeButton, BorderLayout.SOUTH);

        tutorialDialog.setLocationRelativeTo(this); // 相对主窗口居中
        tutorialDialog.setVisible(true);
    }

    public void initIntroduceCards(String purpose) {
        gameRunning = resetActionForGame();
        gameType = gameRunning.getGameType();
        gamePanel = new GamePanel();
        gamePanel.setVisible(false);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
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
        gui = gameType.createGUIManager(gamePanel, gameRunning, null);
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
        SwingWorker<Void, AbstractAction> worker = new SwingWorker<Void, AbstractAction>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (AbstractAction action: actions) {

                    synchronized (gameRunning) {
//                        if (pair.b.equals(Countless))
                        currentPlayer = gameRunning.getGameState().getCurrentPlayer();
                        gameRunning.processOneAction(action);
//                        initDialog();
//                        gameRunning.notifyAll();
                        Thread.sleep(2000);
                        publish(action);
                        System.out.println("TTT");
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
                System.out.println("GGG");
                for (AbstractAction action : chunks) {
                    updateGUI();
                    synchronized (lock) {
                        lock.notifyAll();
                    }
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
                                dialog.setLocationRelativeTo(InterfaceTech.this); // 相对主窗口居中
                                dialog.setVisible(true);
                            });
                        }
//                    updateGUI();
                        gameRunning.getGameState().getDialogs().forEach(x -> {
                            x.setLocationRelativeTo(InterfaceTech.this); // 相对主窗口居中
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

        System.out.println(text.toString());
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
        AbstractGameState gameState = gameRunning.getGameState().copy();
        int currentPlayer = gameState.getCurrentPlayer();
        AbstractPlayer player = gameRunning.getPlayers().get(currentPlayer);
        if (gui != null) {
            gui.update(player, gameState, false);
//            frame.revalidate();
//            frame.repaint();
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

    public void runTertiaryPart(GuideGenerator.SimulateForMechanismParam param, int indexx) {
//        System.out.println(seedsForWinWithBannedAction.size());
//        gameResult = Objects.requireNonNull(seedsForWinWithBannedAction.entrySet().stream().findFirst().orElse(null)).getValue().b;
        if (indexx == 0) {
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, "Now, Let's learn some game results"));
        }
        if (param == null) {
            return;
        }
//        if (param.getGameResult() == CoreConstants.GameResult.GAME_ONGOING) {
//            continue;
//        }
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

    /**
     *
     */
    public void getPreviousRecommendAction() {
        System.out.println("getPreviousRecommendAction start!");
        if (gameType == GameType.Blackjack) {
            DialogUtils.show(DialogUtils.createWithoutPack(InterfaceTech.this, "Game Guide", Boolean.TRUE,
                    1200, 300, "<html><p>Winning tactics in Blackjack require that the player play each hand in the optimum way, and such strategy always takes into account what the dealer's upcard is. When the dealer's upcard is a good one, a 7, 8, 9, 10-card, or ace for example, the player should not stop drawing until a total of 17 or more is reached. When the dealer's upcard is a poor one, 4, 5, or 6, the player should stop drawing as soon as he gets a total of 12 or higher. The strategy here is never to take a card if there is any chance of going bust. The desire with this poor holding is to let the dealer hit and hopefully go over 21. Finally, when the dealer's up card is a fair one, 2 or 3, the player should stop with a total of 13 or higher. With a soft hand, the general strategy is to keep hitting until a total of at least 18 is reached. Thus, with an ace and a six (7 or 17), the player would not stop at 17, but would hit.</p></html>"));
        }
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
            return;
        }
        if (indexx == 0) {
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

    private Game resetActionForGame() {
        AbstractGameState gameState = gameResult.getGameState().copy();
        gameState.reset(seed);
        gameState.setFrame(InterfaceTech.this);
        return new Game(gameResult.getGameType(), gameResult.getPlayers(),
                gameType.createForwardModel(null, gameResult.getPlayers().size()), gameState);
    }

    public JButton getNext() {
        return next;
    }
}
