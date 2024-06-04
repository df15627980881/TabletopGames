package games.blackjack.gui;

import com.beust.ah.A;
import com.clearspring.analytics.util.Lists;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import gui.GamePanel;
import gui.views.CardView;
import gui.views.ComponentView;
import utilities.ImageIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static games.blackjack.gui.BlackjackGUIManager.*;

public class BlackjackDeckView extends ComponentView {

    Image backOfCard;
    String dataPath;
    int minimumCardOffset = 5;
    Rectangle[] rects;
    int cardHighlight = -1;
    boolean highlighting;
    protected GamePanel parent;
    public static String[] locations = {BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
    public static int turn = 0;
    public static int currentFangwei = 0;
    public static int count = 0;
    public static FrenchCard.Suite suite;
    public static int ggg = 0;

    public BlackjackDeckView(Deck<FrenchCard> d, String dataPath, GamePanel parent, String[] gg){
        super(d, playerWidth, cardHeight);
        this.parent = parent;
        backOfCard = ImageIO.GetInstance().getImage(dataPath + "gray_back.png");
        this.dataPath = dataPath;

//        addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ALT){
//                 highlighting = true;
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ALT){
//                    highlighting = false;
//                    cardHighlight = -1;
//                }
//            }
//        });
//        addMouseMotionListener(new MouseAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                if (highlighting){
//                    for (int i = 0; i< rects.length; i++){
//                        if (rects[i].contains(e.getPoint())){
//                            cardHighlight = i;
//                            break;
//                        }
//                    }
//                }
//            }
//        });
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getButton() ==1 && rects != null && rects.length > 0){
//                    for (int i = 0; i< rects.length; i++){
//                        if (rects[i].contains(e.getPoint())){
//                            cardHighlight = i;
//                            break;
//                        }
//                    }
//                }
//                else{
//                    cardHighlight = -1;
//                }
//            }
//        });

//        d.stream().forEach(System.out::println);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JTabbedPane pane = (JTabbedPane) parent.getComponent(0);
                JPanel main = (JPanel) pane.getComponent(0);
                JPanel northPanel = (JPanel) main.getComponent(0);
                JPanel mainGameArea = (JPanel) main.getComponent(1);
                JPanel sides = (JPanel) mainGameArea.getComponent(currentFangwei);
//                BlackjackPlayerView[] playerHand = (BlackjackPlayerView[]) sides.getComponents();
//                Deck<FrenchCard> aaa = (Deck<FrenchCard>) playerHand.getComponent();
                BlackjackPlayerView newP = new BlackjackPlayerView(d, (int) System.currentTimeMillis()%10000, "data/FrenchCards/", parent, locations);
                // TODO
                newP.Points = 0;
                newP.setOpaque(false);
                newP.setPreferredSize(new Dimension(cardWidth*2/3, cardHeight));
//                for (int i = 0; i < playerHand.length; i++) {
//                    newP[i] = playerHand[i];
//                }
//                newP[playerHand.length] = new BlackjackPlayerView(d, (int) System.currentTimeMillis()%10000, "data/FrenchCards/", parent);;
//                playerHand.updateComponent(aaa);
//                sides.remove(0);
                sides.add(newP);
                mainGameArea.remove(currentFangwei);
                mainGameArea.add(sides, locations[turn], currentFangwei);
                System.out.println("turn: " + turn + " locations[turn]: " + locations[turn] + " currentFangwei: " + currentFangwei);
                JPanel newNorth = new JPanel();
                for (int i = 0; i < northPanel.getComponents().length; i++) {
                    JPanel centerPanel = (JPanel) northPanel.getComponent(i);
                    BlackjackPlayerView bv = (BlackjackPlayerView) centerPanel.getComponent(0);
                    Deck<FrenchCard>g = (Deck<FrenchCard>) bv.getComponent();
                    if (!(g.get(0).number == d.get(0).number && g.get(0).suite.equals(d.get(0).suite) && g.get(0).type.equals(d.get(0).type))) {
                        BlackjackPlayerView cc = new BlackjackPlayerView(g, bv.playerID, bv.dataPath, bv.parent, locations);
                        cc.Points = 0;
                        cc.setOpaque(false);
                        cc.setPreferredSize(new Dimension(cardWidth/3, cardHeight));

                        JPanel newCenterPanel = new JPanel(new BorderLayout()); // 用于放置中心牌组
                        newCenterPanel.setOpaque(false);
                        newCenterPanel.add(cc);

                        newNorth.add(newCenterPanel);
                    }
                }

                main.remove(1);
                main.remove(0);
                main.add(newNorth, BorderLayout.NORTH);
                main.add(mainGameArea, BorderLayout.SOUTH);
                pane.remove(0);
                pane.add(main);
                parent.remove(0);
                parent.add(pane, BorderLayout.CENTER);
//                parent.setTurn((parent.getTurn() + 1) % 4);
                parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
                parent.revalidate();
                parent.setVisible(true);
                parent.repaint();
                count += 1;
                turn = (turn + 1) % 4;
                currentFangwei = (currentFangwei + 1) % 4;
                if (count % 4 == 0) {
                    JPanel sides0 = (JPanel) mainGameArea.getComponent(0);
                    JPanel sides1 = (JPanel) mainGameArea.getComponent(1);
                    JPanel sides2 = (JPanel) mainGameArea.getComponent(2);
                    JPanel sides3 = (JPanel) mainGameArea.getComponent(3);
                    BlackjackDeckView b0 = (BlackjackDeckView) sides0.getComponent(count/4);
                    BlackjackDeckView b1 = (BlackjackDeckView) sides1.getComponent(count/4);
                    BlackjackDeckView b2 = (BlackjackDeckView) sides2.getComponent(count/4);
                    BlackjackDeckView b3 = (BlackjackDeckView) sides3.getComponent(count/4);
                    FrenchCard f0 = ((Deck<FrenchCard>) b0.component).get(0);
                    FrenchCard f1 = ((Deck<FrenchCard>) b1.component).get(0);
                    FrenchCard f2 = ((Deck<FrenchCard>) b2.component).get(0);
                    FrenchCard f3 = ((Deck<FrenchCard>) b3.component).get(0);
                    ArrayList<FrenchCard> cards = new ArrayList<>();
                    cards.add(f0); cards.add(f1); cards.add(f2); cards.add(f3);
                    // NT
                    if (suite == null) {
                        FrenchCard maxx = cards.get(currentFangwei);
                        int tmp = -1;
                        for (int i=0; i<4; ++i) {
                            if (cards.get(i).suite == maxx.suite) {
                                if (cards.get(i).number >= maxx.number) {
                                    tmp = i;
                                    maxx = cards.get(i);
                                }
                            }
                        }
                        currentFangwei = tmp;
                        turn = tmp + ggg;
                        System.out.println(currentFangwei);
                    } else {
                        FrenchCard maxx = cards.get(currentFangwei);
                        int tmp = -1;
                        // 首家出的将牌，处理方式和无将一样
                        if (maxx.suite.equals(suite)) {
                            for (int i = 0; i < 4; ++i) {
                                if (cards.get(i).suite == maxx.suite) {
                                    if (cards.get(i).number >= maxx.number) {
                                        tmp = i;
                                        maxx = cards.get(i);
                                    }
                                }
                            }
                            currentFangwei = tmp;
                        } else {
                            // 是否有人出过将牌
                            boolean f = false;
                            for (int i = 0; i < 4; ++i) {
                                if (cards.get(i).suite == suite) {
                                    f = true;
                                    maxx = cards.get(i);
                                }
                            }
                            for (int i = 0; i < 4; ++i) {
                                if (f) {
                                    if (cards.get(i).suite == suite) {
                                        if (cards.get(i).number >= maxx.number) {
                                            tmp = i;
                                            maxx = cards.get(i);
                                        }
                                    }
                                } else {
                                    // 没有人出将牌，处理方式和无将一样
                                    if (cards.get(i).suite == maxx.suite) {
                                        if (cards.get(i).number >= maxx.number) {
                                            tmp = i;
                                            maxx = cards.get(i);
                                        }
                                    }
                                }
                            }
                            currentFangwei = tmp;
                        }
                        turn = tmp + ggg;
                    }
                }
                turn %= 4;
                System.out.println(currentFangwei);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        drawDeck((Graphics2D)g, new Rectangle(0, 0, width, cardHeight));
    }

    public void drawDeck(Graphics2D g, Rectangle rect){
        int size = g.getFont().getSize();
        @SuppressWarnings("Unchecked") PartialObservableDeck<FrenchCard> deck = (PartialObservableDeck<FrenchCard>) component;

        if (deck != null && deck.getSize() != 0){
            int offset = Math.max((rect.width-cardWidth) / deck.getSize(), minimumCardOffset);
            rects = new Rectangle[deck.getSize()];
            for (int i = 0; i < deck.getSize(); i++){
                FrenchCard card = deck.get(i);
                Image cardFace = getCardImage(card);
                Rectangle r = new Rectangle(rect.x + offset * i, rect.y, cardWidth, cardHeight);
                rects[i] = r;
                CardView.drawCard(g, r.x, r.y, r.width, r.height, card, cardFace, backOfCard, deck.isComponentVisible(i, 0));
                g.drawRoundRect(r.x, r.y, r.width, r.height, 15, 15);
            }
            if (cardHighlight != -1){
                FrenchCard card = deck.get(cardHighlight);
                Image cardFace = getCardImage(card);
                Rectangle r = rects[cardHighlight];
                CardView.drawCard(g, r.x, r.y, r.width, r.height, card, cardFace, backOfCard, deck.isComponentVisible(cardHighlight, 0));
                g.drawRoundRect(r.x, r.y, r.width, r.height, 15, 15);
            }
            g.drawString(""+deck.getSize(), rect.x+10, rect.y+cardHeight - size);
        }
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }

    private Image getCardImage(FrenchCard card){
        Image img = null;
        //String coloName = card.
        switch(card.type){
            case Number:
                img = ImageIO.GetInstance().getImage(dataPath + card.number + card.suite + ".png");
                break;
            case Jack:
            case Queen:
            case King:
            case Ace:
                img = ImageIO.GetInstance().getImage(dataPath + card.type + card.suite + ".png");
                break;

        }
        return img;
    }

    public int getCardHighlight(){return cardHighlight;}
    public void setCardHighlight(int cardHighlight) {
        this.cardHighlight = cardHighlight;
    }
    public Rectangle[] getRects() {
        return rects;
    }
}
