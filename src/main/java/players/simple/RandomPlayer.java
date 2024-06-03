package players.simple;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import utilities.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomPlayer extends AbstractPlayer {

    /**
     * Random generator for this agent.
     */
    public RandomPlayer(Random rnd) {
        super(null, "RandomPlayer");
        this.rnd = rnd;
    }

    public RandomPlayer()
    {
        this(new Random());
    }

    @Override
    public AbstractAction _getAction(AbstractGameState observation, List<AbstractAction> actions) {
        ArrayList<AbstractAction> results = new ArrayList<>();
        for (AbstractAction action : actions) {
            AbstractAction bannedAction = this.getBannedAction();
            if (Objects.nonNull(bannedAction)) {
                Class<?> bannedActionClass = bannedAction.getClass();
                if (!bannedActionClass.isInstance(action)) {
                    results.add(action);
                }
            } else {
                results = new ArrayList<>(actions);
            }
        }
//        System.out.println("actions: " + actions.size() + " results: " + results.size());
        int randomAction = rnd.nextInt(results.size());
        return results.get(randomAction);
    }

    @Override
    public String toString() {
        return "Random";
    }

    @Override
    public RandomPlayer copy() {
        return new RandomPlayer(new Random(rnd.nextInt()));
    }
}
