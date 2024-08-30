package games.loveletter.actions;

import core.AbstractGameState;
import core.interfaces.IPrintable;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;
import guide.DialogUtils;
import org.testng.Assert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * The handmaid protects the player from any targeted effects until the next turn.
 */
public class HandmaidAction extends PlayCard implements IPrintable {

    public HandmaidAction(int cardIdx, int playerID) {
        super(LoveLetterCard.CardType.Handmaid, cardIdx, playerID, -1, null, null, true, true);
    }

    @Override
    protected boolean _execute(LoveLetterGameState gs) {
        // set the player's protection status
        gs.setProtection(playerID, true);
        return true;
    }

    @Override
    public HandmaidAction copy() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof HandmaidAction && super.equals(o);
    }

    @Override
    public ArrayList<JDialog> createDialogWithFeedbackForNewbie(Frame frame, AbstractGameState gameState, int currentPlayer) {
        ArrayList<JDialog> results = new ArrayList<>();
        Assert.assertEquals(currentPlayer, playerID);
        results.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                "<html><h2>Handmaid Action</h2><p>The handmaid protects the player " + playerID +
                        " from any targeted effects until the next turn.</p></html>"));
        return results;
    }
}
