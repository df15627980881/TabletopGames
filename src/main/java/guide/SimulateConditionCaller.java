package guide;

import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import games.blackjack.BlackjackGameState;
import games.loveletter.LoveLetterGameState;
import games.loveletter.actions.BaronAction;
import games.loveletter.actions.GuardAction;
import games.loveletter.actions.PlayCard;
import games.loveletter.actions.PrinceAction;
import org.testng.Assert;
import scala.concurrent.impl.FutureConvertersImpl;
import utilities.Pair;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class SimulateConditionCaller {

    /**
     * Common check in Blackjack. Whether all players except the dealer have won.
     */
    public Pair<Boolean, Boolean> method1(Game game) {
//        System.out.println("Executing method1");
        BlackjackGameState gs = (BlackjackGameState) game.getGameState();
        CoreConstants.GameResult[] results = gs.getPlayerResults();
        boolean ok = true;
        for (int i=0; i<gs.getNPlayers()-1; ++i) {
            ok &= results[i] == CoreConstants.GameResult.WIN_GAME;
        }
        ok &= results[gs.getNPlayers()-1] == CoreConstants.GameResult.LOSE_GAME;
        return new Pair<>(!gs.isNotTerminal(), ok);
    }

    /**
     * Check if the novice played the Prince against player1
     */
    public Pair<Boolean, Boolean> method2(Game game) {
//        System.out.println("Executing method2");
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();
        int player = history.get(history.size()-1).a;
        AbstractAction action = history.get(history.size()-1).b;
        if (action instanceof PrinceAction) {
            PrinceAction pa = (PrinceAction) action;
            Assert.assertEquals(player, pa.getPlayerID());
            if (pa.getPlayerID() == 0 && pa.getTargetPlayer() == 1) {
                return new Pair<>(true, true);
            }
        }
        return new Pair<>(true, false);
    }

    /**
     * Check if the novice played the Baron.
     */
    public Pair<Boolean, Boolean> method3(Game game) {
//        System.out.println("Executing method3");
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();
        int player = history.get(history.size()-1).a;
        AbstractAction action = history.get(history.size()-1).b;
        if (!(action instanceof BaronAction)) {
            PlayCard playCard = (PlayCard) action;
            Assert.assertEquals(player, playCard.getPlayerID());
            if (playCard.getPlayerID() == 0) {
                return new Pair<>(true, true);
            } else {
                return new Pair<>(true, false);
            }
        }
        return new Pair<>(true, false);
    }

    /**
     * Check whether the novice played the Guard and guessed it to be the Priest, Baron, Handmaid, or Prince
     */
    public Pair<Boolean, Boolean> method4(Game game) {
//        System.out.println("Executing method4");
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();
        int player = history.get(history.size()-1).a;
        AbstractAction action = history.get(history.size()-1).b;
        if (action instanceof GuardAction) {
            GuardAction playCard = (GuardAction) action;
            Assert.assertEquals(player, playCard.getPlayerID());
            if (playCard.getPlayerID() == 0 && playCard.getTargetCardType().getValue() >= 2 && playCard.getTargetCardType().getValue() <= 5) {
                return new Pair<>(true, true);
            } else {
                return new Pair<>(true, false);
            }
        }
        return new Pair<>(true, false);
    }

    /**
     * Check if the novice played the Guard against player2
     */
    public Pair<Boolean, Boolean> method5(Game game) {
//        System.out.println("Executing method5");
        LoveLetterGameState gs = (LoveLetterGameState) game.getGameState();
        List<Pair<Integer, AbstractAction>> history = gs.getHistory();
        int player = history.get(history.size()-1).a;
        AbstractAction action = history.get(history.size()-1).b;
        if (action instanceof GuardAction) {
            GuardAction playCard = (GuardAction) action;
            Assert.assertEquals(player, playCard.getPlayerID());
            if (playCard.getPlayerID() == 0 && playCard.getTargetPlayer() == 2) {
                return new Pair<>(true, true);
            } else {
                return new Pair<>(true, false);
            }
        }
        return new Pair<>(true, false);
    }


    public Pair<Boolean, Boolean> callMethod(String methodName, Game game) {
        try {
            Method method;
            Object result;
            if (game != null) {
                method = this.getClass().getDeclaredMethod(methodName, Game.class);
                result = method.invoke(this, game);
            } else {
                method = this.getClass().getDeclaredMethod(methodName);
                result = method.invoke(this);
            }
            return (Pair<Boolean, Boolean>) result;
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(false, false);
        }
    }

}
