package games.loveletter.actions;

import core.AbstractGameState;
import core.CoreConstants;
import core.AbstractGameState;
import core.components.Deck;
import core.interfaces.IPrintable;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;
import guide.DialogUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.testng.Assert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The guard allows to attempt guessing another player's card. If the guess is correct, the targeted opponent
 * is removed from the game.
 */
public class GuardAction extends PlayCard implements IPrintable {

    public GuardAction(int cardIdx, int playerID, int opponentID, LoveLetterCard.CardType cardtype, boolean canExecuteEffect, boolean discard) {
        super(LoveLetterCard.CardType.Guard, cardIdx, playerID, opponentID, cardtype, null, canExecuteEffect, discard);
    }

    @Override
    protected boolean _execute(LoveLetterGameState llgs) {
        if (targetCardType == null) return false;

        Deck<LoveLetterCard> opponentDeck = llgs.getPlayerHandCards().get(targetPlayer);

        // guess the opponent's card and remove the opponent from play if the guess was correct
        LoveLetterCard card = opponentDeck.peek();
        if (card.cardType == this.targetCardType) {
            llgs.killPlayer(playerID, targetPlayer, cardType);
            if (llgs.getCoreGameParameters().recordEventHistory) {
                llgs.recordHistory("Guard guess correct!");
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GuardAction && super.equals(o);
    }

    @Override
    public GuardAction copy() {
        return this;
    }
    @Override
    public String getString(AbstractGameState gameState) {
        return "Guard: guess p" + targetPlayer + " has " + targetCardType.name();
    }

    @Override
    public ArrayList<JDialog> createDialogWithFeedbackForNewbie(Frame frame, AbstractGameState gameState, int currentPlayer) {
        ArrayList<JDialog> results = new ArrayList<>();
        Assert.assertEquals(currentPlayer, playerID);
        LoveLetterGameState llgs = (LoveLetterGameState) gameState;
        if (targetPlayer == -1) {
            return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Guard Action</h2><p>No one can be guessed. Nothing happen.</p></html>"));
        }
        Deck<LoveLetterCard> opponentDeck = llgs.getPlayerHandCards().get(targetPlayer);
        LoveLetterCard card = opponentDeck.peek();
        if (Objects.isNull(card)) {
            return Lists.newArrayList(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Guard Action</h2><p>No one can be guessed. Nothing happen.</p></html>"));
        }
        if (llgs.getPlayerResults()[targetPlayer] == CoreConstants.GameResult.LOSE_ROUND) {
            results.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Guard Action</h2><p>The player" + playerID + " guess the card in player" + targetPlayer
                            + "'s hand(guessing " + targetCardType + ", the actual card is " + targetCardType +
                            "), the result is that Player " + playerID + "'s guess is correct, leading to Player "
                            + targetPlayer + " being eliminated.</p></html>"));
        } else {
            results.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><h2>Guard Action</h2><p>The player" + playerID + " guess the card in player" + targetPlayer
                            + "'s hand(guessing " + targetCardType + ", the actual card is " + card.cardType +
                            "), the result is that Player " + playerID + "'s guess is wrong, leading nothing.</p></html>"));
        }
        return results;
    }
}
