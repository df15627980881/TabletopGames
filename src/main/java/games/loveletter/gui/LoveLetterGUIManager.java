package games.loveletter.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import games.GameType;
import games.loveletter.LoveLetterForwardModel;
import games.loveletter.LoveLetterGameState;
import games.loveletter.LoveLetterParameters;
import games.loveletter.actions.*;
import games.loveletter.actions.deep.PlayCardDeep;
import games.loveletter.cards.LoveLetterCard;
import gui.AbstractGUIManager;
import gui.GamePanel;
import gui.IScreenHighlight;
import guide.DialogUtils;
import guide.GuideContext;
import guide.InterfaceTech;
import guide.PreGameState;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import players.human.ActionController;
import players.mcts.MCTSPlayer;
import utilities.ImageIO;
import utilities.Pair;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class LoveLetterGUIManager extends AbstractGUIManager {
    // Settings for display areas
    final static int playerAreaWidth = 300;
    final static int playerAreaHeight = 135;
    final static int llCardWidth = 90;
    final static int llCardHeight = 115;

    // Width and height of total window
    int width, height;
    // List of player hand + discard views
    LoveLetterPlayerView[] playerHands;
    // Draw pile view
    LoveLetterDeckView drawPile;
    LoveLetterDeckView reserve;

    // Currently active player
    int activePlayer = -1;

    int highlightPlayerIdx = 0;

    // Border highlight of active player
    Border highlightActive = BorderFactory.createLineBorder(new Color(220, 27, 67), 3);
    Border[] playerViewBorders, playerViewBordersHighlight;

    LoveLetterGameState llgs;
    LoveLetterForwardModel fm;


    List<LoveLetterCard> cards;

    public LoveLetterGUIManager(GamePanel parent, Game game, String purpose, InterfaceTech frame) {
        super(parent, game, purpose);

        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        JTabbedPane pane = new JTabbedPane();
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BorderLayout());
        JPanel rules = new JPanel();
        pane.add("Main", main);
        pane.add("Rules", rules);
        JLabel ruleText = new JLabel(getRuleText());
        rules.add(ruleText);

        llgs = (LoveLetterGameState) game.getGameState();

        // Initialise active player
        activePlayer = 0;

        // Find required size of window
        int count = 8;
        this.width = 200;
        this.height = 300;
        ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));

        parent.setBackground(ImageIO.GetInstance().getImage("data/loveletter/bg.png"));

        // Create main game area that will hold all game views
        playerHands = new LoveLetterPlayerView[count];
        playerViewBorders = new Border[count];
        playerViewBordersHighlight = new Border[count];
        JPanel mainGameArea = new JPanel();
        mainGameArea.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        mainGameArea.setOpaque(false);

        Deck<LoveLetterCard> deck = new Deck<>("GuideDeck", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        boolean[] visibility = new boolean[count];
        Arrays.fill(visibility, true);
        for (LoveLetterCard.CardType value : LoveLetterCard.CardType.values()) {
            deck.add(new LoveLetterCard(value));
        }

        LoveLetterParameters llp = (LoveLetterParameters) game.getGameState().getGameParameters();

        for (int i = 0; i < count; i++) {
            PartialObservableDeck<LoveLetterCard> playerDeck = new PartialObservableDeck<>("Player " + i + " deck", i, visibility);
            playerDeck.add(deck.get(i));
            LoveLetterPlayerView playerHand = new LoveLetterPlayerView(playerDeck, i, llp.getDataPath(), deck.get(i).cardType.getCardText(llp));
            playerHands[i] = playerHand;
            int p = i;
            playerHands[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    highlightPlayerIdx = p;
                }
            });
            playerHands[i].setOpaque(false);
            mainGameArea.add(playerHands[i]);
        }
        main.add(mainGameArea, BorderLayout.CENTER);

        parent.setLayout(new BorderLayout());
        parent.add(pane, BorderLayout.CENTER);
        parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
        parent.revalidate();
        parent.setVisible(true);
        parent.repaint();

        frame.getNext().addActionListener(e -> {
            parent.removeAll();
            generateDirectionGuide(frame, parent, game, new HashMap<>(), new HashMap<>(), 0);
//            generate(frame, new HashMap<>(), true);
        });
    }

    public LoveLetterGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> humanID) {
        super(parent, game, ac, humanID);

        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        if (game != null) {
            AbstractGameState gameState = game.getGameState();
            fm = (LoveLetterForwardModel) game.getForwardModel();

            if (gameState != null) {
                llgs = (LoveLetterGameState)gameState;
                JTabbedPane pane = new JTabbedPane();
                JPanel main = new JPanel();
                main.setOpaque(false);
                main.setLayout(new BorderLayout());
                JPanel rules = new JPanel();
                pane.add("Main", main);
                pane.add("Rules", rules);
                JLabel ruleText = new JLabel(getRuleText());
                rules.add(ruleText);

                // Initialise active player
                activePlayer = gameState.getCurrentPlayer();

                // Find required size of window
                int nPlayers = gameState.getNPlayers();
                int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : nPlayers == 4 ? 3 : nPlayers <= 8 ? 4 : 5);
                double nVertAreas = 4;
                this.width = playerAreaWidth * nHorizAreas;
                this.height = (int) (playerAreaHeight * nVertAreas);
                ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));

                parent.setBackground(ImageIO.GetInstance().getImage("data/loveletter/bg.png"));

                LoveLetterGameState llgs = (LoveLetterGameState) gameState;
                LoveLetterParameters llp = (LoveLetterParameters) gameState.getGameParameters();

                // Create main game area that will hold all game views
                playerHands = new LoveLetterPlayerView[nPlayers];
                playerViewBorders = new Border[nPlayers];
                playerViewBordersHighlight = new Border[nPlayers];
                JPanel mainGameArea = new JPanel();
                mainGameArea.setLayout(new BorderLayout());
                mainGameArea.setOpaque(false);

                // Player hands go on the edges
                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                int next = 0;
                for (int i = 0; i < nPlayers; i++) {
                    LoveLetterPlayerView playerHand = new LoveLetterPlayerView(llgs.getPlayerHandCards().get(i),
                            llgs.getPlayerDiscardCards().get(i), i, humanID, llp.getDataPath());

                    // Get agent name
                    String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];

                    // Create border, layouts and keep track of this view
                    TitledBorder title = BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "]",
                            TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    playerViewBorders[i] = title;
                    playerViewBordersHighlight[i] = BorderFactory.createCompoundBorder(highlightActive, playerViewBorders[i]);
                    playerHand.setBorder(title);

                    sides[next].setOpaque(false);
                    sides[next].add(playerHand);
                    sides[next].setLayout(new GridBagLayout());
                    next = (next + 1) % (locations.length);
                    playerHands[i] = playerHand;
                    int p = i;
                    playerHands[i].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            highlightPlayerIdx = p;
                        }
                    });
                }

                // Add GUI listener
                game.addListener(new LLGUIListener(fm, parent, playerHands));
                if (gameState.getNPlayers() == 2) {
                    // Add reserve
                    JLabel label = new JLabel("Reserve cards:");
                    reserve = new LoveLetterDeckView(-1, llgs.getReserveCards(), true, llp.getDataPath(),
                            new Rectangle(0, 0, playerAreaWidth, llCardHeight));
                    JPanel wrap = new JPanel();
                    wrap.setOpaque(false);
                    wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
                    wrap.add(label);
                    wrap.add(reserve);
                    sides[next].setOpaque(false);
                    sides[next].add(wrap);
                    sides[next].setLayout(new GridBagLayout());
                }
                for (int i = 0; i < locations.length; i++) {
                    mainGameArea.add(sides[i], locations[i]);
                }

                // Discard and draw piles go in the center
                JPanel centerArea = new JPanel();
                centerArea.setOpaque(false);
                centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
                drawPile = new LoveLetterDeckView(-1, llgs.getDrawPile(), gameState.getCoreGameParameters().alwaysDisplayFullObservable, llp.getDataPath(),
                        new Rectangle(0, 0, playerAreaWidth, llCardHeight));
                centerArea.add(new JLabel("Draw pile:"));
                centerArea.add(drawPile);
                JPanel jp = new JPanel();
                jp.setOpaque(false);
                jp.setLayout(new GridBagLayout());
                jp.add(centerArea);
                mainGameArea.add(jp, BorderLayout.CENTER);

                // Top area will show state information
                JPanel infoPanel = createGameStateInfoPanel("Love Letter", gameState, width, defaultInfoPanelHeight);
                infoPanel.setOpaque(false);
                // Bottom area will show actions available
                JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false);
                actionPanel.setOpaque(false);

                main.add(infoPanel, BorderLayout.NORTH);
                main.add(mainGameArea, BorderLayout.CENTER);
                main.add(actionPanel, BorderLayout.SOUTH);

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
        return 50;
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
        gameInfo.add(gamePhase);
        gameInfo.add(turn);
        gameInfo.add(currentPlayer);

        gameInfo.setPreferredSize(new Dimension(width/2 - 10, height));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new FlowLayout());
        wrapper.add(gameInfo);

        historyInfo.setOpaque(false);
        historyInfo.setPreferredSize(new Dimension(width/2 - 10, height));
        historyContainer = new JScrollPane(historyInfo);
        historyContainer.setOpaque(false);
//        historyContainer.getViewport().setOpaque(false);
        historyContainer.getViewport().setBackground(new Color(229, 218, 209, 255));
        historyContainer.setPreferredSize(new Dimension(width/2 - 25, height));
        wrapper.add(historyContainer);
        return wrapper;
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
        pane.getViewport().setBackground(new Color(229, 218, 209, 255));
        pane.setPreferredSize(new Dimension(width, height));
        pane.getVerticalScrollBar().setUnitIncrement(16);
        pane.getHorizontalScrollBar().setUnitIncrement(16);
        if (boxLayout) {
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return pane;
    }

    @Override
    protected void updateActionButtons(AbstractPlayer player, AbstractGameState gameState) {
        if (gameState.getGameStatus() == CoreConstants.GameResult.GAME_ONGOING) {
//            resetActionButtons();

            activePlayer = gameState.getCurrentPlayer();
            List<AbstractAction> actions = player.getForwardModel().computeAvailableActions(gameState);
            int highlight = playerHands[activePlayer].handCards.getCardHighlight();
            Deck<LoveLetterCard> deck = ((LoveLetterGameState)gameState).getPlayerHandCards().get(activePlayer);
            if (deck.getSize() > 0) {
                if (highlight == -1 || highlight >= deck.getSize()) {
                    highlight = 0;
                    playerHands[activePlayer].handCards.setCardHighlight(highlight);
                }
                LoveLetterCard hCard = deck.get(highlight);

                int k = 0;
                for (AbstractAction action : actions) {
                    if (action instanceof PlayCard) {
                        PlayCard pc = (PlayCard) action;
                        if (pc.getTargetPlayer() == -1 || pc.getTargetPlayer() == highlightPlayerIdx) {
                            actionButtons[k].setVisible(true);
                            actionButtons[k].setButtonAction(action, action.getString(gameState));
                            k++;
                        }
                    }
                }
                for (int i = k; i < actionButtons.length; i++) {
                    actionButtons[i].setVisible(false);
                    actionButtons[i].setButtonAction(null, "");
                }
            } else {
                for (int i = 0; i < actions.size(); i++) {
                    actionButtons[i].setVisible(true);
                    actionButtons[i].setButtonAction(actions.get(i), gameState);
                }
                for (int i = actions.size(); i < actionButtons.length; i++) {
                    actionButtons[i].setVisible(false);
                    actionButtons[i].setButtonAction(null, "");
                }
            }
        } else if (gameState.getGameStatus() == CoreConstants.GameResult.GAME_END) {
            for (ActionButton actionButton : actionButtons) {
                actionButton.setVisible(false);
            }
        }
    }

    @Override
    protected void _update(AbstractPlayer player, AbstractGameState gameState) {
        if (gameState != null) {

            // Update active player highlight
            if (gameState.getCurrentPlayer() != activePlayer) {
                playerHands[activePlayer].handCards.setCardHighlight(-1);
                activePlayer = gameState.getCurrentPlayer();
            }

            // Update decks and visibility
            llgs = (LoveLetterGameState)gameState.copy();
            for (int i = 0; i < gameState.getNPlayers(); i++) {
                boolean front = i == gameState.getCurrentPlayer() && gameState.getCoreGameParameters().alwaysDisplayCurrentPlayer
                        || (CollectionUtils.isNotEmpty(humanPlayerId) && humanPlayerId.contains(i))
                        || gameState.getCoreGameParameters().alwaysDisplayFullObservable;
                playerHands[i].update(llgs, front);

                // Highlight active player
                if (i == gameState.getCurrentPlayer()) {
                    playerHands[i].setBorder(playerViewBordersHighlight[i]);
                } else {
                    playerHands[i].setBorder(playerViewBorders[i]);
                }
            }
            if (reserve != null)
                reserve.updateComponent(llgs.getReserveCards());
            drawPile.updateComponent(llgs.getDrawPile());
            drawPile.setFront(gameState.getCoreGameParameters().alwaysDisplayFullObservable);

        }
    }

    private void generateDirectionGuide(InterfaceTech frame, GamePanel parent, Game game, Map<Integer, PartialObservableDeck<LoveLetterCard>> playerIdAndDeck, Map<Integer, PartialObservableDeck<LoveLetterCard>> discardDeck, int indexx) {
        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        if (indexx == 0) {
            cards = new ArrayList<>();
            for (LoveLetterCard.CardType cardType : LoveLetterCard.CardType.values()) {
                cards.add(new LoveLetterCard(cardType));
            }
            Collections.shuffle(cards, new Random(GuideContext.deckForMechanism.getSeed()));
            DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "First of all, all the cards are hidden, and you can only view your card"));
        } else if (indexx == 1) {
            DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "In your turn, you can get a new card."));
        } else if (indexx == 2) {
            DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "Next, you will play a card from your hand, which will be visible to all other players."));
            for (ActionListener actionListener : frame.getNext().getActionListeners()) {
                frame.getNext().removeActionListener(actionListener);
            }
            frame.getNext().addActionListener(e -> {
                simulateActions(parent, frame, new HashMap<>());
            });
        }
        int count = 3;
        boolean[] visibility = new boolean[count];
        Arrays.fill(visibility, false);
        visibility[0] = true;

        if (game != null) {
            AbstractGameState gameState = game.getGameState();
            fm = (LoveLetterForwardModel) game.getForwardModel();

            if (gameState != null) {
                llgs = (LoveLetterGameState) gameState;
                JTabbedPane pane = new JTabbedPane();
                JPanel main = new JPanel();
                main.setOpaque(false);
                main.setLayout(new BorderLayout());
                JPanel rules = new JPanel();
                pane.add("Main", main);
                pane.add("Rules", rules);
                JLabel ruleText = new JLabel(getRuleText());
                rules.add(ruleText);

                // Initialise active player
                activePlayer = gameState.getCurrentPlayer();

                // Find required size of window
                int nPlayers = gameState.getNPlayers();
                int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : nPlayers == 4 ? 3 : nPlayers <= 8 ? 4 : 5);
                double nVertAreas = 4;
                this.width = playerAreaWidth * nHorizAreas;
                this.height = (int) (playerAreaHeight * nVertAreas);
                ruleText.setPreferredSize(new Dimension(width * 2 / 3 + 60, height * 2 / 3 + 100));

                parent.setBackground(ImageIO.GetInstance().getImage("data/loveletter/bg.png"));

                LoveLetterGameState llgs = (LoveLetterGameState) gameState;
                LoveLetterParameters llp = (LoveLetterParameters) gameState.getGameParameters();

                // Create main game area that will hold all game views
                playerHands = new LoveLetterPlayerView[nPlayers];
                playerViewBorders = new Border[nPlayers];
                playerViewBordersHighlight = new Border[nPlayers];
                JPanel mainGameArea = new JPanel();
                mainGameArea.setLayout(new BorderLayout());
                mainGameArea.setOpaque(false);

                // Player hands go on the edges
                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                int next = 0;
                for (int i = 0; i < nPlayers; i++) {
                    PartialObservableDeck<LoveLetterCard> deck = new PartialObservableDeck<>("Player " + i + " deck", i, visibility);
                    deck.add(cards.get(i));
                    LoveLetterPlayerView playerHand;
                    if (playerIdAndDeck.containsKey(i)) {
                        playerHand = new LoveLetterPlayerView(playerIdAndDeck.get(i), discardDeck.get(i), i, new HashSet<>(), llp.getDataPath());
                    } else {
                        playerHand = new LoveLetterPlayerView(deck, discardDeck.get(i), i, new HashSet<>(), llp.getDataPath());
                        playerIdAndDeck.put(i, deck);
                    }

                    // Get agent name
                    String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];

                    // Create border, layouts and keep track of this view
                    TitledBorder title = BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "]",
                            TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    playerViewBorders[i] = title;
                    playerViewBordersHighlight[i] = BorderFactory.createCompoundBorder(highlightActive, playerViewBorders[i]);
                    playerHand.setBorder(title);

                    sides[next].setOpaque(false);
                    sides[next].add(playerHand);
                    sides[next].setLayout(new GridBagLayout());
                    next = (next + 1) % (locations.length);
                    playerHands[i] = playerHand;
                    int p = i;
                    playerHands[i].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            highlightPlayerIdx = p;
                        }
                    });
                }

                // Add GUI listener
                game.addListener(new LLGUIListener(fm, parent, playerHands));
                if (gameState.getNPlayers() == 2) {
                    // Add reserve
                    JLabel label = new JLabel("Reserve cards:");
                    reserve = new LoveLetterDeckView(-1, llgs.getReserveCards(), true, llp.getDataPath(),
                            new Rectangle(0, 0, playerAreaWidth, llCardHeight));
                    JPanel wrap = new JPanel();
                    wrap.setOpaque(false);
                    wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
                    wrap.add(label);
                    wrap.add(reserve);
                    sides[next].setOpaque(false);
                    sides[next].add(wrap);
                    sides[next].setLayout(new GridBagLayout());
                }
                for (int i = 0; i < locations.length; i++) {
                    mainGameArea.add(sides[i], locations[i]);
                }

                // Discard and draw piles go in the center
                JPanel centerArea = new JPanel();
                centerArea.setOpaque(false);
                centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
                drawPile = new LoveLetterDeckView(-1, llgs.getDrawPile(), gameState.getCoreGameParameters().alwaysDisplayFullObservable, llp.getDataPath(),
                        new Rectangle(0, 0, playerAreaWidth, llCardHeight));
                centerArea.add(new JLabel("Draw pile:"));
                centerArea.add(drawPile);
                JPanel jp = new JPanel();
                jp.setOpaque(false);
                jp.setLayout(new GridBagLayout());
                jp.add(centerArea);
                mainGameArea.add(jp, BorderLayout.CENTER);

                // Top area will show state information
                JPanel infoPanel = createGameStateInfoPanel("Love Letter", gameState, width, defaultInfoPanelHeight);
                infoPanel.setOpaque(false);
                // Bottom area will show actions available
                JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false);
                actionPanel.setOpaque(false);

                main.add(infoPanel, BorderLayout.NORTH);
                main.add(mainGameArea, BorderLayout.CENTER);
                main.add(actionPanel, BorderLayout.SOUTH);

                parent.setLayout(new BorderLayout());
                parent.add(pane, BorderLayout.CENTER);
                parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
                parent.revalidate();
                parent.setVisible(true);
                parent.repaint();

                if (indexx == 0) {
                    for (ActionListener actionListener : frame.getNext().getActionListeners()) {
                        frame.getNext().removeActionListener(actionListener);
                    }
                    frame.getNext().addActionListener(e -> {
                        PartialObservableDeck<LoveLetterCard> deck = new PartialObservableDeck<>("Player " + 0 + " deck", 0, visibility);
                        deck.add(cards.get(0));
                        deck.add(cards.get(count));
                        playerIdAndDeck.put(0, deck);
                        parent.removeAll();
                        generateDirectionGuide(frame, parent, game, playerIdAndDeck, discardDeck, indexx + 1);
                    });
                } else if (indexx == 1) {
                    for (ActionListener actionListener : frame.getNext().getActionListeners()) {
                        frame.getNext().removeActionListener(actionListener);
                    }
                    frame.getNext().addActionListener(e -> {
                        PartialObservableDeck<LoveLetterCard> deck1 = new PartialObservableDeck<>("Player " + 0 + " deck", 0, visibility);
                        PartialObservableDeck<LoveLetterCard> deck2 = new PartialObservableDeck<>("Player " + 0 + " deck", 0, visibility);
                        deck2.add(cards.get(0));
                        deck1.add(cards.get(count));
                        playerIdAndDeck.put(0, deck1);
                        discardDeck.put(0, deck2);
                        parent.removeAll();
                        generateDirectionGuide(frame, parent, game, playerIdAndDeck, discardDeck, indexx + 1);
                    });
                }
            }
        }
    }

    private void simulateActions(GamePanel parent, InterfaceTech frame, Map<Integer, PartialObservableDeck<LoveLetterCard>> playerIdAndDeck) {
        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);
        boolean isFirst = MapUtils.isEmpty(playerIdAndDeck);
        if (isFirst) {
            for (ActionListener actionListener : frame.getNext().getActionListeners()) {
                frame.getNext().removeActionListener(actionListener);
            }
        }
//            List<LoveLetterCard> loveLetterCards = new ArrayList<>(GuideContext.deckForMechanism.getDrawDeck().getComponents());
        if (game != null) {
            AbstractGameState gameState = game.getGameState();
            fm = (LoveLetterForwardModel) game.getForwardModel();

            if (gameState != null) {
                llgs = (LoveLetterGameState)gameState;
                JTabbedPane pane = new JTabbedPane();
                JPanel main = new JPanel();
                main.setOpaque(false);
                main.setLayout(new BorderLayout());
                JPanel rules = new JPanel();
                pane.add("Main", main);
                pane.add("Rules", rules);
                JLabel ruleText = new JLabel(getRuleText());
                rules.add(ruleText);
                activePlayer = gameState.getCurrentPlayer();
                int nPlayers = gameState.getNPlayers();
                int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : nPlayers == 4 ? 3 : nPlayers <= 8 ? 4 : 5);
                double nVertAreas = 4;
                this.width = playerAreaWidth * nHorizAreas;
                this.height = (int) (playerAreaHeight * nVertAreas);
                ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));
                parent.setBackground(ImageIO.GetInstance().getImage("data/loveletter/bg.png"));
                LoveLetterGameState llgs = (LoveLetterGameState) gameState;
                LoveLetterParameters llp = (LoveLetterParameters) gameState.getGameParameters();
                playerHands = new LoveLetterPlayerView[nPlayers];
                playerViewBorders = new Border[nPlayers];
                playerViewBordersHighlight = new Border[nPlayers];
                JPanel mainGameArea = new JPanel();
                mainGameArea.setLayout(new BorderLayout());
                mainGameArea.setOpaque(false);

                // Player hands go on the edges
                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                int next = 0;
                for (int i = 0; i < nPlayers; i++) {
                    LoveLetterPlayerView playerHand;
                    boolean[] visibility = new boolean[game.getPlayers().size()];
                    Arrays.fill(visibility, i == 0);
                    if (isFirst) {
                        PartialObservableDeck<LoveLetterCard> deck = new PartialObservableDeck<>(String.valueOf(i), i, visibility);
                        deck.add(cards.get(i));
                        playerIdAndDeck.put(i, deck);
                        playerHand = new LoveLetterPlayerView(deck,
                                llgs.getPlayerDiscardCards().get(i), i, null, llp.getDataPath());
                    } else {
                        PartialObservableDeck<LoveLetterCard> deck = new PartialObservableDeck<>(String.valueOf(i), i, visibility);
                        PartialObservableDeck<LoveLetterCard> loveLetterCardPartialObservableDeck = llgs.getPlayerHandCards().get(i);
                        deck.add(loveLetterCardPartialObservableDeck.get(0));
                        playerHand = new LoveLetterPlayerView(deck,
                                llgs.getPlayerDiscardCards().get(i), i, null, llp.getDataPath());
                    }

                    // Get agent name
                    String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];

                    // Create border, layouts and keep track of this view
                    TitledBorder title = BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "]",
                            TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    playerViewBorders[i] = title;
                    playerViewBordersHighlight[i] = BorderFactory.createCompoundBorder(highlightActive, playerViewBorders[i]);
                    playerHand.setBorder(title);

                    sides[next].setOpaque(false);
                    sides[next].add(playerHand);
                    sides[next].setLayout(new GridBagLayout());
                    next = (next + 1) % (locations.length);
                    playerHands[i] = playerHand;
                    int p = i;
                    playerHands[i].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            highlightPlayerIdx = p;
                        }
                    });
                }

                // Add GUI listener
                game.addListener(new LLGUIListener(fm, parent, playerHands));
                if (gameState.getNPlayers() == 2) {
                    // Add reserve
                    JLabel label = new JLabel("Reserve cards:");
                    reserve = new LoveLetterDeckView(-1, llgs.getReserveCards(), true, llp.getDataPath(),
                            new Rectangle(0, 0, playerAreaWidth, llCardHeight));
                    JPanel wrap = new JPanel();
                    wrap.setOpaque(false);
                    wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
                    wrap.add(label);
                    wrap.add(reserve);
                    sides[next].setOpaque(false);
                    sides[next].add(wrap);
                    sides[next].setLayout(new GridBagLayout());
                }
                for (int i = 0; i < locations.length; i++) {
                    mainGameArea.add(sides[i], locations[i]);
                }

                // Discard and draw piles go in the center
                JPanel centerArea = new JPanel();
                centerArea.setOpaque(false);
                centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
                drawPile = new LoveLetterDeckView(-1, llgs.getDrawPile(), gameState.getCoreGameParameters().alwaysDisplayFullObservable, llp.getDataPath(),
                        new Rectangle(0, 0, playerAreaWidth, llCardHeight));
                centerArea.add(new JLabel("Draw pile:"));
                centerArea.add(drawPile);
                JPanel jp = new JPanel();
                jp.setOpaque(false);
                jp.setLayout(new GridBagLayout());
                jp.add(centerArea);
                mainGameArea.add(jp, BorderLayout.CENTER);

                // Top area will show state information
                JPanel infoPanel = createGameStateInfoPanel("Love Letter", gameState, width, defaultInfoPanelHeight);
                infoPanel.setOpaque(false);
                // Bottom area will show actions available
                JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false);
                actionPanel.setOpaque(false);

                main.add(infoPanel, BorderLayout.NORTH);
                main.add(mainGameArea, BorderLayout.CENTER);
                main.add(actionPanel, BorderLayout.SOUTH);

                parent.setLayout(new BorderLayout());
                parent.add(pane, BorderLayout.CENTER);
                parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
                parent.revalidate();
                parent.setVisible(true);
                parent.repaint();

                if (isFirst) {
                    PreGameState<LoveLetterCard> deckForMechanism = GuideContext.deckForMechanism;
                    deckForMechanism.resetIndexx();
                    List<AbstractPlayer> players = new ArrayList<>();
                    for (int i = 0; i < deckForMechanism.getPlayerCount(); ++i) players.add(new MCTSPlayer());
                    Game game2 = Game.runOne(GameType.LoveLetter, null, players, deckForMechanism.getSeed(), false, null, null, 1);
                    frame.gameResult = game2;
                    AbstractGameState gameState1 = game2.getGameState().copy();
                    gameState1.reset(deckForMechanism.getSeed());
                    deckForMechanism.resetIndexx();
                    frame.gameRunning = new Game(game2.getGameType(), game2.getPlayers(),
                            GameType.LoveLetter.createForwardModel(null, game2.getPlayers().size()), gameState1);
                    game = frame.gameRunning;
                    frame.updateGUI();
                    DialogUtils.show(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                            "Now, let's understand the function of each action through a brief gameplay session."));
                    parent.removeAll();
                    simulateActions(parent, frame, playerIdAndDeck);
                    return;
                }
            }
        }
        frame.updateGUI();
        List<Pair<Long, AbstractAction>> playerIdAndActions = GuideContext.deckForMechanism.getPlayerIdAndActions();
        // When we use algorithm to generate the game, we make sure the first 10 actions include 8 types of LoveLetterCard. So here we just show the first 10 actions.
        playerIdAndActions = playerIdAndActions.subList(0, 10);
        List<AbstractAction> actions = playerIdAndActions.stream().map(pp -> pp.b).toList();
        SwingWorker<Void, AbstractAction> worker = frame.processSpecificActions(actions);
        for (ActionListener actionListener : frame.getNext().getActionListeners()) {
            frame.getNext().removeActionListener(actionListener);
        }
        frame.getNext().addActionListener(e -> {
            worker.addPropertyChangeListener(evt -> {
                if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                    parent.repaint();
                    System.out.println("FinishActionIntroduce");
                    frame.runGameResult();
                }
            });
            worker.execute();
        });
    }

    public String getRuleText() {
        String rules = "<html><center><h1>Love Letter</h1></center><br/><hr><br/>";
        rules += "<p>You try to earn the favour of the princess and get your love letter delivered to her. The closer you are (the higher your card number) at the end, the better. The closest player, or the only one left in the game, is the winner of the round. Win most rounds to win the game.</p><br/>";
        rules += "<p>On your turn, you draw a card to have 2 in hand, and then play one of the cards, discarding it and executing its effect.</p>";
        rules += "<p><b>Types of cards</b>: " +
                "<ul><li>Guard (1; x5): guess another player's card; if correct, that player has to discard their card and is eliminated.</li>" +
                "<li>Priest (2; x2): see another player's card.</li>" +
                "<li>Baron (3; x2): compare cards with another player; the player with the lower card is eliminated.</li>" +
                "<li>Handmaid (4; x2): the player is protected for 1 round and cannot be targeted by others' actions.</li>" +
                "<li>Prince (5; x2): choose a player to discard their card and draw another (can be yourself).</li>" +
                "<li>King (6; x1): choose a player to swap cards with.</li>" +
                "<li>Countess (7; x1): must be discarded if the other card in hand is a King or a Prince.</li>" +
                "<li>Princess (8; x1): player is eliminated if they discard this card.</li>" +
                "</ul></p><br/>";
        rules += "<hr><p><b>INTERFACE: </b> Find actions available at any time at the bottom of the screen. Each player has 2 components in their area: their hand (hidden; left) and their cards played/discarded (right). Click on cards in a deck to see them better / select them to see actions associated. Click on player areas (e.g. player names) to see actions targetting them.</p>";
        rules += "</html>";
        return rules;
    }
}
