package guide;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import gui.AbstractGUIManager;
import gui.GUI;
import gui.GamePanel;
import guide.param.Question;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import players.PlayerType;
import players.human.ActionController;
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

/**
 * This class is used to control the front-end display of the tutorial.
 */
public class InterfaceTech extends GUI {

    private GameType gameType;

    /**
     * This object is different from the GameRunning object below. GameResult records the final outcome of the game,
     * whereas GameRunning resets the game state on the GameResult object (clearing History).
     * This allows us to re-execute each action step-by-step,
     * ensuring that GameRunning can return to the state of GameResult based on the actions recorded in History.
     */
    public Game gameResult;

    public Game gameRunning;

    private Long seed;

    private AbstractGUIManager gui;

    private Timer guiUpdater;

    private GamePanel gamePanel;

    private JPanel wrapper;

    /**
     * used in SwingWorker for controlling the interface display
     * This ensures that the interface can be refreshed in real time after each Action is executed,
     * so that the user can see it, otherwise the code will quickly execute all the actions。
     */
    private final Object lock = new Object();

    private JPanel buttonPanel;
    private JButton next;
    private boolean paused, end;
    private ActionController humanInputQueue;

    /**
     * In guide process, it isn't the same as that in gs
     */
    private int currentPlayer;

    private Thread gameThread;

    private JButton replay;

    private List<AbstractPlayer> playersForSimulate;

    private QuestionService questionService;

    private ActionListener startTrigger;

    private PreGameState preGameState;

    public InterfaceTech(Long seed, Game gameRunning) {
        init(seed, gameRunning);
    }

    public void init(Long seed, Game gameResult) {
        this.gameResult = gameResult;
        this.seed = seed;
        this.gameType = gameResult.getGameType();
        this.gamePanel = new GamePanel();
        this.questionService = new QuestionService(this.gameType);
        this.next = new JButton("Next");
        this.replay = new JButton("Replay!");
        this.end = this.paused = false;
        this.humanInputQueue = new ActionController();
        this.playersForSimulate = new ArrayList<>();

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
                this.preGameState = GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex);
                next.setEnabled(false);
                end = false;
                buttonPanel.remove(replay);
                GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex).resetIndexx();
                gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
                GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex).resetIndexx();
                gameRunning.reset(playersForSimulate);
                gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
                setFrameProperties();
                int c = new Random(System.currentTimeMillis()).nextInt(50, 150);
                guiUpdater = new Timer(c, event -> updateGUI());
                guiUpdater.start();
                buildInterface(false);
                gameRunning.setPaused(paused);
                DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300, 200, this.preGameState.getSimulateInfo().getStartText()));
                if (Objects.nonNull(preGameState) && Objects.nonNull(preGameState.getSimulateInfo())) {;
                    List<Pair<Long, AbstractAction>> playerIdAndActions = preGameState.getPlayerIdAndActions();
                    for (int i = 0; i < preGameState.getSimulateInfo().getBeginActionIndex(); i++) {
                        gameRunning.processOneAction(playerIdAndActions.get(i).b);
                        updateGUI();
                    }
                }
                buildInterface(false);
                updateGUI();
                gameRunning.run();
                end = true;
                buttonPanel.add(replay);
                next.setEnabled(true);
                guiUpdater.stop();
                buttonPanel.revalidate();
                buttonPanel.repaint();
                buildInterface(false);
                updateGUI();
                GuideContext.deckForSimulateIndex += 1;
            };
            gameThread = new Thread(runnable);
            gameThread.start();
        };
    }

    /**
     * Prepare to display mechanism part
     * @param purpose which mechanisms do we want to show?
     */
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
        gamePanel.revalidate();
        gamePanel.setVisible(true);
        gamePanel.repaint();
        setFrameProperties();

        /**
         * Mechanism part is implemented in each GUIManager class
         */
        gui = gameType.createGUIManagerForGuide(gamePanel, gameRunning, purpose, this);
    }

    /**
     * Construct the interface, the disadvantage is that the page will flash the screen every time it is called
     * @param reset whether initial gameRunning object
     */
    public void buildInterface(boolean reset) {
        if (reset) {
            gameRunning = resetActionForGame();
            gameRunning.setTurnPause(1000);
        }
        gamePanel = new GamePanel();
        gamePanel.setVisible(false);
        gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) :
                gameType.createGUIManager(gamePanel, gameRunning, null);

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

    /**
     * First step for introducing the game rule
     */
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

    /**
     * Avoid frequent UI updates on the main thread
     * done() An effective way for all actions to be executed is to jump to the next game module
     * @param actions Actions need to perform one by one
     * @return SwingWorker Object
     */
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
                    gameRunning.getGameState().getDialogs().forEach(x -> {
                        x.setLocationRelativeTo(InterfaceTech.this);
                        x.setVisible(true);
                    });
                    updateGUI();
                    gameRunning.getGameState().setDialogs(new ArrayList<>());
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

    /**
     * Display game result in Mechanism part
     */
    public void showGameResult() {
        StringBuilder text = new StringBuilder();
        text.append("Game is over! ");
        for (int i = 0; i < gameRunning.getGameState().getPlayerResults().length; i++) {
            text.append("Player").append(i).append(" ").append(gameRunning.getGameState().getPlayerResults()[i]).append(",");
        }
        DialogUtils.show(DialogUtils.create(InterfaceTech.this,
                "Game Guide", Boolean.TRUE, 300, 200, text.toString()));
    }

    public void updateGUI() {
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
        }

    }

    public void display() {
        try {
            buildInterface(true);
            beforeGameIntroduce();
            initIntroduceCards("All");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively displays all game results
     */
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
            DialogUtils.show(DialogUtils.create(InterfaceTech.this, "Game Guide", Boolean.TRUE, 300,
                    200, "Now, Let's learn some game results"));
        }


        PreGameState preGameState = GuideContext.deckForResult.get(GuideContext.deckForResultIndex);
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
            playersForSimulate = new ArrayList<>(this.preGameState.getSimulateInfo().getPlayers().stream()
                    .map(x -> PlayerType.valueOf(x).createPlayerInstance(seed, humanInputQueue, null)).toList());
            replay.addActionListener(e -> {
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
                gameRunning = gameType.createGameInstance(playersForSimulate.size(), null);
                gameRunning.reset(playersForSimulate);
                gameRunning.setTurnPause(200);
                gui = (humanInputQueue != null) ? gameType.createGUIManager(gamePanel, gameRunning, humanInputQueue) : null;
                setFrameProperties();
                buildInterface(false);
                end = false;
                getContentPane().remove(replay);
                startTrigger.actionPerformed(e);
                simulate();
            });
        }
    }

    /**
     * Simulation Part
     */
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
        playersForSimulate = new ArrayList<>(preGameState.getSimulateInfo().getPlayers().stream()
                .map(x -> PlayerType.valueOf(x).createPlayerInstance(seed, humanInputQueue, null)).toList());

        // When novices finish all actions
        if (end) {
            buttonPanel.add(replay);
        }
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        updateGUI();
    }

    /**
     * The final module of tutorial -- quiz
     * @param questions read from JSON
     * @param keys questions size
     * @param indexx now located in which question
     */
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

    /**
     * Reset gameResult object to initial state
     * @return game object
     */
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
