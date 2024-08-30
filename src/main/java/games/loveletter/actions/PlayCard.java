package games.loveletter.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.PartialObservableDeck;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;
import guide.DialogUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class PlayCard extends AbstractAction {
    protected final int playerID;
    final int targetPlayer;
    protected final LoveLetterCard.CardType cardType;
    final LoveLetterCard.CardType forcedCountessCardType;
    final boolean canExecuteEffect;
    final boolean discard;
    protected final int cardIdx;

    LoveLetterCard.CardType targetCardType, otherCardInHand;

    public PlayCard(LoveLetterCard.CardType cardType, int cardIdx, int playerID, int targetPlayer, LoveLetterCard.CardType targetCardType, LoveLetterCard.CardType forcedCountessCardType, boolean canExecuteEffect, boolean discard) {
        this.cardType = cardType;
        this.playerID = playerID;
        this.targetPlayer = targetPlayer;
        this.targetCardType = targetCardType;
        this.forcedCountessCardType = forcedCountessCardType;
        this.canExecuteEffect = canExecuteEffect;
        this.discard = discard;
        this.cardIdx = cardIdx;
    }
    public PlayCard(int cardIdx, int playerID, boolean discard, LoveLetterCard.CardType targetCardType) {
        this.cardType = null;
        this.playerID = playerID;
        this.targetPlayer = -1;
        this.targetCardType = targetCardType;
        this.forcedCountessCardType = null;
        this.canExecuteEffect = false;
        this.discard = discard;
        this.cardIdx = cardIdx;
    }
    public PlayCard(int cardIdx, int playerID, boolean discard) {
        this.cardType = null;
        this.playerID = playerID;
        this.targetPlayer = -1;
        this.forcedCountessCardType = null;
        this.canExecuteEffect = false;
        this.discard = discard;
        this.cardIdx = cardIdx;
    }
    public PlayCard(int cardIdx, int playerID, boolean discard, int targetPlayer) {
        this.cardType = null;
        this.playerID = playerID;
        this.targetPlayer = targetPlayer;
        this.forcedCountessCardType = null;
        this.canExecuteEffect = false;
        this.discard = discard;
        this.cardIdx = cardIdx;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        LoveLetterGameState llgs = (LoveLetterGameState) gs;

        if (discard) {
            PartialObservableDeck<LoveLetterCard> from = llgs.getPlayerHandCards().get(playerID);
            Deck<LoveLetterCard> to = llgs.getPlayerDiscardCards().get(playerID);
            LoveLetterCard card = null;
            if (cardIdx != -1) {
                card = from.get(cardIdx);
            } else {
                // Find card by type
                for (LoveLetterCard c : from.getComponents()) {
                    if (c.cardType == cardType) {
                        card = c;
                        break;
                    }
                }
            }
            if (card != null) {
                // Discard card
                from.remove(card);
                to.add(card);
            } else {
                System.err.println("No card in hand matching the required type " + cardType);
                throw new AssertionError("No card in hand matching the required type");
            }
        }

        // Execute card effect
        if (canExecuteEffect) // If not, just discard the card, it's ok
            return _execute(llgs);

        return true;
    }

    @Override
    public PlayCard copy() {
        return this;
    }

    protected boolean _execute(LoveLetterGameState llgs) {return true;}

    @Override
    public String toString() {
        if (cardType != null) return cardType.name();
        return "(?)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayCard playCard)) return false;

        // cardidx not included because it is ignored for some action space structures
        return playerID == playCard.playerID && targetPlayer == playCard.targetPlayer && canExecuteEffect == playCard.canExecuteEffect && discard == playCard.discard && cardType == playCard.cardType && forcedCountessCardType == playCard.forcedCountessCardType && targetCardType == playCard.targetCardType && otherCardInHand == playCard.otherCardInHand;
    }

    @Override
    public int hashCode() {
        // cardidx not included because it is ignored for some action space structures
        return Objects.hash(playerID, targetPlayer, cardType, forcedCountessCardType, canExecuteEffect, discard, targetCardType, otherCardInHand);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        if (!canExecuteEffect) return cardType + " - no effect";
        else if (cardType != null) return cardType.getString(this);
        else return "(?) No effect";
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getTargetPlayer() {
        return targetPlayer;
    }

    public LoveLetterCard.CardType getCardType() {
        return cardType;
    }

    public LoveLetterCard.CardType getForcedCountessCardType() {
        return forcedCountessCardType;
    }

    public LoveLetterCard.CardType getTargetCardType() {
        return targetCardType;
    }

    public boolean canExecuteEffect() {
        return canExecuteEffect;
    }

    public boolean isDiscard() {
        return discard;
    }

    public LoveLetterCard.CardType getOtherCardInHand() {
        return otherCardInHand;
    }

    public int getCardIdx() {
        return cardIdx;
    }

    @Override
    public ArrayList<JDialog> createDialogWithFeedbackForNewbie(Frame frame, AbstractGameState gameState, int currentPlayer) {
        if (this.cardType == LoveLetterCard.CardType.Princess) {
            return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Princess Action</h2><p>The other card cannot be played, " +
                            "so only the princess can be played. The player" + this.playerID + " is eliminated.</p></html>"));
        }
        if (this.cardType == LoveLetterCard.CardType.Countess) {
            if (this.forcedCountessCardType != null) {
                return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                        "<html><h2>Countess Action</h2><p>Since the other card in your hand is "
                                + this.forcedCountessCardType + ", the Countess will be played.</p></html>"));
            } else {
                return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                        "<html><h2>Countess Action</h2><p>Nothing happen.</p></html>"));
            }
        }
        // I think there's no way to visit here...
        System.err.println("createDialogWithFeedbackForNewbie???");
        return Lists.newArrayList();
    }
}
