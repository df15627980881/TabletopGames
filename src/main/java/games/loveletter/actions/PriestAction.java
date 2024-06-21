package games.loveletter.actions;

import core.AbstractGameState;
import core.CoreConstants;
import core.components.PartialObservableDeck;
import core.interfaces.IPrintable;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;
import guide.DialogUtils;
import org.testng.Assert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * The Priest allows a player to see another player's hand cards.
 * This has no effect in case the game is fully observable.
 */
public class PriestAction extends PlayCard implements IPrintable {

    public PriestAction(int cardIdx, int playerID, int opponentID, boolean canExecuteEffect, boolean discard) {
        super(LoveLetterCard.CardType.Priest, cardIdx, playerID, opponentID, null, null, canExecuteEffect, discard);
    }

    @Override
    protected boolean _execute(LoveLetterGameState llgs) {
        PartialObservableDeck<LoveLetterCard> opponentDeck = llgs.getPlayerHandCards().get(targetPlayer);

        // Set all cards to be visible by the current player
        for (int i = 0; i < opponentDeck.getComponents().size(); i++)
            opponentDeck.setVisibilityOfComponent(i, playerID, true);

        targetCardType = opponentDeck.get(0).cardType;
        if (llgs.getCoreGameParameters().recordEventHistory) {
            llgs.recordHistory("Priest sees " + targetCardType);
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof PriestAction;
    }

    @Override
    public PriestAction copy() {
        PriestAction copy = new PriestAction(cardIdx, playerID, targetPlayer, canExecuteEffect, discard);
        copy.targetCardType = targetCardType;
        return copy;
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Priest: see p" + targetPlayer;
    }

    @Override
    public ArrayList<JDialog> createDialogWithFeedbackForNewbie(Frame frame, AbstractGameState gameState, int currentPlayer) {
        ArrayList<JDialog> results = new ArrayList<>();
        Assert.assertEquals(currentPlayer, playerID);
        results.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                "<html><h2>Priest Action</h2><p>The player" + playerID + " saw the card in player" + targetPlayer
                        + "'s hand, and the result is targetCardType.</p></html>"));
        return results;
    }
}
