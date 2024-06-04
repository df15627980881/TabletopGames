package games.blackjack.gui;

import core.CoreConstants;
import core.Game;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import games.blackjack.BlackjackGameState;
import games.blackjack.BlackjackParameters;
import gui.GamePanel;
import utilities.ImageIO;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;

public class BlackjackMechanismGUI extends BlackjackGUIManager {

    private final String purpose;

    public BlackjackMechanismGUI(GamePanel parent, Game game, String purpose) {
        super(parent, game, purpose);
        this.purpose = purpose;
    }

    public void generateDirectionGuide() {
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
    }

    public void generatePointGuide() {
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
    }

    public enum Mechanism {
        DIRECTION("Direction"),
        SOFT_HAND("Soft Hand"),
        POINT("Point");

        private final String description;

        Mechanism(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
