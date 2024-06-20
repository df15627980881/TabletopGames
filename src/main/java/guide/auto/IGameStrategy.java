package guide.auto;

import core.AbstractGameState;
import core.Game;

public interface IGameStrategy {

    boolean isValid(String strategy, Game game, Long seed);

    void exportJson();

    boolean isEnd();

    /**
     * Record each round initial deck for guide generate, each deck is dependant with gs and synchronize with history
     * @param gameState
     */
    void recordDeck(AbstractGameState gameState);
}
