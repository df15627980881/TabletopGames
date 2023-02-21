package core;

import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import evaluation.metrics.Event;

import java.util.Arrays;

import static core.CoreConstants.GameResult.GAME_ONGOING;
import static core.CoreConstants.GameResult.TIMEOUT;
import static evaluation.metrics.Event.GameEvent.ROUND_OVER;
import static evaluation.metrics.Event.GameEvent.TURN_OVER;

public abstract class StandardForwardModel extends AbstractForwardModel {

    @Override
    protected void _next(AbstractGameState currentState, AbstractAction action) {
        _beforeAction(currentState, action);
        if (action != null) {
            action.execute(currentState);
        } else {
            throw new AssertionError("No action selected by current player");
        }
        // We then register the action with the top of the stack ... unless the top of the stack is this action
        // in which case go to the next action
        // We can't just register with all items in the Stack, as this may represent some complex dependency
        // For example in Dominion where one can Throne Room a Throne Room, which then Thrones a Smithy
        if (currentState.actionsInProgress.size() > 0) {
            IExtendedSequence topOfStack = currentState.actionsInProgress.pop();
            if (topOfStack != action) {
                topOfStack.registerActionTaken(currentState, action);
            } else {
                if (currentState.actionsInProgress.size() > 0) {
                    IExtendedSequence nextOnStack = currentState.actionsInProgress.peek();
                    nextOnStack.registerActionTaken(currentState, action);
                }
            }
            currentState.actionsInProgress.push(topOfStack);
        }
        _afterAction(currentState, action);
    }

    /**
     * This is a method hook for any game-specific functionality that should run before an Action is executed
     * by the forward model
     *
     * @param currentState - the current game state
     * @param actionChosen - the action chosen by the current player, not yet applied to the game state
     */
    protected void _beforeAction(AbstractGameState currentState, AbstractAction actionChosen) {
        // override if needed
    }

    /**
     * This is a method hook for any game-specific functionality that should run after an Action is executed
     * by the forward model
     *
     * @param currentState the current game state
     * @param actionTaken  the action taken by the current player, already applied to the game state
     */
    protected void _afterAction(AbstractGameState currentState, AbstractAction actionTaken) {
        // override if needed
    }

    /**
     * End the player turn. This will publish a TURN_OVER event, increases the turn counter and changes the currentPlayer on the
     * game state to be the one specified.
     *
     * @param gs         - game state to end current player's turn in.
     * @param nextPlayer - the player whose turn it is next.
     */
    public final void endPlayerTurn(AbstractGameState gs, int nextPlayer) {
        if (gs.getGameStatus() != GAME_ONGOING) return;

        gs.getPlayerTimer()[gs.getCurrentPlayer()].incrementTurn();
        gs.listeners.forEach(l -> l.onEvent(Event.createEvent(TURN_OVER, gs)));
        if (gs.getCoreGameParameters().recordEventHistory) {
            gs.recordHistory(TURN_OVER.name());
        }
        gs.turnCounter++;
        gs.turnOwner = nextPlayer;
    }

    /**
     * <p>The default assumption is that after a player has finished their turn, play will proceed
     * sequentially to the next player, looping back to the current first player once all players have acted.
     * If this is not the case, then use the alternative method with a specific argument for the next player</p>
     *
     * <p>It is the responsibility of the game-specific forward model that extends this class to call endPlayerTurn()
     * and perform any other end-of-turn game logic.</p>
     *
     * @param gs - game state to end current player's turn in.
     */
    @Override
    public final void endPlayerTurn(AbstractGameState gs) {
        endPlayerTurn(gs, (gs.turnOwner + 1) % gs.nPlayers);
    }

    /**
     * <p>Method executed at the end of a Round (however that is defined in a game).
     * It increments player timers and publishes a ROUND_OVER event.
     * It resets the turn counter to 0, sets the currentPlayer to the one specified (0 in default method),
     * and increments the Round counter.
     * If maximum number of rounds is set, and it is reached, the game ends.
     * If there are no players still playing, the game ends and method returns.</p>
     *
     * <p>It is the responsibility of the game-specific forward model that extends this class to call endRound()</p>
     *
     * @param gs                     - current game state.
     * @param firstPlayerOfNextRound the first player to act in the next round
     */
    public final void endRound(AbstractGameState gs, int firstPlayerOfNextRound) {
        if (gs.getGameStatus() != GAME_ONGOING) return;

        gs.getPlayerTimer()[gs.getCurrentPlayer()].incrementRound();
        gs.listeners.forEach(l -> l.onEvent(Event.createEvent(ROUND_OVER, gs)));
        if (gs.getCoreGameParameters().recordEventHistory) {
            gs.recordHistory(ROUND_OVER.name());
        }
        gs.roundCounter++;
        if (gs.getGameParameters().maxRounds != -1 && gs.roundCounter == gs.getGameParameters().maxRounds) {
            endGame(gs); // we end the game validly
        } else if (gs.getGameParameters().timeoutRounds != -1 && gs.roundCounter == gs.getGameParameters().timeoutRounds) {
            endGame(gs);
            // then we override the Result to be Timeout
            gs.setGameStatus(TIMEOUT);
            Arrays.fill(gs.playerResults, TIMEOUT);
        } else {
            gs.turnCounter = 0;
            gs.turnOwner = firstPlayerOfNextRound;
            gs.firstPlayer = firstPlayerOfNextRound;
        }
    }

    /**
     * End a round, with no change to the firstPlayer
     * @param gs
     */
    public final void endRound(AbstractGameState gs) {
        endRound(gs, gs.firstPlayer);
    }

    /**
     * A Forward Model should be stateless - and hence have no need to implement a _copy() method
     *
     * @return this
     */
    @Override
    public final StandardForwardModel _copy() {
        return this;
    }
}
