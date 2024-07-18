package games.blackjack.gui;

import core.*;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import games.blackjack.BlackjackGameState;
import games.blackjack.BlackjackParameters;
import gui.AbstractGUIManager;
import gui.GamePanel;
import gui.IScreenHighlight;
import guide.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.testng.collections.Lists;
import players.human.ActionController;
import utilities.ImageIO;
import utilities.Pair;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BlackjackGUIManager extends AbstractGUIManager {
    final static int playerWidth = 300;
    final static int playerHeight = 130;
    final static int cardWidth = 90;
    final static int cardHeight = 115;

    int width, height;
    BlackjackPlayerView[] playerHands;

    int activePlayer = -1;

    Border highlightActive = BorderFactory.createLineBorder(new Color(47,132,220), 3);
    Border[] playerViewBorders;

    int indexx, playerId;

    JTabbedPane pane;

    public BlackjackGUIManager(GamePanel parent, Game game, String purpose, InterfaceTech frame) {
        super(parent, game, purpose);
//        Deck<FrenchCard> deck = PreGameStateUtils.getBlackjack().getDrawDeck();
//        preGameState = PreGameStateUtils.getBlackjack();

//        deck.shuffle(new Random(preGameState.getSeed()));
//        frenchCards = deck.getComponents();
        indexx = 0;
        playerId = 0;
        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        if (MechanismEnum.DIRECTION.getDescription().equals(purpose)) {
            generateDirectionGuide(frame, new HashMap<>(), false);
        } else if (MechanismEnum.POINT.getDescription().equals(purpose)) {
            generatePointGuide(frame);
        } else if (MechanismEnum.SOFT_HAND.getDescription().equals(purpose)) {
//            generateSoftHand(frame);
        } else if (MechanismEnum.ALL.getDescription().equals(purpose)) {
            generatePointGuide(frame);
//            generateDirectionGuide(frame, new HashMap<>(), true);
//            generatePointGuide();
//            generateSoftHand();
        }
    }

    public BlackjackGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> humanID) {
        super(parent, game, ac, humanID);

        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        if (game != null){
            AbstractGameState gameState = game.getGameState();
            if (gameState != null){
                JTabbedPane pane = new JTabbedPane();
                JPanel main = new JPanel();
                main.setOpaque(false);
                main.setLayout(new BorderLayout());
                JPanel rules = new JPanel();
                pane.add("Main", main);
                pane.add("Rules", rules);
                JLabel ruleText = new JLabel(getRuleText());
                rules.add(ruleText);
                rules.setBackground(new Color(43, 108, 25, 111));

                activePlayer = gameState.getCurrentPlayer();

                int nPlayers = gameState.getNPlayers();
                int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : nPlayers == 4 ? 3 : nPlayers <= 8 ? 4 : 5);
                double nVertAreas = 3.5;
                this.width = playerWidth * nHorizAreas;
                this.height = (int) (playerHeight* nVertAreas);
                ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));

                BlackjackGameState bjgs = (BlackjackGameState) gameState;
                BlackjackParameters bjgp = (BlackjackParameters) gameState.getGameParameters();

                parent.setBackground(ImageIO.GetInstance().getImage("data/FrenchCards/table-background.jpg"));

                playerHands = new BlackjackPlayerView[nPlayers];
                playerViewBorders = new Border[nPlayers];
                JPanel mainGameArea = new JPanel();
                mainGameArea.setOpaque(false);
                mainGameArea.setLayout(new BorderLayout());

                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                int next = 0;
                for (int i = 0; i < nPlayers; i++) {
                    BlackjackPlayerView playerHand = new BlackjackPlayerView(bjgs.getPlayerDecks().get(i), i, bjgp.getDataPath());
                    playerHand.setOpaque(false);

                    // Get agent name
                    String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];

                    // Create border, layouts and keep track of this view
                    TitledBorder title;
                    if (i == bjgs.getDealerPlayer()) {
                        title = BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DEALER [" + agentName + "]",
                                TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    } else {
                        title = BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "]",
                                TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    }
                    playerViewBorders[i] = title;
                    playerHand.setBorder(title);

                    sides[next].add(playerHand);
                    sides[next].setLayout(new GridBagLayout());
                    sides[next].setOpaque(false);
                    next = (next + 1) % (locations.length);
                    playerHands[i] = playerHand;
                }
                for (int i = 0; i < locations.length; i++) {
                    mainGameArea.add(sides[i], locations[i]);
                }

                // Top area will show state information
                JPanel infoPanel = createGameStateInfoPanel("Blackjack", gameState, width, defaultInfoPanelHeight);
                // Bottom area will show actions available
                JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false);

                // Add all views to frame
                main.add(mainGameArea, BorderLayout.CENTER);
                main.add(infoPanel, BorderLayout.NORTH);
                main.add(actionPanel, BorderLayout.SOUTH);

                pane.add("Main", main);
                pane.add("Rules", rules);

                parent.setLayout(new BorderLayout());
                parent.add(pane, BorderLayout.CENTER);
                parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
                parent.revalidate();
                parent.setVisible(true);
                parent.repaint();
            }
        }
    }

    @Override
    public int getMaxActionSpace() {
        return 15;
    }


    @Override
    protected JComponent createActionPanel(IScreenHighlight[] highlights, int width, int height, boolean boxLayout) {
        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        if (boxLayout) {
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        }

        actionButtons = new ActionButton[maxActionSpace];
        for (int i = 0; i < maxActionSpace; i++) {
            ActionButton ab = new ActionButton(ac, highlights);
            actionButtons[i] = ab;
            actionButtons[i].setVisible(false);
            actionPanel.add(actionButtons[i]);
        }
        for (ActionButton actionButton : actionButtons) {
            actionButton.informAllActionButtons(actionButtons);
        }

        JScrollPane pane = new JScrollPane(actionPanel);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        pane.setPreferredSize(new Dimension(width, height));
        if (boxLayout) {
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return pane;
    }

    @Override
    protected JPanel createGameStateInfoPanel(String gameTitle, AbstractGameState gameState, int width, int height) {
        JPanel gameInfo = new JPanel();
        gameInfo.setOpaque(false);
        gameInfo.setLayout(new BoxLayout(gameInfo, BoxLayout.Y_AXIS));
        gameInfo.add(new JLabel("<html><h1>" + gameTitle + "</h1></html>"));

        updateGameStateInfo(gameState);

        gameInfo.add(gameStatus);
        gameInfo.add(playerStatus);
        gameInfo.add(playerScores);
        gameInfo.add(gamePhase);
        gameInfo.add(turn);
        gameInfo.add(currentPlayer);

        gameInfo.setPreferredSize(new Dimension(width/2 - 10, height));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new FlowLayout());
        wrapper.add(gameInfo);

        historyInfo.setPreferredSize(new Dimension(width/2 - 10, height));
        historyContainer = new JScrollPane(historyInfo);
        historyContainer.setPreferredSize(new Dimension(width/2 - 25, height));
        historyInfo.setOpaque(false);
        historyContainer.setOpaque(false);
        historyContainer.getViewport().setBackground(new Color(43, 108, 25, 111));
//        historyContainer.getViewport().setOpaque(false);
        historyInfo.setEditable(false);
        wrapper.add(historyContainer);
        return wrapper;
    }

    @Override
    protected void _update(AbstractPlayer player, AbstractGameState gameState) {
        if (gameState != null) {
            if (gameState.getCurrentPlayer() != activePlayer) {
                playerHands[activePlayer].setCardHighlight(-1);
                activePlayer = gameState.getCurrentPlayer();
            }

            // Update decks and visibility
            BlackjackGameState bjgs = (BlackjackGameState) gameState;
            for (int i = 0; i < gameState.getNPlayers(); i++) {
                playerHands[i].update(bjgs);

                // Highlight active player
                if (i == gameState.getCurrentPlayer()) {
                    Border compound = BorderFactory.createCompoundBorder(highlightActive, playerViewBorders[i]);
                    playerHands[i].setBorder(compound);
                } else {
                    playerHands[i].setBorder(playerViewBorders[i]);
                }
            }
        }
    }

    public void generateDirectionGuide(InterfaceTech frame, Map<Integer, PartialObservableDeck<FrenchCard>> playerIdAndDeck, boolean isAll) {
        List<FrenchCard> frenchCards = new ArrayList<>(GuideContext.deckForMechanism.getDrawDeck().getComponents());
        if (game != null){
            AbstractGameState gameState = game.getGameState();
            if (gameState != null){
                pane = new JTabbedPane();
                JPanel main = new JPanel();
                main.setOpaque(false);
                main.setLayout(new BorderLayout());
                JPanel rules = new JPanel();
                pane.add("Main", main);
                pane.add("Rules", rules);
                JLabel ruleText = new JLabel(getRuleText());
                rules.add(ruleText);
                rules.setBackground(new Color(43, 108, 25, 111));

                activePlayer = gameState.getCurrentPlayer();

                int nPlayers = gameState.getNPlayers();
                int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : nPlayers == 4 ? 3 : nPlayers <= 8 ? 4 : 5);
                double nVertAreas = 3.5;
                this.width = playerWidth * nHorizAreas;
                this.height = (int) (playerHeight* nVertAreas);
                ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));

                BlackjackGameState bjgs = (BlackjackGameState) gameState;
                BlackjackParameters bjgp = (BlackjackParameters) gameState.getGameParameters();

                if (MapUtils.isEmpty(playerIdAndDeck)) {
                    DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                            "A game can consist of 2 to 7 players. In this example, we assume there are four players and one dealer, with Player " + bjgs.getDealerPlayer() + " acting as the dealer."));
                }

                parent.setBackground(ImageIO.GetInstance().getImage("data/FrenchCards/table-background.jpg"));

                playerHands = new BlackjackPlayerView[nPlayers];
                playerViewBorders = new Border[nPlayers];
                JPanel mainGameArea = new JPanel();
                mainGameArea.setOpaque(false);
                mainGameArea.setLayout(new BorderLayout());

                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                int next = 0;
                for (int i = 0; i < nPlayers; i++) {
                    BlackjackPlayerView playerHand = new BlackjackPlayerView(playerIdAndDeck.get(i), i, bjgp.getDataPath());
                    playerHand.setOpaque(false);

                    // Get agent name
                    String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];

                    // Create border, layouts and keep track of this view
                    TitledBorder title;
                    if (i == bjgs.getDealerPlayer()) {
                        title = BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DEALER [" + agentName + "]",
                                TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    } else {
                        title = BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "]",
                                TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    }
                    playerViewBorders[i] = title;
                    playerHand.setBorder(title);

                    sides[next].add(playerHand);
                    sides[next].setLayout(new GridBagLayout());
                    sides[next].setOpaque(false);
                    next = (next + 1) % (locations.length);
                    playerHands[i] = playerHand;
                }
                for (int i = 0; i < locations.length; i++) {
                    mainGameArea.add(sides[i], locations[i]);
                }

                // Top area will show state information
                JPanel infoPanel = createGameStateInfoPanel("Blackjack", gameState, width, defaultInfoPanelHeight);
                // Bottom area will show actions available
//                JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false);

                // Add all views to frame
                main.add(mainGameArea, BorderLayout.CENTER);
                main.add(infoPanel, BorderLayout.NORTH);
//                main.add(actionPanel, BorderLayout.SOUTH);

                pane.add("Main", main);
                pane.add("Rules", rules);

                parent.setLayout(new BorderLayout());
                parent.add(pane, BorderLayout.CENTER);
                parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
                parent.revalidate();
                parent.setVisible(true);
                parent.repaint();
            }
        }

        if (MapUtils.isEmpty(playerIdAndDeck)) {
            for (ActionListener actionListener : frame.getNext().getActionListeners()) {
                frame.getNext().removeActionListener(actionListener);
            }
            frame.getNext().addActionListener(e -> {
                if (indexx == game.getPlayers().size()) {
                    DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                            "So far all the cards in the first round have been issued."));
                }
                if (indexx >= (game.getPlayers().size()) << 1) {
                    DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                            "Now that all cards have been issued, the player to the left of the dealer begins to choose which Action to take."));
//                    parent.removeAll();
                    if (isAll) {
                        simulateActions(frame, playerIdAndDeck);
                    }
                    return;
                }
                if (indexx < game.getPlayers().size()) {
                    boolean[] visibility = new boolean[game.getPlayers().size()];
                    Arrays.fill(visibility, true);
                    PartialObservableDeck<FrenchCard> playerDeck = new PartialObservableDeck<>("HiddenForGuide", indexx % game.getPlayers().size(), visibility);
                    playerDeck.add(frenchCards.get(indexx));
                    playerIdAndDeck.put(indexx % game.getPlayers().size(), playerDeck);
                    indexx += 1;
                    parent.removeAll();
                    generateDirectionGuide(frame, playerIdAndDeck, isAll);
                } else {
                    PartialObservableDeck<FrenchCard> playerDeck = playerIdAndDeck.get(indexx%game.getPlayers().size());
                    playerDeck.add(frenchCards.get(indexx));
                    if (indexx == ((game.getPlayers().size()) << 1) - 1) {
                        boolean[] visibility = new boolean[game.getPlayers().size()];
                        Arrays.fill(visibility, true);
                        visibility[0] = false;
                        List<boolean[]> elementVisibility = (LinkedList<boolean[]>) playerDeck.getElementVisibility();
                        // Because PartialObservableDeck#add method add the element at the first place......
                        elementVisibility.set(0, visibility);
                        playerDeck.setVisibility(elementVisibility);
                    }
                    playerIdAndDeck.put(indexx % game.getPlayers().size(), playerDeck);
                    indexx += 1;
                    parent.removeAll();
                    generateDirectionGuide(frame, playerIdAndDeck, isAll);
                }
            });
        }
    }

    private void simulateActions(InterfaceTech frame, Map<Integer, PartialObservableDeck<FrenchCard>> playerIdAndDeck) {
        for (ActionListener actionListener : frame.getNext().getActionListeners()) {
            frame.getNext().removeActionListener(actionListener);
        }

        frame.updateGUI();
        List<Pair<Long, AbstractAction>> playerIdAndActions = GuideContext.deckForMechanism.getPlayerIdAndActions();
        List<AbstractAction> playerActions = playerIdAndActions.stream().filter(p -> p.a != game.getPlayers().size() - 1).map(pp -> pp.b).toList();
        List<AbstractAction> dealerActions = playerIdAndActions.stream().filter(p -> p.a == game.getPlayers().size() - 1).map(pp -> pp.b).toList();

        SwingWorker<Void, AbstractAction> worker = frame.processSpecificActions(playerActions);
        frame.getNext().addActionListener(e -> {
//            frame.gameRunning.processOneAction(preGameState.getActions().get(i.get()));
//            i.addAndGet(1);
//            frame.updateGUI();
            worker.addPropertyChangeListener(evt -> {
                if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                    BlackjackGameState gs = (BlackjackGameState) game.getGameState();
                    boolean[] visibility = new boolean[game.getPlayers().size()];
                    Arrays.fill(visibility, true);
                    List<boolean[]> elementVisibility = gs.getPlayerDecks().get(gs.getDealerPlayer()).getElementVisibility();
                    elementVisibility.set(0, visibility);
                    gs.getPlayerDecks().get(gs.getDealerPlayer()).setVisibility(elementVisibility);

                    parent.removeAll();
                    int cur = game.getPlayers().size() - 1;
                    BlackjackParameters gameParameters = (BlackjackParameters) gs.getGameParameters();
//                    visibility = new boolean[gs.getNPlayers()];
//                    Arrays.fill(visibility, true);
//                    PartialObservableDeck<FrenchCard> playerDeck = new PartialObservableDeck<>("Player " + gs.getDealerPlayer() + " deck", gs.getDealerPlayer(), visibility);
//                    playerDeck.add(gs.getPlayerDecks().get(gs.getDealerPlayer()));
//                    elementVisibility = (LinkedList<boolean[]>) playerDeck.getElementVisibility();
//                    elementVisibility.set(0, visibility);
//                    playerDeck.setVisibility(elementVisibility);
//                    if (deck.get(i).type == FrenchCard.FrenchCardType.Ace) {
//                        playerHands[i] = new BlackjackPlayerView(playerDeck, i, "data/FrenchCards/", "It can be seen as Point 11");
//                    ddd
//                    BlackjackPlayerView playerHand = new BlackjackPlayerView(playerDeck, cur, gameParameters.getDataPath());
                    BlackjackPlayerView playerHand = new BlackjackPlayerView(gs.getPlayerDecks().get(cur), cur, gameParameters.getDataPath());
                    playerHand.setOpaque(false);
                    // Get agent name
                    String[] split = game.getPlayers().get(cur).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];
                    parent.setBackground(ImageIO.GetInstance().getImage("data/FrenchCards/table-background.jpg"));
                    // Create border, layouts and keep track of this view
                    TitledBorder title = BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DEALER [" + agentName + "]",
                                TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    playerViewBorders[cur] = title;
                    playerHand.setBorder(title);
                    playerHands[cur] = playerHand;
                    frame.updateGUI();
                    DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                            "Now the dealer shows the cards in his hand."));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    pane = new JTabbedPane();
                    JPanel main = new JPanel();
                    main.setOpaque(false);
                    main.setLayout(new BorderLayout());
                    JPanel rules = new JPanel();
                    pane.add("Main", main);
                    pane.add("Rules", rules);
                    JLabel ruleText = new JLabel(getRuleText());
                    rules.add(ruleText);
                    rules.setBackground(new Color(43, 108, 25, 111));
                    JPanel mainGameArea = new JPanel();
                    mainGameArea.setOpaque(false);
                    mainGameArea.setLayout(new BorderLayout());

                    String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                    JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                    int next = 0;
                    for (int i = 0; i < game.getPlayers().size(); i++) {
                        sides[next].add(playerHands[i]);
                        sides[next].setLayout(new GridBagLayout());
                        sides[next].setOpaque(false);
                        next = (next + 1) % (locations.length);
                    }
                    for (int i = 0; i < locations.length; i++) {
                        mainGameArea.add(sides[i], locations[i]);
                    }

                    // Top area will show state information
                    JPanel infoPanel = createGameStateInfoPanel("Blackjack", gs, width, defaultInfoPanelHeight);
                    // Bottom area will show actions available
//                JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false);

                    // Add all views to frame
                    main.add(mainGameArea, BorderLayout.CENTER);
                    main.add(infoPanel, BorderLayout.NORTH);
//                main.add(actionPanel, BorderLayout.SOUTH);

                    pane.add("Main", main);
                    pane.add("Rules", rules);

                    parent.setLayout(new BorderLayout());
                    parent.add(pane, BorderLayout.CENTER);
                    parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
                    parent.revalidate();
                    parent.setVisible(true);
                    parent.repaint();
                    processDealerAction(frame, dealerActions);
//                    generateSoftHand(frame);
                }
            });
            worker.execute();
        });
    }

    private void processDealerAction(InterfaceTech frame, List<AbstractAction> dealerActions) {
        for (ActionListener actionListener : frame.getNext().getActionListeners()) {
            frame.getNext().removeActionListener(actionListener);
        }
        SwingWorker<Void, AbstractAction> worker = frame.processSpecificActions(new ArrayList<>(dealerActions));
        worker.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                parent.repaint();
                frame.showGameResult();
//                frame.runTertiaryPart(frame.simulateForMechanisms.get(0), 0);
                frame.runGameResult();
            }
        });
        worker.execute();

    }

    public void generatePointGuide(InterfaceTech frame) {
        for (ActionListener actionListener : frame.getNext().getActionListeners()) {
            frame.getNext().removeActionListener(actionListener);
        }
        GuideContext.guideStage = GuideContext.GuideState.SHOW_MECHANISM_POINT;
        DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                "Let's learn the points of each card."));
        JTabbedPane pane = new JTabbedPane();
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BorderLayout());
        JPanel rules = new JPanel();
        pane.add("Main", main);
        pane.add("Rules", rules);
        JLabel ruleText = new JLabel(getRuleText());
        rules.add(ruleText);
        rules.setBackground(new Color(43, 108, 25, 111));

        activePlayer = 0;

        this.width = 200;
        this.height = 200;
        ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));

        parent.setBackground(ImageIO.GetInstance().getImage("data/FrenchCards/table-background.jpg"));
        int count = 13;
        boolean[] visibility = new boolean[count];
        Arrays.fill(visibility, true);
        Deck<FrenchCard> deck = new Deck<>("GuideDeck", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        for (int i=2; i<=10; ++i) {
            deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, i));
        }
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Diamonds));

//        PartialObservableDeck<FrenchCard> playerDeck = new PartialObservableDeck<>("Player " + 0 + " deck", 0, visibility);
//        deck.stream().forEach(playerDeck::add);

        playerHands = new BlackjackPlayerView[count];
        playerViewBorders = new Border[count];
        JPanel mainGameArea = new JPanel();
        mainGameArea.setOpaque(false);
//        mainGameArea.setLayout(new BorderLayout());
        mainGameArea.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
//        String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
//        JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
//        JPanel[] playerPanels = new JPanel[count];
        for (int i = 0; i < count; i++) {
            PartialObservableDeck<FrenchCard> playerDeck = new PartialObservableDeck<>("Player " + i + " deck", i, visibility);
            playerDeck.add(deck.get(i));
            if (deck.get(i).type == FrenchCard.FrenchCardType.Ace) {
                playerHands[i] = new BlackjackPlayerView(playerDeck, i, "data/FrenchCards/", "It can be seen as Point 11");
            } else {
                playerHands[i] = new BlackjackPlayerView(playerDeck, i, "data/FrenchCards/");
            }
            BlackjackGameState gameState = (BlackjackGameState) game.getGameState();
            BlackjackParameters params = (BlackjackParameters) gameState.getGameParameters();
            int points = 0;
            switch (deck.get(i).type) {
                case Number:
                    points += deck.get(i).number;
                    break;
                case Jack:
                    points += params.jackCard;
                    break;
                case Queen:
                    points += params.queenCard;
                    break;
                case King:
                    points += params.kingCard;
                    break;
                case Ace:
                    points = 1;
                    break;
            }
            playerHands[i].Points = points;
            playerHands[i].setOpaque(false);
            mainGameArea.add(playerHands[i]);
        }

        // Add all views to frame
        main.add(mainGameArea, BorderLayout.CENTER);
//        main.add(infoPanel, BorderLayout.NORTH);
//                main.add(actionPanel, BorderLayout.SOUTH);

        pane.add("Main", main);
        pane.add("Rules", rules);

        parent.setLayout(new BorderLayout());
        parent.add(pane, BorderLayout.CENTER);
        parent.setPreferredSize(new Dimension(width, height));
        parent.revalidate();
        parent.setVisible(true);
        parent.repaint();

        frame.getNext().addActionListener(e -> {
            parent.removeAll();
            generateDirectionGuide(frame, new HashMap<>(), true);
        });
    }


    public static String getRuleText() {
        String rules = "<html><center><h1>Blackjack</h1></center><br/><hr><br/>";
        rules += "<p>Players are each dealt 2 cards face up. The dealer is also dealt 2 cards, one up (exposed) and one down (hidden). " +
                "The value of number cards 2 through 10 is their pip value (2 through 10). " +
                "Face cards (jack, queen, and king) are all worth 10. " +
                "Aces can be worth 1 or 11. A hand's value is the sum of the card values. Players are allowed to draw additional cards to improve their hands. " +
                "A hand with an ace valued as 11 is called \"soft\", meaning that the hand will be guaranteed to not score more than 21 by taking an additional card. The value of the ace will become 1 to prevent the hand from exceeding 21. Otherwise, the hand is called \"hard\".\n" +
                "</p><br/><p>" +
                "Once all the players have completed their hands, it is the dealer's turn. The dealer hand will not be completed if all players have either exceeded the total of 21 or received blackjacks. The dealer then reveals the hidden card and must draw cards, one by one, until the cards total up to 17 points. " +
                "At 17 points or higher the dealer must stop. " +
                "The better hand is the hand where the sum of the card values is closer to 21 without exceeding 21. The detailed outcome of the hand follows:" +
                "</p><br/><ul><li>" +
                "If the player is dealt an ace and a ten-value card (called a \"blackjack\" or \"natural\"), and the dealer does not, the player wins." +
                "</li><li>If the player exceeds a sum of 21 (\"busts\"), the player loses, even if the dealer also exceeds 21." +
                "</li><li>If the dealer exceeds 21 (\"busts\") and the player does not, the player wins." +
                "</li><li>If the player attains a final sum higher than the dealer and does not bust, the player wins." +
                "</li><li>If both dealer and player receive a blackjack or any other hands with the same sum, this will be called a \"push\" and no one wins.</li></ul>";


        rules += "<hr><p><b>INTERFACE: </b> Choose action (Hit or Stand) at the bottom of the screen.</p>";
        rules += "</html>";
        return rules;
    }
}

