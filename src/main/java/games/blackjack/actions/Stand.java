package games.blackjack.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IPrintable;
import games.blackjack.BlackjackGameState;
import guide.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Stand extends AbstractAction implements IPrintable {

    @Override
    public boolean execute(AbstractGameState gs) {
        // Nothing to do
        return true;
    }

    @Override
    public AbstractAction copy() {
        return this; //immutable
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Stand;
    }

    @Override
    public int hashCode() {
        return 904344;
    }

    @Override
    public String getString(AbstractGameState gameState){
        return "Stand";
    }

    @Override
    public String toString() {
        return "Stand";
    }

    @Override
    public ArrayList<JDialog> createDialogWithFeedbackForNewbie(Frame frame, AbstractGameState gameState, int currentPlayer) {
        BlackjackGameState gs = (BlackjackGameState) gameState;
        ArrayList<JDialog> result = new ArrayList<>();
        if (currentPlayer == gs.getDealerPlayer()) {
            result.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                    "<html><p>Because the score is greater than or equal to 17, " +
                            "the dealer do the Stand Action(no need to get a new card from the deck).</p></html>"));
        }
        result.add(DialogUtils.create(frame, "Game Guide", Boolean.TRUE, 300, 200,
                "<html><h2>Stand Action</h2><p>No cards will be drawn. Next it's player "
                + gs.getCurrentPlayer() + "'s turn</p></html>"));
        return result;
    }
}
