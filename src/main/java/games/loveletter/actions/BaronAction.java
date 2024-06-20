package games.loveletter.actions;

import core.AbstractGameState;
import core.CoreConstants;
import core.components.Deck;
import core.components.PartialObservableDeck;
import core.interfaces.IPrintable;
import games.loveletter.LoveLetterGameState;
import games.loveletter.LoveLetterParameters;
import games.loveletter.cards.LoveLetterCard;
import guide.DialogUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.testng.Assert;
import utilities.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * The Baron lets two players compare their hand card. The player with the lesser valued card is removed from the game.
 */
public class BaronAction extends PlayCard implements IPrintable {
    private transient LoveLetterCard.CardType playerCard;
    private transient LoveLetterCard.CardType opponentCard;
    private Pair<LoveLetterCard.CardType, LoveLetterCard.CardType> playAndOpponentCardTypeForGuide;

    public BaronAction(int cardIdx, int playerID, int opponentID, boolean canExecuteEffect, boolean discard) {
        super(LoveLetterCard.CardType.Baron, cardIdx, playerID, opponentID, null, null, canExecuteEffect, discard);
    }

    @Override
    protected boolean _execute(LoveLetterGameState llgs) {
        Deck<LoveLetterCard> opponentDeck = llgs.getPlayerHandCards().get(targetPlayer);
        PartialObservableDeck<LoveLetterCard> playerDeck = llgs.getPlayerHandCards().get(playerID);

        // compares the value of the player's hand card with another player's hand card
        // the player with the lesser valued card will be removed from the game
        LoveLetterCard opponentCard = opponentDeck.peek();
        LoveLetterCard playerCard = playerDeck.peek();
        if (opponentCard != null && playerCard != null) {
            this.otherCardInHand = playerCard.cardType;
            this.targetCardType = opponentCard.cardType;
            playAndOpponentCardTypeForGuide = new Pair<>(LoveLetterCard.CardType.valueOf(playerCard.cardType.name()),
                    LoveLetterCard.CardType.valueOf(opponentCard.cardType.name()));
            if (opponentCard.cardType.getValue() < playerCard.cardType.getValue())
                llgs.killPlayer(playerID, targetPlayer, cardType);
            else if (playerCard.cardType.getValue() < opponentCard.cardType.getValue())
                llgs.killPlayer(playerID, playerID, cardType);
        } else {
            System.err.println("player with ID " + targetPlayer + " was targeted using a Baron card" +
                    " but one of the players has no cards left.");
            throw new IllegalArgumentException("player with ID " + targetPlayer + " was targeted using a Baron card" +
                    " but one of the players has no cards left.");
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof BaronAction;
    }

    @Override
    public BaronAction copy() {
        BaronAction copy = new BaronAction(cardIdx, playerID, targetPlayer, canExecuteEffect, discard);
        copy.targetCardType = targetCardType;
        copy.otherCardInHand = otherCardInHand;
        return copy;
    }
    @Override
    public ArrayList<JDialog> createDialogWithFeedbackForNewbie(Frame frame, AbstractGameState gameState, int currentPlayer) {
        ArrayList<JDialog> results = new ArrayList<>();
        Assert.assertEquals(currentPlayer, playerID);

        LoveLetterGameState llgs = (LoveLetterGameState) gameState;
        if (targetPlayer == -1) {
            return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Baron Action</h2><p>No one can be guessed. Nothing happen.</p></html>"));
        }
        if (llgs.getPlayerResults()[targetPlayer] == CoreConstants.GameResult.LOSE_ROUND) {
            return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Baron Action</h2><p>The player" + playerID + " and the player" + targetPlayer
                            + " compare the values of the cards in their hands, resulting in " +
                            "Player" + playerID + " winning the game and Player" + targetPlayer + " being eliminated.</p></html>"));
        }
        if (llgs.getPlayerResults()[playerID] == CoreConstants.GameResult.LOSE_ROUND) {
            return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Baron Action</h2><p>The player" + playerID + " and the player" + targetPlayer
                            + " compare the values of the cards in their hands, resulting in " +
                            "Player" + targetPlayer + " winning the game and Player" + playerID + " being eliminated.</p></html>"));
        }
        results.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Baron Action</h2><p>The player" + playerID + " and the player" + targetPlayer
                            + " compare the values of the cards in their hands, resulting in drawing and nobody being eliminated.</p></html>"));
        return results;
    }

}
